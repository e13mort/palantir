package com.e13mort.palantir.client.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.e13mort.palantir.client.ui.presentation.ConfigureActiveProjectsPM
import com.e13mort.palantir.client.ui.presentation.ConfigureActiveProjectsPM.ActionButton

@Composable
fun ConfigureActiveProjectsPM.Render() {
    LaunchedEffect(Unit) {
        load()
    }
    val listState = state.collectAsState().value
    MainContent(listState)
}

@Composable
private fun ConfigureActiveProjectsPM.MainContent(
    listState: ConfigureActiveProjectsPM.ListState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        RenderActionButtons {
            handleButton(it)
        }
        RenderProjectList(listState) { projectId, newState ->
            updateSyncState(projectId, newState)
        }
    }
}

@Composable
fun RenderActionButtons(
    callback: (ActionButton) -> Unit = { }
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box {
            Row {
                Button(
                    modifier = Modifier,
                    onClick = { callback(ActionButton.SAVE) }
                ) {
                    Text("Save")
                }
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = { callback(ActionButton.CANCEL) }
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun RenderProjectList(
    listState: ConfigureActiveProjectsPM.ListState,
    callback: (String, Boolean) -> Unit = { _,_ -> }
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
        when (listState) {
            ConfigureActiveProjectsPM.ListState.LOADING -> RenderLoading()
            is ConfigureActiveProjectsPM.ListState.ProjectsList -> RenderProjects(listState) { id, newState ->
                callback(id, newState)
            }

            ConfigureActiveProjectsPM.ListState.READY -> {}
        }
    }

}

@Composable
private fun RenderLoading() {
    Text("Loading")
}

@Composable
private fun RenderProjects(
    projects: ConfigureActiveProjectsPM.ListState.ProjectsList,
    syncUpdateCallback: (String, Boolean) -> Unit
) {
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

@Composable
@Preview
fun PreviewButtons() {
    RenderActionButtons {

    }
}