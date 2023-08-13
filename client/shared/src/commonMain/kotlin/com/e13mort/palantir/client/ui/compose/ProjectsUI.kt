package com.e13mort.palantir.client.ui.compose

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.e13mort.palantir.client.ui.presentation.ProjectsListPM

@Composable
fun ProjectsListPM.Render() {
    LaunchedEffect("start") {
        load()
    }
    when(val currentState = state.collectAsState().value) {
        ProjectsListPM.ListState.LOADING -> RenderLoading()
        is ProjectsListPM.ListState.ProjectsList -> RenderProjects(currentState) { id, newState ->
            this.updateSyncState(id, newState)
        }
        ProjectsListPM.ListState.READY -> { }
    }
}

@Composable
private fun RenderLoading() {
    Text("Loading")
}

@Composable
private fun RenderProjects(
    projects: ProjectsListPM.ListState.ProjectsList,
    syncUpdateCallback: (String, Boolean) -> Unit
) {
    val scrollbar = ScrollbarStyle(
    minimalHeight = 16.dp,
    thickness = 8.dp,
    shape = MaterialTheme.shapes.small,
    hoverDurationMillis = 300,
    unhoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
    hoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.50f)
    )
    CompositionLocalProvider(LocalScrollbarStyle provides scrollbar) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(projects.projects) { project ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Checkbox(
                        checked = project.indexed,
                        onCheckedChange = {
                            syncUpdateCallback(project.id, it)
                        },
                    )
                    Text(project.name)
                }
            }
        }

    }
}