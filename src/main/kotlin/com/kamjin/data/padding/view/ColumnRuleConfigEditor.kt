package com.kamjin.data.padding.view

import cn.hutool.db.ds.simple.*
import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.data.*
import com.kamjin.data.padding.model.*
import com.kamjin.data.padding.style.*
import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.stage.*
import javafx.util.*
import tornadofx.*
import java.util.*
import javax.sql.*
import kotlin.properties.*
import tornadofx.getValue
import tornadofx.setValue
import java.io.*

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/04/27
 */

/**
 * 列规则editor
 */
class ColumnRuleConfigEditor : View() {

    lateinit var innerFunCheckBox: ComboBox<String>

    private val model by inject<ColumnRuleConfig>()

    private val tableMetadataController by inject<TableMetadataController>()

    private val selectRuleToggleGroup = ToggleGroup()

    private val customCodeFilters: ObservableList<ScriptInputItem> = observableListOf()

    lateinit var paramTextProperty: StringProperty

    override val root = form {
        style {
            alignment = Pos.TOP_RIGHT
            setMinWidth(510.0)
            setMinHeight(800.0)
        }

        vbox(30) {
            //列信息区
            hbox {
                text("列信息：") {
                    addClass(TextStyle.title)
                }
                textflow() {
                    text(model.tableName)
                    text(" | ")
                    text(model.columnName)
                    text(" | ")
                    text(model.columnComment)
                    text(" | ")
                    text(model.columnType)
                    text(" | ")
                    text(model.columnLength.asString())
                }.hiddenWhen { model.columnName.isBlank() }
            }

            //规则选择 单选
            hbox {

                //model rule
                selectRuleToggleGroup.bind(model.selectedRule)

                text("规则：") {
                    addClass(TextStyle.title)
                }
                hbox(40) {
                    vbox(10) {
                        ColumnConfigRoleEnum.values().forEach {
                            radiobutton(it.desc, selectRuleToggleGroup, it.name)
                        }
                    }

                    text("快捷选择>>>").addClass(TextStyle.title)

                    vbox(20) {
                        hbox {
                            text("内置函数选择：")
                            val innerFunMap = obtainInnerFunMap()
                            innerFunCheckBox =
                                combobox(property = model.ruleFunName, values = innerFunMap.keys.toList()) {
                                    selectionModel.selectedItemProperty().addListener { obs, old, new ->
                                        model.ruleFunName.set(new)
                                        model.ruleFun.set(innerFunMap[new])

                                        //if the rule not select, auto select the innerFunRule
                                        if (model.selectedRule.value != ColumnConfigRoleEnum.innerFun.name) {
                                            model.selectedRule.set(ColumnConfigRoleEnum.innerFun.name)
                                        }
                                    }
                                }
                        }

                        button("其他表字段选择") {
                            val otherTableColumnMetadata = objectProperty<ColumnMetadata>()
                            action {
                                val selectOtherColumnView = object : View() {
                                    private val allColumnMetadatas =
                                        tableMetadataController.queryAllTableInfos().flatMap { it.columnMetadatas }

                                    private val rootChilds = allColumnMetadatas
                                        .map { it.tableName }
                                        .distinct().map { ColumnMetadata(name = it, tableName = "") }

                                    override val root = treeview<ColumnMetadata> tree@{
                                        root = TreeItem(ColumnMetadata(name = "tables"))
                                        cellFormat { text = it.name }
                                        populate { parent ->
                                            if (parent == root) rootChilds else allColumnMetadatas.filter { it.tableName == parent.value.name }
                                        }

                                        selectionModel.selectedItemProperty().onChange {
                                            otherTableColumnMetadata.set(it?.value)
                                        }
                                    }
                                }
                                selectOtherColumnView.openWindow()

                                selectOtherColumnView.root.childrenUnmodifiable.forEach { item ->
                                    item.setOnMouseClicked {
                                        if (it.clickCount == 2 && it.button.name == "PRIMARY") { //left button click twice
                                            //set the textShow
                                            val otherTableColumnKey = otherTableColumnMetadata.get().key
                                            paramTextProperty.set(otherTableColumnKey)

                                            //check and auto select the rule
                                            if (model.selectedRule.get() != ColumnConfigRoleEnum.withOtherTableColumn.name) {
                                                model.selectedRule.set(ColumnConfigRoleEnum.withOtherTableColumn.name)
                                            }

                                            //set value to model
                                            model.otherTableColumnKey.set(otherTableColumnKey)

                                            //close
                                            selectOtherColumnView.close()
                                        }
                                    }
                                }
                            }
                        }

                    }


                }

                //not innerFun selected dont show this
                selectRuleToggleGroup.selectedToggleProperty().addListener { obs, old, new ->
                    val toggleBean = new?.toggleGroupProperty()?.bean
                    if (toggleBean != null && toggleBean is RadioButton) {

                        with(innerFunCheckBox) {
                            val innerFunSelected = (toggleBean.text == ColumnConfigRoleEnum.innerFun.name).toProperty()
                            if (innerFunSelected.value) show() else hide()
                        }
                    }
                }
            }

            //规则函数参数
            hbox {
                text("规则函数参数：") {
                    addClass(TextStyle.title)
                }
                textarea(model.ruleFunParam) {
                    maxHeight = 120.0
                    maxWidth = 500.0

                    tooltip = Tooltip("rule method params,eg:['a','b','c']")
                    paramTextProperty = textProperty()
                    paramTextProperty.addListener { obs, old, new ->
                        log.info("规则函数参数 You typed: $new")

                        model.ruleFunParam.set(new)

                        //other table column key
                        if (model.selectedRule.get() == ColumnConfigRoleEnum.withOtherTableColumn.name) {
                            model.otherTableColumnKey.set(new)
                        }
                    }
                }
            }

            //自定义多个过滤器获取内容 支持js和sql
            vbox(10) scriptInputVbox@{
                text("自定义过滤器：") {
                    addClass(TextStyle.title)
                }
                button("+").action {
                    if (model.selectedRule.get() != ColumnConfigRoleEnum.custom.name) {
                        alert(
                            type = Alert.AlertType.WARNING,
                            header = "unselect custom rule!",
                            content = "please select custom rule"
                        )
                        return@action
                    }

                    val inputItem = ScriptInputItem()
                    customCodeFilters.add(inputItem)

                    val theTogglebutton = togglebutton {
                        val stateText = selectedProperty().stringBinding {
                            val s = if (it == true) ScriptType.JS else ScriptType.SQL
                            inputItem.type = s
                            s.name
                        }
                        textProperty().bind(stateText)
                    }

                    hbox scriptItemHbox@{
                        //content
                        val theScriptField = textfield {
                            textProperty().addListener { _, _, new ->
                                log.info("自定义过滤器，当前值: $new")
                            }

                            textProperty().bind(inputItem.scriptProperty)
                        }

                        button("-") {
                            action {
                                //remove the filter
                                this@scriptInputVbox.children.remove(theTogglebutton)
                                this@scriptItemHbox.children.remove(theScriptField)
                                this@scriptItemHbox.children.remove(this)
                            }
                        }
                    }
                }
            }

            buttonbar {
                button("重置") {
                    model.rollback()
                }
                button("保存") {
                    enableWhen(model.dirty)
                    action {
                        model.commit {
                            //save customScriptFilters
                            model.customScriptFilters.addAll(customCodeFilters)

                            //column key
                            val key = model.item.key

                            //create expression by role type
                            val expression = createExpressionsByRoleType(model.item)
                            if (expression != null) {
                                putExpression(key, expression)
                            }
                        }

                        //save Local cache
                        tableMetadataController.saveLocalCache()
                        tableMetadataController.saveUsedConfigCache()
                    }
                }
            }
        }
    }
}


class ScriptInputItem : JsonModelAuto {

    val typeProperty = SimpleObjectProperty<ScriptType>()
    var type by typeProperty

    val scriptProperty = SimpleStringProperty()
    var script by scriptProperty
}

enum class ScriptType {
    UNKNOW,
    JS,
    SQL
}

enum class ColumnConfigRoleEnum(val desc: String) {
    doNoting("无需配置"),
    withOtherTableColumn("其他表字段"),
    innerFun("内置函数"),
    custom("自定义")
}