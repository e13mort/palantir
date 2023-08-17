package com.e13mort.palantir.client.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.e13mort.palantir.cli.render.ASCIIPercentileReportRenderer
import com.e13mort.palantir.cli.render.DateStringConverter
import com.e13mort.palantir.client.ui.presentation.MRReportsPM
import com.e13mort.palantir.interactors.PercentileReport
import com.e13mort.palantir.interactors.ReportRender
import com.e13mort.palantir.model.ReportsRepository
import java.text.SimpleDateFormat

@Composable
fun MRReportsPM.Render() {
    val currentState = uiStates.collectAsState().value
    val controlsEnabled = currentState !is MRReportsPM.State.LOADING
    Column {
        Column {
            TextField("", enabled = controlsEnabled, onValueChange = {
                textFieldState.value = textFieldState.value.copy(value = it)
            })
            Button(onClick = {
                calculateReports()
            }, enabled = controlsEnabled) {
                Text("Show report")
            }
        }

        RenderStateData(currentState, reportRenderer)
    }
}

@Composable
fun RenderStateData(
    currentState: MRReportsPM.State, reportRender: ReportRender<PercentileReport, String>
) {
    when (currentState) {
        MRReportsPM.State.LOADING -> ShowLoadingUI()
        MRReportsPM.State.READY -> ShowReadyUI()
        is MRReportsPM.State.ReportsReady -> ShowReports(currentState, reportRender)
    }
}

@Composable
fun ShowReports(
    reportsReadyState: MRReportsPM.State.ReportsReady,
    reportRenderer: ReportRender<PercentileReport, String>
) {
    Card(Modifier.padding(16.dp)) {
        Column {
            reportsReadyState.reports.forEach { (projectName, report) ->

                Column {
                    Text("Project: $projectName")
                    Text(
                        text = reportRenderer.render(report),
                        style = TextStyle(fontFamily = FontFamily.Monospace)
                    )
                }
            }

        }
    }
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
    RenderStateData(MRReportsPM.State.ReportsReady(
        mapOf(
            "Test Project1" to createTestReport(),
            "Test Project2" to createTestReport(),
        )
    ), createTestRender())
}

private fun createTestReport() = object : PercentileReport {
    override fun periodsCount(): Int {
        return 1
    }

    override fun period(index: Int): PercentileReport.Period {
        return PercentileReport.Period(0, System.currentTimeMillis())
    }

    override fun periodValue(
        index: Int,
        percentile: ReportsRepository.Percentile
    ): Long {
        return 123
    }

    override fun totalMRCount(index: Int): Int {
        return 42
    }

    override fun compareTwoPeriods(
        firstIndex: Int,
        secondIndex: Int,
        percentile: ReportsRepository.Percentile
    ): Float {
        return 0F
    }

}

fun createTestRender(): ReportRender<PercentileReport, String> {
    return ASCIIPercentileReportRenderer(
        object : DateStringConverter {
            override fun convertDateToString(date: Long): String {
                return SimpleDateFormat("dd-MM-yyyy").format(date)
            }
        },
        ReportsRepository.Percentile.values().toList()
    )
}

