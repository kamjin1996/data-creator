package com.kamjin.data.padding.view

import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.model.*
import javafx.scene.paint.*
import tornadofx.*
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
 * 表元数据控制器
 */
class TableMetadataView : View() {

    private val tableMetadataController = find(TableMetadataController::class)

    val columnModel: ColumnRuleConfig by inject()

    val tableModel: TableRuleConfig by inject()

    override val root = vbox(20) {
        textflow {
            text("当前配置文件所在目录：")
            text() { bind(tableMetadataController.currentUseConfigDir) }
        }

        textflow {
            text("默认条数：")
            textfield() {
                bind(tableModel.recordCount)

                setOnMouseExited {
                    //rm focus
                    focusTraversableProperty().set(false)

                    tableModel.commit() {

                        //save Local cache
                        tableMetadataController.saveLocalCache()
                        tableMetadataController.saveUsedConfigCache()
                    }
                }
            }
        }

        tableview(tableMetadataController.queryAllTableInfos()) {
            style {
                setMinWidth(510.0)
                setMinHeight(800.0)
                autosize()
            }

            readonlyColumn("表名", TableMetadata::name)
            readonlyColumn("备注", TableMetadata::comment)

            bindSelected(tableModel)

            rowExpander(expandOnDoubleClick = true) {
                paddingLeft = expanderColumn.width
                tableview(it.columnMetadatas) {
                    readonlyColumn("列名", ColumnMetadata::name)
                    readonlyColumn("备注", ColumnMetadata::comment)
                    readonlyColumn("规则", ColumnMetadata::selectRule) {
                        cellFormat {
                            this.text = this.item
                            style {
                                backgroundColor += c("#7BEA3A")
                                textFill = Color.BLACK
                            }
                        }
                    }

                    //model change
                    bindSelected(columnModel)

                    selectionModel.selectedItemProperty().onChange {
                        log.info("change ${it?.name}")
                    }
                }
            }
        }
    }
}
