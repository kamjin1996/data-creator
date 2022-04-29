package main.kotlin.com.kamjin.data.padding.view

import cn.hutool.db.ds.simple.*
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

    var dataSource: DataSource? = null

    private var model = DataSourceViewModel()

    override val root = form {
        hbox(20) {
            fieldset("Database Config") {
                vbox {
                    field("url: ") { textfield {}.bind(model.urlProperty) }
                    field("user: ") { textfield { }.bind(model.userProperty) }
                    field("password: ") { passwordfield { }.bind(model.passwordProperty) }
                }
            }
        }

        buttonbar {
            button("reset").action {
                model.clear()
            }

            button("testConnect").action {
                val simpleDataSource = SimpleDataSource(model.url, model.user, model.password)
                val valid = simpleDataSource.connection.isValid(3000)
                alert(type = Alert.AlertType.INFORMATION, "valid", content = valid.toString())
            }

            button("save").action {
                dataSource = SimpleDataSource(model.url, model.user, model.password)
                val valid = dataSource?.connection?.isValid(3000)
                if (valid == false) {
                    alert(type = Alert.AlertType.INFORMATION, "valid", content = false.toString())
                }
            }
        }
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
        this.urlProperty.set("")
        this.userProperty.set("")
        this.passwordProperty.set("")
    }
}