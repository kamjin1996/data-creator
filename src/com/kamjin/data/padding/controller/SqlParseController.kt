package com.kamjin.data.padding.controller

import com.kamjin.data.padding.model.*
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

    //column expression saved cache
    private var columnExpressionMap = mutableMapOf<String, List<ColumnRuleExpression>>()

    //be gen sql
    private var currentSql: StringBuilder = StringBuilder("")

    /**
     * query be gen sql
     */
    fun queryCurrentSqls(): String {
        return currentSql.toString()
    }

    fun queryColumnRuleExpressionsByKey(key:String): List<ColumnRuleExpression>? {
        return columnExpressionMap[key]
    }

    /**
     * put for expressions
     */
    fun putColumnRuleExpressions(key: String, es: List<ColumnRuleExpression>) {
        columnExpressionMap.put(key, es)
    }

    /**
     * append sql
     */
    private fun appendSql(sql: String) {
        if (currentSql.toString().isNotBlank()) {
            currentSql.append("\n")
        }
        currentSql.append(sql)
    }

}