package com.kamjin.data.padding.model

import com.kamjin.data.padding.view.*
import javafx.beans.property.*
import javafx.collections.*
import tornadofx.*
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

enum class DbColumnType {
    bigint,
    varchar,
    int,
    tinyint,
    datetime,
}

class ColumnMetadata(
    tableName: String?,
    name: String,
    type: String,
    length: Int,
    comment: String?,
) {

    val tableNameProperty = SimpleStringProperty(tableName)
    var tableName by tableNameProperty

    val commentProperty = SimpleStringProperty(comment)
    var comment by commentProperty

    val lengthProperty = SimpleIntegerProperty(length)
    var length by lengthProperty

    val typeProperty = SimpleStringProperty(type)
    var type by typeProperty

    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    val selectRuleProperty = SimpleStringProperty()
    var selectRule by selectRuleProperty

    val ruleFunNameProperty = SimpleStringProperty()
    var ruleFunName by ruleFunNameProperty

    val ruleFunProperty = SimpleObjectProperty<(String?) -> String>()
    var ruleFun by ruleFunProperty

    val ruleFunParamProperty = SimpleStringProperty()
    var ruleFunParam by ruleFunParamProperty

    val otherTableColumnKeyProperty = SimpleStringProperty()
    var otherTableColumnKey by otherTableColumnKeyProperty

    val customScriptFiltersProperty = SimpleListProperty<Pair<ScriptType, String>>(observableListOf())
    var customScriptFilters by customScriptFiltersProperty

}

class TableMetadata(name: String, comment: String?, columnMetadatas: ObservableList<ColumnMetadata>) {
    val commentProperty = SimpleStringProperty(comment)
    var comment by commentProperty

    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    val columnMetadatasProperty = SimpleObjectProperty<ObservableList<ColumnMetadata>>(columnMetadatas)
    var columnMetadatas by columnMetadatasProperty

}
