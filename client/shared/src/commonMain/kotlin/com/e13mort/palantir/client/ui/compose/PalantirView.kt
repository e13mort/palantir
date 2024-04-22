/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.e13mort.palantir.client.ui.presentation.MRReportsPM
import com.e13mort.palantir.client.ui.presentation.MainAppPM
import com.e13mort.palantir.client.ui.presentation.ProjectsScreenPM
import com.e13mort.palantir.client.ui.presentation.SettingsPM
import io.chozzle.composemacostheme.MacButton
import io.chozzle.composemacostheme.MacCheckbox
import io.chozzle.composemacostheme.MacTheme
import io.chozzle.composemacostheme.modifiedofficial.MacOutlinedTextField

object Plntr {
    @Composable
    fun Theme(content: @Composable () -> Unit) {
        MacTheme(content = content)
    }

    @Composable
    fun TextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        isError: Boolean = false,
        singleLine: Boolean,
        placeholder: @Composable() (() -> Unit)? = null,
    ) {
        MacOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            isError = isError,
            placeholder = placeholder,
            singleLine = singleLine
        )
    }

    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        content: @Composable RowScope.() -> Unit
    ) {
        MacButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun CheckBox(
        checked: Boolean,
        onCheckedChange: ((Boolean) -> Unit),
        modifier: Modifier = Modifier,
    ) {
        MacCheckbox(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppPM.Render() {
    Scaffold {
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(0.2F)
            ) {
                RenderMenu()
            }
            Box(
                modifier = Modifier.fillMaxSize().weight(0.8F)
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

fun MainAppPM.TopLevelItems.name(): String {
    return when (this) {
        MainAppPM.TopLevelItems.PROJECTS -> "Projects"
        MainAppPM.TopLevelItems.SETTINGS -> "Settings"
        MainAppPM.TopLevelItems.REPORTS -> "Reports"
    }
}

fun MainAppPM.TopLevelItems.icon(): ImageVector {
    return when (this) {
        MainAppPM.TopLevelItems.PROJECTS -> Icons.Default.List
        MainAppPM.TopLevelItems.SETTINGS -> Icons.Default.Settings
        MainAppPM.TopLevelItems.REPORTS -> Icons.Default.Report
    }
}

@Composable
private fun MainAppPM.RenderMainContent() {
    when (val currentState = this.states.collectAsState().value) {
        is ProjectsScreenPM -> currentState.Render()
        is SettingsPM -> {
            Text("Settings")
        }

        is MRReportsPM -> currentState.Render()
        else -> {
            Text("Not supported yet")
        }
    }
}