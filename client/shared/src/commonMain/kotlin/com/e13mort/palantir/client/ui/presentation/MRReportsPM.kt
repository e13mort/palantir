package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.interactors.PercentileReport
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import com.e13mort.palantir.interactors.Range
import com.e13mort.palantir.model.ReportsRepository
import com.e13mort.palantir.utils.period
import com.e13mort.palantir.utils.secondsToFormattedTimeDiff
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel

class MRReportsPM(
    pmParams: PmParams,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val projectsInteractor: PrintAllProjectsInteractor,
    private val dateStringConverter: DateStringConverter,
    private val percentiles: List<ReportsRepository.Percentile>,
    val reportsInteractorFactory: (Long, List<Range>) -> Interactor<PercentileReport>,
) : PresentationModel(pmParams) {
    companion object Description : PmDescription

    private val _states = MutableStateFlow<State>(State.READY)
    val uiStates: StateFlow<State>
        get() = _states

    val textFieldState = MutableStateFlow(RangeTextFieldState(""))

    fun calculateReports() {
        scope.launch {
            val rangeTextFieldState = textFieldState.value
            if (!rangeTextFieldState.valid) return@launch
            _states.value = State.LOADING
            launch(backgroundDispatcher) {
                val allProjectsResult = projectsInteractor.run()
                val syncedProjects = allProjectsResult.projects(true)
                val resultMap = mutableMapOf<String, List<State.ReportsReady.ReportDataRow>>()
                syncedProjects.forEach { project ->
                    val report = reportsInteractorFactory(
                        project.id().toLong(), //todo refactor to avoid casting
                        rangeTextFieldState.ranges()
                    ).run()
                    resultMap[project.name()] = report.prepareReports()
                    val dataHeaders = percentiles.map {
                        it.name
                    }
                    scope.launch {
                        _states.value = State.ReportsReady(resultMap.toMap(), dataHeaders)
                    }
                }
            }
        }
    }

    private fun PercentileReport.prepareReports(): List<State.ReportsReady.ReportDataRow> {
        return mutableListOf<State.ReportsReady.ReportDataRow>().also { resultContainer ->
            for (i in 0 until periodsCount()) {
                resultContainer += State.ReportsReady.ReportDataRow(
                    period = period(i, dateStringConverter),
                    totalMrCount = totalMRCount(i),
                    data = mutableListOf<State.ReportsReady.ReportDataRow.CellData>().also { dataContainer ->
                        percentiles.forEach { percentile ->
                            val cellData = State.ReportsReady.ReportDataRow.CellData(
                                compactTimeDuration = periodValue(i, percentile).secondsToFormattedTimeDiff(),
                                relativeTimeDiff = convertDiffToPercents(i, percentile),
                            )
                            dataContainer += cellData
                        }
                    }
                )
            }
        }
    }

    private fun PercentileReport.convertDiffToPercents(
        i: Int,
        percentile: ReportsRepository.Percentile
    ): Int {
        val diff = compareTwoPeriods(i, (i - 1).coerceAtLeast(0), percentile)
        return (diff * 100 - 100).toInt()
    }

    data class RangeTextFieldState(
        val value: String,
        val valid: Boolean = true
    ) {
        fun ranges(): List<Range> {
            return listOf(Range(0, System.currentTimeMillis()))
        }
    }

    sealed interface State {
        object READY : State
        object LOADING : State
        data class ReportsReady(
            val reports: Map<String, List<ReportDataRow>>,
            val dataHeaders: List<String>,
        ) : State {
            data class ReportDataRow(
                val period: String,
                val totalMrCount: Int,
                val data: List<CellData>
            ) {
                data class CellData(
                    val compactTimeDuration: String,
                    val relativeTimeDiff: Int,
                )
            }
        }
    }

}