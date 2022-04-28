package com.kamjin.data.padding.view;

import cn.hutool.core.io.*
import com.kamjin.data.padding.controller.*
import javafx.application.*
import javafx.stage.*
import tornadofx.*
import java.nio.charset.*
import kotlin.concurrent.*

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

    override val root = vbox {
        //1个菜单项 包含导入导出
        //导入为导入某库的建表sql
        //导出为导出插入数据sql
        menubar {
            menu("文件操作") {
                item("导入")
                item("导出").action {
                    //covert to sql
                    //progress show
                    progressindicator {
                        thread {
                            for (i in 1..100) {
                                Platform.runLater { progress = i.toDouble() / 100.0 }
                                TODO("covert")
                            }
                        }
                    }

                    sqlExport()
                }
            }
        }
    }

    fun sqlExport() {
        chooseFile(
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

