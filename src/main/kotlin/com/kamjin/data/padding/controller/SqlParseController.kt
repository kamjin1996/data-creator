package com.kamjin.data.padding.controller

import com.kamjin.data.padding.data.*
import com.kamjin.data.padding.model.*
import com.kamjin.data.padding.view.*
import javafx.collections.*
import tornadofx.*
import java.lang.StringBuilder

//be referenced the other column
//key: table.column
//value is generated values
private val beReferenceColumnValues = mutableMapOf<String, MutableList<Any?>>()

fun queryNeedReferenceValuesByColumnKey(columnKey: String): List<Any?>? {
    return beReferenceColumnValues[columnKey]
}

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

    //be gen sql
    private var currentCreateSql: StringBuilder = StringBuilder(String.EMPTY)
    private var currentTruncateSql: StringBuilder = StringBuilder(String.EMPTY)

    fun queryCurrentCreateSqls() = currentCreateSql.toString()

    fun queryCurrentTruncateSqls() = currentTruncateSql.toString()

    fun convertTheTruncateSql() {
        //obtain table metadata
        val tableInfos = this.tableMetadataController.queryAllTableInfos()

        tableInfos.forEach {
            append(currentTruncateSql, """truncate ${it.name};""")
        }
    }

    /**
     * covertModelToCreateSql
     */
    fun convertModelToCreateSql() {
        //obtain table metadata
        val tableInfos = this.tableMetadataController.queryAllTableInfos()

        //obtain be referenced column
        val needReferenceOtherTableColumnKeyList = tableInfos.flatMap {
            it.columnMetadatas.filter { metadata -> metadata.needReferenceOtherColumn() }
                .map { metadata -> metadata.otherTableColumnKey }
        }

        //order
        val ordered = obtainOrderTableMetadata(tableInfos)

        //append sql and exec expressions by column metadata
        for (tableMetadata in ordered) {
            val useColumnMetadata =
                tableMetadata.columnMetadatas.filterNot { it.selectRule == null || it.selectRule == ColumnConfigRoleEnum.doNoting.name }

            if (useColumnMetadata.isEmpty()) continue //the table not have config

            val sqls = (1..tableMetadata.recordCount).map {
                """insert into ${tableMetadata.name}( ${
                    useColumnMetadata.map { it.name }.joinToString(",")
                } ) values ( ${
                    useColumnMetadata.map RESULT@{
                        val key = it.key
                        val expression = queryExpression(key)
                        val result =
                            if (expression is AbstractChainFilter<*>) expression.chainFilter(null) else expression?.exec()

                        //if be referenced add to valuePool
                        if (needReferenceOtherTableColumnKeyList.contains(key)) {
                            val v = beReferenceColumnValues[key]
                            if (v == null) {
                                beReferenceColumnValues[key] = mutableListOf()
                            }
                            beReferenceColumnValues[key]?.add(result)
                        }

                        return@RESULT when (DbColumnType.valueOf(it.type)) {
                            DbColumnType.int, DbColumnType.bigint -> result
                            DbColumnType.datetime, DbColumnType.date, DbColumnType.varchar, DbColumnType.tinyint -> "'$result'"
                        }
                    }.joinToString(",")
                } );"""
            }
            this.append(currentCreateSql, sqls)
        }
    }

    /**
     * append sqlContents
     */
    private fun append(sqlSb: StringBuilder, texts: List<String>) {
        texts.forEach { this.append(sqlSb, it) }
    }

    /**
     * append to sqlContent
     */
    private fun append(sqlSb: StringBuilder, text: String) {
        if (sqlSb.toString().isNotBlank()) {
            sqlSb.append("\n")
        }
        sqlSb.append(text)
    }

    private fun obtainOrderTableMetadata(tableMetadata: ObservableList<TableMetadata>): List<TableMetadata> {
        return tableMetadata.distinct().sorted().apply {
            this.forEach {
                it.columnMetadatas.sort()
            }
        }
    }

    init {
        tableMetadataController.queryAllTableInfos().flatMap { it.columnMetadatas }.forEach {
            createExpressionsByRoleType(it)?.let { e -> putExpression(it.key, e) }
        }
    }
}
