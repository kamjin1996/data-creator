package com.kamjin.data.padding.model

import cn.hutool.db.*
import cn.hutool.script.*
import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.view.*
import javafx.application.*
import javafx.scene.control.*
import tornadofx.*
import tornadofx.FX.Companion.find
import java.util.*
import javax.sql.*

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
    fun filter(lastResult: Any?): Any?
}

abstract class AbstractChainFilter<in T : Any> {

    /**
     * 下一个filter
     */
    var next: AbstractChainFilter<*>? = null

    /**
     * 指定下一个filter
     *
     * @param nextFilter
     * @return
     */
    fun setNext(nextFilter: AbstractChainFilter<*>): AbstractChainFilter<*> {
        next = nextFilter
        return next!!
    }

    /**
     * 检查api是否可访问 递交
     *
     * @param request
     * @return
     */
    fun chainFilter(request: T?): Any? {
        val result = this.filter(request)
        return if (hasNext()) {
            next().chainFilter(request)
        } else result
    }

    /**
     * 获取下一个filter
     *
     * @return
     */
    protected fun next(): AbstractChainFilter<T> {
        @Suppress("UNCHECKED_CAST")
        return next as AbstractChainFilter<T>
    }

    /**
     * 是否有下一个
     *
     * @return
     */
    fun hasNext(): Boolean {
        return Objects.nonNull(next)
    }

    /**
     * filter
     *
     * @param request
     * @return
     */
    abstract fun filter(request: T?): Any?
}

class InnerFunExpression(
    var funInstance: InnerFun,
    var param: String?
) : ColumnRuleExpression {

    private val maker = Maker() //the column only one maker

    override fun exec(): String {
        return funInstance(maker, param)
    }
}

class NothingExpression : ColumnRuleExpression, ValueFilter, AbstractChainFilter<Any>() {
    override fun exec(): String {
        return ""
    }

    override fun filter(lastResult: Any?): Any {
        return ""
    }
}

class OtherTableColumnExpression(var columnKey: String) : ColumnRuleExpression {

    val sqlParseController = find<SqlParseController>()

    override fun exec(): String {
        //find values by key
        val values = sqlParseController.queryNeedReferenceValuesByColumnKey(columnKey)

        //random select by values
        return values?.random().toString()
    }
}

class JsCodeExpression(var jsFunContext: String) :
    ColumnRuleExpression,
    ValueFilter, AbstractChainFilter<Any>() {

    override fun exec(): String {
        return filter(null).toString()
    }

    override fun filter(lastResult: Any?): Any? {
        return ScriptUtil.eval(
            """
            function a(result){
            $jsFunContext
            }
           a($lastResult)
        """.trimIndent()
        )
    }
}

class SqlCodeExpression(var sql: String) :
    ColumnRuleExpression, ValueFilter, AbstractChainFilter<Any>() {

    private val dataSource: DataSource?
        get() = find<DataSourceConfigView>().dataSource

    override fun exec(): String {
        return filter(null).toString()
    }

    override fun filter(lastResult: Any?): Any {
        if (dataSource == null) {
            alert(
                type = Alert.AlertType.WARNING,
                header = "datasource config",
                content = "datasource config error,please correct config"
            )
            Platform.runLater {

            }

        }
        return DbUtil.use(dataSource).queryString(sql, lastResult)
    }
}

fun main() {

    val context = """
         var a = 1
            var b = 1
            return a+b+result
    """.trimIndent()

    val result = 10
    println(
        ScriptUtil.eval(
            """
            function a(result){
            $context
            }
           a($result)
        """.trimIndent()
        )
    )
}