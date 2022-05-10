package com.kamjin.data.padding.model

import cn.hutool.db.*
import cn.hutool.script.*
import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.data.*
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

    fun hasNext(): Boolean {
        return Objects.nonNull(next)
    }

    abstract fun filter(request: T?): Any?
}

class InnerFunExpression(
    var funInstance: InnerFun?,
    var param: String?
) : ColumnRuleExpression {

    private val maker = Maker() //the column only one maker

    override fun exec(): String {
        return funInstance?.invoke(maker, param) ?: String.EMPTY
    }
}

class NothingExpression : ColumnRuleExpression, ValueFilter, AbstractChainFilter<Any>() {
    override fun exec(): String {
        return String.EMPTY
    }

    override fun filter(lastResult: Any?): Any {
        return String.EMPTY
    }
}

class OtherTableColumnExpression(needUniqueObtain: Boolean, var columnKey: String) : ColumnRuleExpression {

    private val values by lazy { queryNeedReferenceValuesByColumnKey(columnKey)?.toMutableList() }

    private val obtainFun: () -> String by lazy {
        if (needUniqueObtain) {
            {
                val random = values?.randomOrNull()
                if (random != null) {
                    values?.remove(random)
                    random.toString()
                } else {
                    ""
                }
            }
        } else {
            { values?.random().toString() }
        }
    }

    override fun exec(): String {
        return obtainFun.invoke()
    }
}

class JsCodeExpression(var jsFunContext: String) :
    ColumnRuleExpression,
    ValueFilter, AbstractChainFilter<Any>() {

    private val contextFun = lazy {
        { lastResult: Any? ->
            ScriptUtil.compile(
                """
            function a(result){
            $jsFunContext
            }
           a($lastResult)
        """.trimIndent()
            ) //compile the script context
        }
    }

    override fun exec(): String {
        return filter(null).toString()
    }

    override fun filter(lastResult: Any?): Any? {
        return contextFun.value.invoke(lastResult)
    }
}

class SqlCodeExpression(var sql: String) :
    ColumnRuleExpression, ValueFilter, AbstractChainFilter<Any>() {

    private val dataSource: DataSource?
        get() = find<DataSourceConfigView>().obtainDataSourceWithTip()

    override fun exec(): String {
        return filter(null).toString()
    }

    override fun filter(lastResult: Any?): Any {
        return DbUtil.use(dataSource).queryString(sql, lastResult)
    }
}

/**
 * create expressions role type
 */
fun createExpressionsByRoleType(model: ColumnMetadata): ColumnRuleExpression? {
    return when (ColumnConfigRoleEnum.valueOf(model.selectRule ?: return null)) {
        ColumnConfigRoleEnum.doNoting -> NothingExpression()
        ColumnConfigRoleEnum.withOtherTableColumn -> OtherTableColumnExpression(
            model.otherTableColumnValueObtainUnique,
            model.otherTableColumnKey
        )
        ColumnConfigRoleEnum.innerFun -> InnerFunExpression(
            model.ruleFun ?: obtainInnerFunMap()[model.ruleFunName],
            model.ruleFunParam
        )
        ColumnConfigRoleEnum.custom -> model.customScriptFilters.map {
            val filters = when (it.type!!) {
                ScriptType.UNKNOW -> NothingExpression()
                ScriptType.SQL -> SqlCodeExpression(it.script)
                ScriptType.JS -> JsCodeExpression(it.script)
            }
            return@map filters
        }.apply {
            //set all next filter
            val iterator = this.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (iterator.hasNext()) {
                    next.setNext(iterator.next())
                }
            }
        }.first()
    }
}

fun obtainInnerFunMap() = mapOf<String, InnerFun>(
    "autoId" to { maker, param -> maker.autoId(param).toString() },
    "formatAutoId" to { maker, param -> maker.formatAutoId(param) },
    "choice" to { maker, param -> maker.choice(param) },
    "order" to { maker, param -> maker.order(param).toString() },
    "time" to { maker, param -> maker.time(param).toString() },
    "date" to { maker, param -> maker.date(param).toString() },
)

object ColumnRuleExpressionHandler {

    /**
     * column expression saved cache
     * key: table.column
     */
    private var columnExpressionMap = mutableMapOf<String, ColumnRuleExpression>()

    fun query(key: String): ColumnRuleExpression? {
        return columnExpressionMap[key]
    }

    /**
     * put for expressions
     */
    fun put(key: String, e: ColumnRuleExpression) {
        columnExpressionMap[key] = e
    }
}

fun putExpression(key: String, e: ColumnRuleExpression) = ColumnRuleExpressionHandler.put(key, e)

fun queryExpression(key: String) = ColumnRuleExpressionHandler.query(key)


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