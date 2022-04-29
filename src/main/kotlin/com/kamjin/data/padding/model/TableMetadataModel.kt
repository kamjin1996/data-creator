package main.kotlin.com.kamjin.data.padding.model

import javafx.beans.property.*
import javafx.collections.*
import main.kotlin.com.kamjin.data.padding.data.*
import main.kotlin.com.kamjin.data.padding.view.*
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
) : Comparable<ColumnMetadata> {

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

    val ruleFunProperty = SimpleObjectProperty<(Maker, String?) -> String>()
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

class TableMetadata(name: String, comment: String?, columnMetadatas: ObservableList<ColumnMetadata>) :
    Comparable<TableMetadata> {
    val commentProperty = SimpleStringProperty(comment)
    var comment by commentProperty

    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    val columnMetadatasProperty = SimpleObjectProperty<ObservableList<ColumnMetadata>>(columnMetadatas)
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
