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
    private var currentSql: StringBuilder = StringBuilder(String.EMPTY)

    /**
     * query be gen sql
     */
    fun queryCurrentSqls(): String {
        return currentSql.toString()
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
                            DbColumnType.datetime, DbColumnType.varchar, DbColumnType.tinyint -> "'$result'"
                        }
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
