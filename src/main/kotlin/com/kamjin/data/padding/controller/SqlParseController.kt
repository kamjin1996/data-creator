package main.kotlin.com.kamjin.data.padding.controller

import javafx.collections.*
import main.kotlin.com.kamjin.data.padding.model.*
import tornadofx.*
import java.lang.StringBuilder

/**
 *
 *
 *
 * sql parse controller
 *
 * @author kam
 * @since 2022/04/28
 */
class SqlParseController : Controller() {

    private val tableMetadataController = find<TableMetadataController>()

    /**
     * column expression saved cache
     * key: table.column
     */
    private var columnExpressionMap = mutableMapOf<String, ColumnRuleExpression>()

    //be referenced the other column
    //key: table.column
    //value is generated values
    private val beReferenceColumnValues = mutableMapOf<String, MutableList<Any?>>()

    //be gen sql
    private var currentSql: StringBuilder = StringBuilder("")

    /**
     * query be gen sql
     */
    fun queryCurrentSqls(): String {
        return currentSql.toString()
    }

    fun queryColumnRuleExpressionByKey(key: String): ColumnRuleExpression? {
        return columnExpressionMap[key]
    }

    /**
     * put for expressions
     */
    fun putColumnRuleExpression(key: String, e: ColumnRuleExpression) {
        columnExpressionMap[key] = e
    }

    /**
     * covertModelToSql
     */
    fun convertModelToSql() {
        //obtain table metadata
        val tableInfos = this.tableMetadataController.queryAllTableInfos()

        //obtain be referenced column
        val needReferenceOtherTableColumnKeyList = tableInfos.flatMap {
            it.columnMetadatas.filter { metadata -> metadata.needReferenceOtherColumn() }
                .map { metadata -> metadata.otherTableColumnKey }
        }

        //order
        val ordered = obtainOrderTableMetadata(tableInfos)

        //TODO 100 count

        //append sql and exec expressions by column metadata
        for (tableMetadata in ordered) {
            val sqls = (1..1000).map {
                """insert into ${tableMetadata.name}( ${
                    tableMetadata.columnMetadatas.map { it.name }.joinToString(",")
                } ) values ( ${
                    tableMetadata.columnMetadatas.map {
                        val key = it.key
                        val expression = columnExpressionMap[key]
                        val result =
                            if (expression is AbstractChainFilter<*>) expression.chainFilter(null) else expression?.exec()

                        //if be referenced add to valuepool
                        if (needReferenceOtherTableColumnKeyList.contains(key)) {
                            beReferenceColumnValues.computeIfPresent(key) { _, values ->
                                values.add(result)
                                values
                            }
                        }
                        result
                    }.joinToString(",")
                } );"""
            }
            this.append(sqls)
        }
    }

    /**
     * append sqlContents
     */
    private fun append(texts: List<String>) {
        texts.forEach { this.append(it) }
    }

    /**
     * append to sqlContent
     */
    private fun append(text: String) {
        if (currentSql.toString().isNotBlank()) {
            currentSql.append("\n")
        }
        currentSql.append(text)
    }

    fun queryNeedReferenceValuesByColumnKey(columnKey: String): List<Any?>? {
        return beReferenceColumnValues[columnKey]
    }

    private fun obtainOrderTableMetadata(tableMetadatas: ObservableList<TableMetadata>): List<TableMetadata> {
        return tableMetadatas.distinct().sorted().apply {
            this.forEach {
                it.columnMetadatas.sort()
            }
        }
    }
}