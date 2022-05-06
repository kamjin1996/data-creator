package com.kamjin.data.padding.controller

import com.kamjin.data.padding.data.*
import com.kamjin.data.padding.model.*
import com.kamjin.data.padding.view.*
import javafx.collections.*
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

    /**
     * column expression saved cache
     * key: table.column
     */
    private var columnExpressionMap = mutableMapOf<String, ColumnRuleExpression>()

    private val tableMetadataController = find<TableMetadataController>()

    init {
        tableMetadataController.queryAllTableInfos().flatMap { it.columnMetadatas }
            .fold(columnExpressionMap) { acc, c ->
                createExpressionsByRoleType(c)?.let { acc.put(c.key, it) }
                acc
            }
    }

    //be referenced the other column
    //key: table.column
    //value is generated values
    private val beReferenceColumnValues = mutableMapOf<String, MutableList<Any?>>()

    //be gen sql
    private var currentSql: StringBuilder = StringBuilder(String.EMPTY)

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
            val useColumnMetadata =
                tableMetadata.columnMetadatas.filterNot { it.selectRule == null || it.selectRule == ColumnConfigRoleEnum.doNoting.name }

            if (useColumnMetadata.isEmpty()) continue //the table not have config

            val sqls = (1..1000).map {
                """insert into ${tableMetadata.name}( ${
                    useColumnMetadata.map { it.name }.joinToString(",")
                } ) values ( ${
                    useColumnMetadata.map {
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

    private fun obtainOrderTableMetadata(tableMetadata: ObservableList<TableMetadata>): List<TableMetadata> {
        return tableMetadata.distinct().sorted().apply {
            this.forEach {
                it.columnMetadatas.sort()
            }
        }
    }
}
