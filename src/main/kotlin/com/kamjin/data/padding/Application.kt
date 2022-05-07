package com.kamjin.data.padding

import com.kamjin.data.padding.style.*
import com.kamjin.data.padding.view.*
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
}

fun main(args: Array<String>) {
    launch<Application>(args)
}