package com.kamjin.data.padding.view

import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.data.*
import javafx.beans.property.*
import tornadofx.*

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/05/06
 */
class TableMetadataImportView : View() {

    val tableMetadataController = find<TableMetadataController>()

    val models
        get() = tableMetadataController.queryTableBaseInfos().map { TableCheckBoxModel(it.tableName, false) }

    override val root = vbox {
        flowpane {
            models.forEach { model ->
                checkbox {
                    textProperty().bind(model.tableName)
                    selectedProperty().bind(model.selected)
                }
            }
        }

        buttonbar {
            button("select all") { action { models.forEach { it.selected.set(true) } } }
            button("unSelect all") { action { models.forEach { it.selected.set(false) } } }
            button("save") {
                action {
                    //begin import
                    val data = models.filter { it.selected.value == true }
                        .map { it.tableName.value }.let { TableMetadataHandler.import(it) }
                    if (data.isNotEmpty()) { // not empty,then clear
                        tableMetadataController.tableInfos.clear()
                        tableMetadataController.tableInfos.addAll(data)
                    }
                }

            }
        }
    }
}

class TableCheckBoxModel(tableName: String, selected: Boolean) {

    val tableName = SimpleStringProperty(tableName)
    val selected = SimpleBooleanProperty(selected)
}