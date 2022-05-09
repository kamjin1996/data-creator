package com.kamjin.data.padding.data

import cn.hutool.db.*
import com.kamjin.data.padding.controller.*
import com.kamjin.data.padding.model.*
import com.kamjin.data.padding.view.*
import com.mysql.jdbc.exceptions.*
import javafx.beans.property.*
import tornadofx.*
import java.util.regex.*
import tornadofx.getValue
import tornadofx.setValue
import java.lang.Exception

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/05/06
 */
object TableMetadataHandler {

    val tableMetadataController = find<TableMetadataController>()

    fun import(includeTables: List<String>): List<TableMetadata> {
        //query all table
        val useTableBaseInfos = tableMetadataController.queryTableBaseInfos().let {
            if (includeTables.isNotEmpty()) {
                it.filter { table -> includeTables.contains(table.tableName) }
            } else {
                it //else all
            }
        }

        //parse
        return useTableBaseInfos.map { tableInfo ->
            val dataSource = find<DataSourceConfigView>().obtainDataSourceWithTip()
            TableMetadata().apply {
                this.name = tableInfo.tableName
                this.comment = tableInfo.tableComment
                val columnInfos = Db.use(dataSource).query("show full fields from ${tableInfo.tableName};")
                this.columnMetadatas = columnInfos.map { columnInfo ->
                    val length = deduceColumnValueLength(columnInfo.getStr("Type"))
                    ColumnMetadata(
                        tableName = tableInfo.tableName,
                        name = columnInfo.getStr("Field"),
                        type = columnInfo.getStr("Type").substringBefore("("),
                        length = length,
                        comment = columnInfo.getStr("Comment")
                    )
                }.toObservable()
            }
        }
    }

    private fun deduceColumnValueLength(originalDbType: String): Int? {
        val mayLength = originalDbType.substringAfter("(").substringBefore(")")
        try {
            return mayLength.toInt()
        } catch (e: Exception) {
        }
        return null
    }
}

object ConfigHandler {

    fun import(configFileDirPath: String) {

    }

    fun export(newConfigFileDirPath: String) {}
}

//fun main() {
//    val dataSource = SimpleDataSource(
//        "jdbc:mysql://192.168.1.103:3306/xijing_wisdom_java?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useOldAliasMetadataBehavior=true&allowMultiQueries=true&serverTimezone=GMT%2B8&useSSL=false",
//        "root",
//        "123456"
//    )
//
//    val dbInfo = parseDbInfoFrom(dataSource.url)
//
//    println(
//        Db.use(
//            dataSource
//        ).query("select table_comment,table_name from information_schema.tables where table_schema = '${dataSource.connection.catalog}'")
//    )
//}

fun parseDbInfoFrom(url: String): DbInfo {
    val p =
        Pattern.compile("jdbc:(?<db>\\w+):.*((//)|@)(?<host>.+):(?<port>\\d+)(/|(;DatabaseName=)|:)(?<dbName>\\w+)\\??.*");
    val m = p.matcher(url);
    if (m.find()) {
        return DbInfo(m.group("db"), m.group("host"), m.group("port"), m.group("dbName"))
    }
    throw MySQLSyntaxErrorException("DbInfo parse error from:[$url]")
}

data class DbInfo(val db: String, val host: String, val port: String, val dbName: String)

class TableBaseInfo(tableName: String, tableComment: String) {
    val tableCommentProperty = SimpleStringProperty(tableComment)
    var tableComment by tableCommentProperty
    val tableNameProperty = SimpleStringProperty(tableName)
    var tableName by tableNameProperty
}

fun main() {
//    println("varchar(10)".substringAfter("(").substringBefore(")"))
//    println("datetime".substringAfter("(").substringBefore(")"))
        println("varchar(10)".substringBefore("("))
    println("datetime".substringBefore("("))
}
