package com.kamjin.data.padding.view

import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.model.*
import javafx.scene.paint.*
import tornadofx.*

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

    val model: ColumnRuleConfig by inject()

    override val root = vbox {
        tableview(tableMetadataController.queryAllTableInfos()) {
            style {
                setMinWidth(510.0)
                setMinHeight(800.0)
            }

            readonlyColumn("表名", TableMetadata::name)
            readonlyColumn("备注", TableMetadata::comment)
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
                    bindSelected(model)

                    selectionModel.selectedItemProperty().onChange {
                        log.info("change ${it?.name}")
                    }
                }
            }
        }
    }
}
