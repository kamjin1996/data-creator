package com.kamjin.data.padding

import com.kamjin.data.padding.style.*
import com.kamjin.data.padding.view.*
import javafx.scene.*
import jfxtras.styles.jmetro.*
import tornadofx.*

/**
 *
 *
 * application
 *
 *
 * @author kam
 * @since 2022/04/29
 */
class Application : App(Main::class, TextStyle::class) {
    init {
        reloadStylesheetsOnFocus()
    }

    override fun createPrimaryScene(view: UIComponent): Scene {
        return Scene(view.root).apply {
            JMetro(this, Style.LIGHT)
        }
    }
}

fun main(args: Array<String>) {
    launch<Application>(args)
}