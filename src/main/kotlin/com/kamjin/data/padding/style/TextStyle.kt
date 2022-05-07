package com.kamjin.data.padding.style

import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/05/07
 */
class TextStyle : Stylesheet() {

    companion object {
        val title by cssclass()
        val value by cssclass()
    }

    init {
        title {
            fontSize = 16.px
            fontWeight = FontWeight.EXTRA_BOLD
            textFill = Color.RED
        }

        value {
            fontSize = 12.px
        }
    }

}