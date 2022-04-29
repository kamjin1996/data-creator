package com.kamjin.data.padding.view

import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.model.*
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

    val model: ColumnRuleConfigModel by inject()

    override val root = vbox {
        tableview(tableMetadataController.queryAllTableInfos()) {
            readonlyColumn("表名", TableMetadata::name)
            readonlyColumn("备注", TableMetadata::comment)
            rowExpander(expandOnDoubleClick = true) {
                paddingLeft = expanderColumn.width
                tableview(it.columnMetadatas) {
                    readonlyColumn("列名", ColumnMetadata::name)
                    readonlyColumn("备注", ColumnMetadata::comment)
                    readonlyColumn("类型", ColumnMetadata::type)
                    readonlyColumn("长度", ColumnMetadata::length)
                    //model change
                    bindSelected(model)

                    selectionModel.selectedItemProperty().onChange {
                        println("change ${it?.name}")
                    }
                }
            }
        }
    }
}
