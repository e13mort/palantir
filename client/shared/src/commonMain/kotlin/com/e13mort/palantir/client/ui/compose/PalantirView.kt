package com.e13mort.palantir.client.ui.compose

import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.e13mort.palantir.client.ui.presentation.MainAppPM
import com.e13mort.palantir.client.ui.presentation.ProjectsListPM
import com.e13mort.palantir.client.ui.presentation.SettingsPM

@Composable
fun PalantirView() {
    Text("Palantir")
}

@Composable
fun MainAppPM.Render() {
    DesktopTheme {  }
    MaterialTheme {
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(0.3F)
            ) {
                RenderMenu()
            }
            Box(
                modifier = Modifier.fillMaxSize().weight(0.7F)
            ) {
                RenderMainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppPM.RenderMenu() {
    ModalDrawerSheet {
        menuItems.forEach {
            NavigationDrawerItem(
                label = { Text(it.name()) },
                selected = false,
                onClick = { switchToItem(it) },
                icon = { Icon(it.icon(), null) }
            )
        }
    }
}

fun MainAppPM.TopLevelItems.name() : String {
    return when(this) {
        MainAppPM.TopLevelItems.PROJECTS -> "Projects"
        MainAppPM.TopLevelItems.SETTINGS -> "Settings"
    }
}

fun MainAppPM.TopLevelItems.icon(): ImageVector {
    return when(this) {
        MainAppPM.TopLevelItems.PROJECTS -> Icons.Default.List
        MainAppPM.TopLevelItems.SETTINGS -> Icons.Default.Settings
    }
}

@Composable
private fun MainAppPM.RenderMainContent() {
    when (val currentState = this.states.collectAsState().value) {
        is ProjectsListPM -> currentState.Render()
        is SettingsPM -> { Text("Settings") }
        else -> {
            Text("Not supported yet")
        }
    }
}

@Composable
@Preview
fun preview() {
    PalantirView()
}