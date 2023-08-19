package com.e13mort.palantir.client.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e13mort.palantir.client.ui.presentation.MRReportsPM

@Composable
fun MRReportsPM.Render() {
    val currentState = uiStates.collectAsState().value
    val controlsEnabled = currentState !is MRReportsPM.State.LOADING
    Column {
        Row {
            val rangeTextFieldState = textFieldState.collectAsState().value
            TextField(
                value = rangeTextFieldState.value,
                onValueChange = {
                    updateReportsRanges(it)
                },
                isError = !rangeTextFieldState.valid,
                placeholder = { Text("Ranges") }
            )
            Button(onClick = {
                calculateReports()
            }, enabled = controlsEnabled && rangeTextFieldState.valid) {
                Text("Show report")
            }
        }

        RenderStateData(currentState)
    }
}

@Composable
fun RenderStateData(
    currentState: MRReportsPM.State
) {
    when (currentState) {
        MRReportsPM.State.LOADING -> ShowLoadingUI()
        MRReportsPM.State.READY -> ShowReadyUI()
        is MRReportsPM.State.ReportsReady -> ShowReports(currentState)
    }
}

@Composable
fun ShowReports(
    reportsReadyState: MRReportsPM.State.ReportsReady
) {
    Box(modifier = Modifier
        .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            reportsReadyState.reports.forEach { (projectName, report) ->
                Card(
                    modifier = Modifier
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                    ) {
                        val cell1Weight = .2F
                        val cell2Weight = .1F
                        val dataHeaders = reportsReadyState.dataHeaders
                        val dataCellWeight = (1 - cell1Weight - cell2Weight) / dataHeaders.size
                        Text(
                            text = projectName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.background(MaterialTheme.colors.secondary)
                            ) {
                                Cell("Period", cell1Weight)
                                Cell("MR Count", cell2Weight)

                                dataHeaders.forEach {
                                    Cell(it, dataCellWeight)
                                }
                            }
                            report.forEach { dataRow ->
                                Row {
                                    Cell(dataRow.period, cell1Weight)
                                    Cell(dataRow.totalMrCount.toString(), cell2Weight)
                                    dataRow.data.forEach {
                                        val text = "${it.compactTimeDuration} (${it.relativeTimeDiff}%)"
                                        Cell(text, dataCellWeight)
                                    }
                                }

                            }
                        }
                    }

                }
                Spacer(modifier = Modifier.padding(16.dp))
            }

        }
    }
}

@Composable
private fun RowScope.Cell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier
            .border(width = 1.dp, color = Color.Black)
            .padding(8.dp)
            .weight(weight),
        fontSize = 10.sp
    )
}

@Composable
fun ShowReadyUI() {
    Text("Select range")
}

@Composable
fun ShowLoadingUI() {
    Text("Loading")
}

@Preview
@Composable
fun PreviewReady() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Color.Gray)
                .padding(8.dp)
        ) {
            val dataHeaders = listOf("P1", "P2", "P3")
            RenderStateData(
                MRReportsPM.State.ReportsReady(
                    mapOf(
                        "Test Project1" to createTestReport(dataHeaders.size),
                        "Test Project2" to createTestReport(dataHeaders.size),
                    ),
                    dataHeaders,
                )
            )
        }
    }
}

private fun createTestReport(dataRowsCount: Int): List<MRReportsPM.State.ReportsReady.ReportDataRow> {
    return mutableListOf<MRReportsPM.State.ReportsReady.ReportDataRow>().also { result ->
        repeat(2) {
            result += MRReportsPM.State.ReportsReady.ReportDataRow(
                "01-01-1970 - 19-08-2023",
                42,
                mutableListOf<MRReportsPM.State.ReportsReady.ReportDataRow.CellData>().also { data ->
                    repeat(dataRowsCount) { counter ->
                        data += MRReportsPM.State.ReportsReady.ReportDataRow.CellData(
                            compactTimeDuration = "$counter",
                            relativeTimeDiff = (100 - (Math.random() * 200)).toInt() // from -100 to +100
                        )
                    }
                }
            )
        }
    }

}