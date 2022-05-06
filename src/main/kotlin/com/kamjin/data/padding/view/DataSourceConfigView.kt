package com.kamjin.data.padding.view

import cn.hutool.db.ds.simple.*
import com.kamjin.data.padding.data.*
import javafx.beans.property.*
import javafx.scene.control.*
import tornadofx.*
import javax.sql.*
import tornadofx.getValue
import tornadofx.setValue

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/04/29
 */
class DataSourceConfigView() : View() {

    private val _localCacheUrlKey = "data.creator.dbConfig.url"
    private val _localCacheUserKey = "data.creator.dbConfig.user"
    private val _localCachePasswordKey = "data.creator.dbConfig.pass"

    private val _alertHeader = "Db Config"

    private var _dataSource: DataSource? = null

    private var model = DataSourceViewModel()

    override val root = form {
        hbox(20) {
            fieldset("Database Config") {
                vbox {
                    field("Url: ") { textfield {}.bind(model.urlProperty) }
                    field("User: ") { textfield { }.bind(model.userProperty) }
                    field("Password: ") { passwordfield { }.bind(model.passwordProperty) }
                }
            }
        }

        buttonbar {
            button("Reset").action {
                model.clear()
            }

            button("TestConnect").action {
                val simpleDataSource = SimpleDataSource(model.url, model.user, model.password)
                val valid = simpleDataSource.connection.isValid(3000)
                alert(type = Alert.AlertType.INFORMATION, _alertHeader, content = valid.toString())
            }

            button("Save").action {
                _dataSource = SimpleDataSource(model.url, model.user, model.password)
                val valid = _dataSource?.connection?.isValid(3000)
                if (valid == false) {
                    alert(type = Alert.AlertType.INFORMATION, _alertHeader, content = false.toString())
                } else {
                    alert(type = Alert.AlertType.INFORMATION, _alertHeader, content = "save success")
                }

                //save local cache
                saveLocalCache()
            }
        }
    }

    fun obtainDataSource(): DataSource? {
        return _dataSource
    }

    fun obtainDataSourceWithTip(): DataSource? {
        return _dataSource.also {
            fun checkMe(dataSource: DataSource?) {
                if (dataSource == null || !dataSource.connection.isValid(5000)) {
                    alert(
                        Alert.AlertType.WARNING,
                        header = _alertHeader,
                        content = "connect error,please config datasource on setting"
                    )
                }
            }
            checkMe(it)
        }
    }

    private fun saveLocalCache() {
        preferences(this::class.simpleName) {
            put(_localCacheUrlKey, model.url)
            put(_localCacheUserKey, model.user)
            put(_localCachePasswordKey, model.password)
            log.info("local database cache save success")
        }
    }

    private fun loadLocalCache() {
        //try connect last dataSource
        preferences(this::class.simpleName) {
            model.url = get(_localCacheUrlKey, "")
            model.user = get(_localCacheUserKey, "")
            model.password = get(_localCachePasswordKey, "")
            log.info("get local database cache, for model")
            try {
                _dataSource = SimpleDataSource(model.url, model.user, model.password)
                log.info("connect success")
            } catch (e: Exception) {
                log.warning("local database cache to connect db error.. ${e.printStackTrace()}")
            }
        }
    }

    init {
        loadLocalCache()
    }

}

class DataSourceStatusView : View() {

    private val _dataSource by find<DataSourceConfigView>().obtainDataSource().toProperty()

    override val root = vbox(20) {
        text("Connect Status: ${_dataSource != null}")
        text("Schema: ${_dataSource?.connection?.catalog}")
        text("Url: ${_dataSource?.connection?.metaData?.url}")
        text("User: ${_dataSource?.connection?.metaData?.userName}")
    }
}

class DataSourceViewModel {

    val urlProperty = SimpleStringProperty()
    var url by urlProperty

    val userProperty = SimpleStringProperty()
    var user by userProperty

    val passwordProperty = SimpleStringProperty()
    var password by passwordProperty

    fun clear() {
        this.urlProperty.set(String.EMPTY)
        this.userProperty.set(String.EMPTY)
        this.passwordProperty.set(String.EMPTY)
    }
}