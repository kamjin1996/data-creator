package com.kamjin.data.padding.view

import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.data.*
import javafx.beans.property.*
import javafx.geometry.*
import javafx.scene.control.*
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

    private val tableMetadataController = find<TableMetadataController>()

    private val models =
        observableListOf(tableMetadataController.queryTableBaseInfos().map { TableCheckBoxModel(it.tableName, false) })

    private val allCheckBox = observableListOf<CheckBox>()

    override val root = vbox {
        flowpane {
            paddingAll = insets.all
            vgap = 5.0
            hgap = 5.0

            alignment = Pos.CENTER_LEFT

            models.forEach { model ->
                checkbox {
                    textProperty().bind(model.tableName)

                    action {
                        model.selected.set(isSelected)
                    }
                }.let { allCheckBox.add(it) }
            }
        }

        buttonbar {
            button("select all") { action { allCheckBox.forEach { it.selectedProperty().set(true) } } }
            button("unSelect all") { action { allCheckBox.forEach { it.selectedProperty().set(false) } } }
            button("save") {
                action {
                    //begin import
                    val data = models.filter { it.selected.value == true }
                        .map { it.tableName.value }.let { TableMetadataHandler.import(it) }

                    if (data.isNotEmpty()) { // not empty,then clear
                        tableMetadataController.tableInfos.clear()
                        tableMetadataController.tableInfos.addAll(data)

                        //clear cache and save new cache
                        tableMetadataController.clearLocalCache()
                        tableMetadataController.saveLocalCache()
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