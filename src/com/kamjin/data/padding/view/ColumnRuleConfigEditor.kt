package com.kamjin.data.padding.view

import cn.hutool.db.ds.simple.*
import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.model.*
import javafx.beans.property.*
import javafx.collections.*
import javafx.scene.*
import javafx.scene.control.*
import javafx.stage.*
import javafx.util.*
import tornadofx.*
import java.util.*
import javax.sql.*
import kotlin.properties.*
import tornadofx.getValue
import tornadofx.setValue

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

    private val sqlParseController = find<SqlParseController>()

    private val innerFunMap = mapOf<String, InnerFun>(
        "autoId" to { maker, param -> maker.autoId(param).toString() },
        "choice" to { maker, param -> Maker.choice(param) },
        "order" to { maker, param -> Maker().order(param).toString() },
        "time" to { maker, param -> Maker().time(param).toString() }
    )

    lateinit var innerFunCheckBox: ComboBox<String>

    private val model: ColumnRuleConfigModel by inject()

    private val selectRuleToggleGroup = ToggleGroup()

    private val customCodeFilters: ObservableList<ScriptInputItem> = observableListOf()

    override val root = form {
        vbox(30) {
            //列信息区
            group {
                text("列信息：")
                hbox(20) {
                    text(model.tableName)
                    text(model.columnName)
                    text(model.columnComment)
                    text(model.columnType)
                    text(model.columnLength.asString())
                }
            }

            //规则选择 单选
            group {

                //model rule
                selectRuleToggleGroup.bind(model.selectedRule)

                text("规则：")
                hbox(40) {
                    vbox {
                        ColumnConfigRoleEnum.values().map {
                            radiobutton(it.desc, selectRuleToggleGroup).action {
                                model.selectedRule.set(it.name)
                            }
                        }
                    }

                    vbox {
                        innerFunCheckBox = combobox(property = model.ruleFunName, values = innerFunMap.keys.toList()) {
                            selectionModel.selectedItemProperty().addListener { obs, old, new ->
                                model.ruleFunName.set(new)
                                model.ruleFun.set(innerFunMap[new])
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
            group {
                text("规则函数参数：")
                textarea(model.ruleFunParam) {
                    maxHeight = 100.0
                    maxWidth = 300.0

                    tooltip = Tooltip("rule method params,eg:['a','b','c']")
                    textProperty().addListener { obs, old, new ->
                        println("规则函数参数 You typed: " + new)
                        model.ruleFunParam.set(new)
                        if (model.selectedRule.get() == ColumnConfigRoleEnum.withOtherTableColumn.name) {
                            model.otherTableColumnKey.set(new)
                        }
                    }
                }
            }

            //自定义多个过滤器获取内容 支持js和sql
            group {
                text("自定义过滤器：")
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

                    togglebutton {
                        val stateText = selectedProperty().stringBinding {
                            val s = if (it == true) ScriptType.JS else ScriptType.SQL
                            inputItem.type = s
                            s.name
                        }
                        textProperty().bind(stateText)
                    }

                    //content
                    textfield {
                        textProperty().addListener { obs, old, new ->
                            println("自定义过滤器 You typed: " + new)
                        }

                        textProperty().bind(inputItem.scriptProperty)
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
                        val expression = createExpressionsByRoleType()
                        sqlParseController.putColumnRuleExpression(key, expression)
                    }
                }
            }
        }
    }

    /**
     * create expressions role type
     */
    private fun createExpressionsByRoleType(): ColumnRuleExpression {
        return when (ColumnConfigRoleEnum.valueOf(model.selectedRule.get())) {
            ColumnConfigRoleEnum.doNoting -> NothingExpression()
            ColumnConfigRoleEnum.withOtherTableColumn -> OtherTableColumnExpression(model.otherTableColumnKey.get())
            ColumnConfigRoleEnum.innerFun -> InnerFunExpression(model.ruleFun.get(), model.ruleFunParam.get())
            ColumnConfigRoleEnum.custom -> model.customScriptFilters.map {
                val filters = when (it.type!!) {
                    ScriptType.UNKNOW -> NothingExpression()
                    ScriptType.SQL -> SqlCodeExpression(it.script)
                    ScriptType.JS -> JsCodeExpression(it.script)
                }
                return@map filters
            }.apply {
                //set all next filter
                val iterator = this.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (iterator.hasNext()) {
                        next.setNext(iterator.next())
                    }
                }
            }.first()
        }
    }
}

class ScriptInputItem {

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
    doNoting("doNothing"),
    withOtherTableColumn("withOtherTableColumn"),
    innerFun("innerFun"),
    custom("custom")
}