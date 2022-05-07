package com.kamjin.data.padding.view;

import cn.hutool.core.io.*
import com.kamjin.data.padding.controller.*
import javafx.stage.*
import tornadofx.*
import java.io.*
import java.nio.charset.*

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/04/27
 */
class Main : View("Main") {
    override val root = borderpane {
        top<TopView>()
        left<LeftView>()
    }
}

class LeftView : View() {
    override val root = borderpane {
        hbox(10) {
            left<TableMetadataView>()
            right<ColumnRuleConfigEditor>()
        }
    }
}

class TopView : View() {

    val sqlParseController = find<SqlParseController>()

    override val root = hbox(5) {
        //1个菜单项 包含导入导出
        //导入为导入某库的建表sql
        //导出为导出插入数据sql
        menubar {
            menu("元数据") {
                item("导入元数据").action {
                    confirm(header = "导入确认", content = "确认导入新的元数据吗？当前配置将被覆盖，如需要请及时保存") {
                        openInternalWindow<TableMetadataImportView>()
                    }
                }

                item("生成SQL导出").action {
                    //covert to sql
                    //progress show
                    sqlParseController.convertModelToSql()
                    information(header = "转为SQL", content = "转换成功，即将导出..")

                    sqlExport()
                }
            }
        }

        menubar {
            menu("配置") {
                item("导入配置").action {
                    confirm(header = "导入确认", content = "确认导入配置吗？当前配置将被覆盖，如需要请及时保存") {
                        configImport()
                    }
                }

                item("当前配置导出") {
                    action {
                        configExport()
                    }
                }

            }
        }

        menubar {
            menu("数据源") {
                item("当前信息") {
                    action {
                        find<DataSourceStatusView>().openWindow()
                    }
                }
                item("新建连接") {
                    action {
                        openInternalWindow<DataSourceConfigView>()
                    }
                }
            }
        }
    }

    fun configImport() {
        chooseDirectory(
            initialDirectory = File(System.getProperty("user.home")),
            title = "选择配置所在文件夹路径",
        ).let {
            if (it?.isDirectory != true) {
                warning(header = "导入文件", content = "当前选择错误，请正确选择文件夹")
                return
            }
            val configFiles = it.listFiles().filter { it.extension != CONFIG_EXTENTION }.toTypedArray()
            if (configFiles.isEmpty()) {
                warning("${CONFIG_EXTENTION} 格式配置文件不存在")
                return
            }
            find<TableMetadataController>().loadLocalCache(configFiles)
        }
    }

    fun configExport() {
        chooseDirectory(
            initialDirectory = File(System.getProperty("user.home")),
            title = "选择保存配置的路径",
        ).let {
            if (it?.isDirectory != true) {
                warning(header = "导入文件", content = "当前选择错误，请正确选择文件夹")
                return
            }
            val newDir = File(it.path + "/data-creator-" + System.currentTimeMillis())
            newDir.mkdirs()
            find<TableMetadataController>().saveLocalCache(newDir)
        }
    }

    fun sqlExport() {
        chooseFile(
            initialDirectory = File(System.getProperty("user.home")),
            title = "export sql",
            mode = FileChooserMode.Save,
            filters = arrayOf(FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"))
        ).let {
            if (it.isEmpty()) {
                return
            }
            val sqls = sqlParseController.queryCurrentSqls()
            it[0].let { FileUtil.writeString(sqls, it, StandardCharsets.UTF_8.name()) }
        }
    }
}

