package com.kamjin.data.padding.model

import com.kamjin.data.padding.data.*
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
    tableName: String? = null,
    name: String = String.EMPTY,
    type: String = String.EMPTY,
    length: Int? = null,
    comment: String? = null,
) : Comparable<ColumnMetadata>, JsonModelAuto {

    val tableNameProperty = SimpleStringProperty(tableName)
    var tableName by tableNameProperty

    val commentProperty = SimpleStringProperty(comment)
    var comment by commentProperty

    val lengthProperty = SimpleIntegerProperty(length ?: 0)
    var length by lengthProperty

    val typeProperty = SimpleStringProperty(type)
    var type by typeProperty

    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    val selectRuleProperty = SimpleStringProperty()
    var selectRule by selectRuleProperty

    val ruleFunNameProperty = SimpleStringProperty()
    var ruleFunName by ruleFunNameProperty

    val ruleFunProperty = SimpleObjectProperty<InnerFun>()
    var ruleFun by ruleFunProperty

    val ruleFunParamProperty = SimpleStringProperty()
    var ruleFunParam by ruleFunParamProperty

    val otherTableColumnKeyProperty = SimpleStringProperty()
    var otherTableColumnKey by otherTableColumnKeyProperty

    val customScriptFiltersProperty = SimpleListProperty<ScriptInputItem>(observableListOf())
    var customScriptFilters by customScriptFiltersProperty

    override fun compareTo(other: ColumnMetadata): Int {
        if (this.needReferenceOtherColumn() && this.otherTableColumnKey == other.key) {
            return -1
        } else {
            return 1
        }
    }

}

fun ColumnMetadata.needReferenceOtherColumn(): Boolean {
    return this.selectRule == ColumnConfigRoleEnum.withOtherTableColumn.name && this.otherTableColumnKey != null
}

val ColumnMetadata.key: String
    get() = this.tableName + "." + this.name

class TableMetadata(
    name: String = String.EMPTY,
    comment: String? = null,
    columnMetadata: ObservableList<ColumnMetadata> = observableListOf()
) :
    Comparable<TableMetadata>, JsonModelAuto {

    val commentProperty = SimpleStringProperty(comment)
    var comment by commentProperty

    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    val recordCountProperty = SimpleIntegerProperty()
    var recordCount by recordCountProperty

    val columnMetadatasProperty = SimpleObjectProperty<ObservableList<ColumnMetadata>>(columnMetadata)
    var columnMetadatas by columnMetadatasProperty

    override fun compareTo(other: TableMetadata): Int {
        //needReferenceKeys
        val needReferenceKeys =
            this.columnMetadatas.filter { it.needReferenceOtherColumn() }.map { it.otherTableColumnKey }

        //otherTableKeys
        val otherKeys = other.columnMetadatas.map { it.key }

        //cul intersect list
        if ((otherKeys intersect needReferenceKeys).isNotEmpty()) {
            return 1
        } else {
            return -1
        }
    }


}
