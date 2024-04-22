/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.ui.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.e13mort.palantir.client.ui.presentation.ActiveProjectsPM
import com.e13mort.palantir.client.ui.presentation.ConfigureActiveProjectsPM
import com.e13mort.palantir.client.ui.presentation.ProjectsScreenPM

@Composable
fun ProjectsScreenPM.Render() {
    when (val screenPM = screens().collectAsState().value) {
        is ActiveProjectsPM -> screenPM.Render()
        is ConfigureActiveProjectsPM -> screenPM.Render()
        else -> {
            Text("Not implemented yet")
        }
    }
}