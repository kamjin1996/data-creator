package com.kamjin.data.padding.model

import com.kamjin.data.padding.controller.*
import tornadofx.FX.Companion.find

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/04/28
 */
interface ColumnRuleExpression {

    /**
     * exec
     */
    fun exec(): String
}

interface ValueFilter {

    /**
     * filter lastResult
     */
    fun filter(lastResult: String?): String
}

class InnerFunExpression(
    var funInstance: ((String?) -> String),
    var param: String?
) : ColumnRuleExpression {

    override fun exec(): String {
        return funInstance(param)
    }
}

class NothingExpression : ColumnRuleExpression {
    override fun exec(): String {
        return ""
    }
}

class OtherTableColumnExpression(var columnKey: String) : ColumnRuleExpression {

    val sqlParseController = find<SqlParseController>()

    override fun exec(): String {
        val otherColumnRuleExpressions = sqlParseController.queryColumnRuleExpressionsByKey(columnKey)
        val iterator = otherColumnRuleExpressions?.iterator()

        //result
        var result: String? = null
        while (iterator?.hasNext() == true) {
            val expression = iterator.next()
            if (expression is ValueFilter) {
                result = expression.filter(result)
            } else {
                result = expression.exec()
            }
        }
        return result ?: ""
    }

}

class JsCodeExpression(jsFun: String) : ColumnRuleExpression, ValueFilter {
    override fun exec(): String {
        return filter(null)
    }

    override fun filter(lastResult: String?): String {

        TODO("exec js code ,param is lastResult")
        return ""
    }
}

class SqlCodeExpression(sql: String) : ColumnRuleExpression, ValueFilter {
    override fun exec(): String {
        return filter(null)
    }

    override fun filter(lastResult: String?): String {
        TODO("exec js code ,param is lastResult")
        return ""
    }
}