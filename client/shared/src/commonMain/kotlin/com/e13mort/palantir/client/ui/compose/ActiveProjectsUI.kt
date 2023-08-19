package com.e13mort.palantir.client.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e13mort.palantir.client.ui.presentation.ActiveProjectsPM

@Composable
fun ActiveProjectsPM.Render() {
    LaunchedEffect(Unit) {
        load()
    }
    val projectInfoList = states.collectAsState().value
    RenderScreen(projectInfoList) {
        handeConfigureBtn()
    }
}

@Composable
private fun RenderScreen(
    projectInfoList: List<ActiveProjectsPM.ProjectInfo>,
    configureClickListener: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Plntr.Button(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
                ,
                onClick = { configureClickListener() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("Configure")
                }
            }
        }
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(250.dp)
        ) {
            items(projectInfoList) {
                it.Render()
            }
        }
    }
}

@Composable
fun ActiveProjectsPM.ProjectInfo.Render() {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    text = name,
                    fontSize = 24.sp,
                )
                Row(modifier = Modifier.padding(top = 16.dp)) {
                    Column {
                        Text("Branches:")
                        Text("MRs:")
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("$branchCount")
                        Text("$mrCount")
                    }
                }

            }
        }
    }
}

@Composable
@Preview
fun previewList() {
    val previewData = listOf(
        ActiveProjectsPM.ProjectInfo("id1", "Test Project", 7, 3),
        ActiveProjectsPM.ProjectInfo("id1", "Another test Project", 15, 14)
    )
    RenderScreen(previewData)
}