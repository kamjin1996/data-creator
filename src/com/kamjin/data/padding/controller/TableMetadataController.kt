package com.kamjin.data.padding.controller

import com.kamjin.data.padding.model.*
import javafx.collections.*
import tornadofx.*

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
    val tableInfos: ObservableList<TableMetadata> = listOf(
        TableMetadata(
            "employee", "员工", listOf(
                ColumnMetadata("employee", "id", DbColumnType.bigint.name, 20, "主键ID"),
                ColumnMetadata("employee", "name", DbColumnType.varchar.name, 255, "名称"),
                ColumnMetadata("employee", "mobile", DbColumnType.varchar.name, 11, "手机号"),
                ColumnMetadata("employee", "create_time", DbColumnType.datetime.name, 0, "创建时间"),
            ).toObservable()
        ),
        TableMetadata(
            "device", "设备", listOf(
                ColumnMetadata("device", "id", DbColumnType.bigint.name, 20, "主键ID"),
                ColumnMetadata("device", "deviceCode", DbColumnType.varchar.name, 255, "设备号"),
                ColumnMetadata("device", "ip", DbColumnType.varchar.name, 255, "IP"),
                ColumnMetadata("device", "create_time", DbColumnType.datetime.name, 0, "创建时间"),
            ).toObservable()
        )
    ).toObservable()

    /**
     * 查询所有表元数据信息
     *
     */
    fun queryAllTableInfos(): ObservableList<TableMetadata> {
        return tableInfos
    }
}
