package com.e13mort.palantir.client.desktop

import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.e13mort.palantir.client.ui.PalantirView

fun main() = singleWindowApplication(
    title = "Palantir",
    state = WindowState()
) {
    PalantirView()
}

