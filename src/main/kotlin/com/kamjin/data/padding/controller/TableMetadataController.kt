package com.kamjin.data.padding.controller

import cn.hutool.db.*
import com.kamjin.data.padding.data.*
import com.kamjin.data.padding.model.*
import com.kamjin.data.padding.view.*
import javafx.collections.*
import tornadofx.*
import java.io.*
import java.nio.file.*
import javax.json.*

val CONFIG_EXTENTION = ".jdat"

/**
 * <p>
 * 表元数据控制器
 * </p>
 *
 * @author kam
 * @since 2022/04/27
 */
class TableMetadataController : Controller() {

    /**
     * TODO 数据源
     * 从字段解析而来
     */
//    var tableInfos: ObservableList<TableMetadata> = listOf(
//        TableMetadata(
//            "employee", "员工", listOf(
//                ColumnMetadata("employee", "id", DbColumnType.bigint.name, 20, "主键ID"),
//                ColumnMetadata("employee", "name", DbColumnType.varchar.name, 255, "名称"),
//                ColumnMetadata("employee", "mobile", DbColumnType.varchar.name, 11, "手机号"),
//                ColumnMetadata("employee", "create_time", DbColumnType.datetime.name, 0, "创建时间"),
//            ).toObservable()
//        ),
//        TableMetadata(
//            "device", "设备", listOf(
//                ColumnMetadata("device", "id", DbColumnType.bigint.name, 20, "主键ID"),
//                ColumnMetadata("device", "deviceCode", DbColumnType.varchar.name, 255, "设备号"),
//                ColumnMetadata("device", "ip", DbColumnType.varchar.name, 255, "IP"),
//                ColumnMetadata("device", "create_time", DbColumnType.datetime.name, 0, "创建时间"),
//            ).toObservable()
//        )
//    ).toObservable()

    var tableInfos: ObservableList<TableMetadata> = observableListOf()

    val key = "tableInfos"

    var configFiles: Array<File> = arrayOf()

    val parentFileDir = File("./$key")
        .apply {
            if (!this.exists()) {
                this.mkdirs()
            } else {
                if (!this.isDirectory) {
                    throw FileAlreadyExistsException("the path:$this not dir")
                }

                configFiles = this.listFiles()
            }
        }

    /**
     * 查询所有表元数据信息
     *
     */
    fun queryAllTableInfos(): ObservableList<TableMetadata> {
        return tableInfos
    }

    fun queryTableBaseInfos(): List<TableBaseInfo> {
        val dataSource = find<DataSourceConfigView>().obtainDataSourceWithTip() ?: return mutableListOf()
        return Db.use(dataSource)
            .query("select table_comment,table_name from information_schema.tables where table_schema = '${dataSource.connection?.catalog}'")
            .map { TableBaseInfo(it.getStr("table_name"), it.getStr("table_comment")) }
    }

    fun loadLocalCache(configFiles: Array<File>) {
        try {
            configFiles.forEach {
                val model = loadJsonModel<TableMetadata>(it.toPath()) // !!! don't use file.path,the type is String
                tableInfos.add(model)
            }
        } catch (e: JsonException) {
            log.warning(e.printStackTrace().toString())
        }
    }

    fun saveLocalCache(dir: File = parentFileDir) {
        if (!dir.exists()) warning("dir: $dir 该路径不存在")
        tableInfos.forEach {
            val newFile = File(dir.path + "//" + it.name + CONFIG_EXTENTION).apply {
                if (!this.exists()) {
                    this.createNewFile()
                }
            }
            it.save(newFile.toPath())
            log.info("save---$newFile")
        }
    }

    fun clearLocalCache(dir: File = parentFileDir) {
        dir.listFiles().forEach { it.delete() }
    }

    init {
        loadLocalCache(configFiles)
    }
}
