package org.kimplify

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.kimplify.kurrency.sample.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kurrency",
    ) {
        App()
    }
}