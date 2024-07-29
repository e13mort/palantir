/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.interactors.PercentileReport
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import com.e13mort.palantir.interactors.Range
import com.e13mort.palantir.model.Percentile
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.RangeParser
import com.e13mort.palantir.utils.asInputString
import com.e13mort.palantir.utils.asString
import com.e13mort.palantir.utils.secondsToFormattedTimeDiff
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel

class MRReportsPM(
    pmParams: PmParams,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val projectsInteractor: PrintAllProjectsInteractor,
    private val dateStringConverter: DateStringConverter,
    private val rangeParser: RangeParser,
    private val percentiles: List<Percentile>,
    private val reportsInteractor: Interactor<Pair<Long, List<Range>>, PercentileReport>,
) : PresentationModel(pmParams) {
    companion object Description : PmDescription

    private val _states = MutableStateFlow<State>(State.READY)
    val uiStates: StateFlow<State>
        get() = _states

    private val _textFieldState = MutableStateFlow(createInitRange())

    val textFieldState: StateFlow<RangeTextFieldState>
        get() = _textFieldState

    fun calculateReports() {
        scope.launch {
            val rangeTextFieldState = textFieldState.value
            if (!rangeTextFieldState.valid) return@launch
            _states.value = State.LOADING
            projectsInteractor.run(Unit).flowOn(backgroundDispatcher).collect {
                val allProjectsResult = it
                val syncedProjects = allProjectsResult.projects(true)
                val resultMap = mutableMapOf<String, List<State.ReportsReady.ReportDataRow>>()
                syncedProjects.forEach { project ->
                    scope.launch {
                        reportsInteractor.run(
                            project.id().toLong() to //todo refactor to avoid casting
                                    rangeTextFieldState.ranges
                        ).flowOn(backgroundDispatcher).collect { report: PercentileReport ->
                            resultMap[project.name()] = report.prepareReports()
                            val dataHeaders = percentiles.map { percentile ->
                                percentile.name
                            }
                            _states.value = State.ReportsReady(resultMap.toMap(), dataHeaders)
                        }

                    }
                }
            }
        }
    }

    fun updateReportsRanges(newValue: String) {
        val newRanges = try {
            rangeParser.convert(newValue)
        } catch (e: Exception) {
            emptyList()
        }
        val valid = newRanges.isNotEmpty()
        _textFieldState.value =
            _textFieldState.value.copy(
                value = newValue, valid = valid, ranges = newRanges
            )
    }

    private fun PercentileReport.prepareReports(): List<State.ReportsReady.ReportDataRow> {
        return mutableListOf<State.ReportsReady.ReportDataRow>().also { resultContainer ->
            for (i in 0 until periodsCount()) {
                resultContainer += State.ReportsReady.ReportDataRow(
                    period = period(i).asString(dateStringConverter),
                    totalMrCount = totalMRCount(i),
                    data = mutableListOf<State.ReportsReady.ReportDataRow.CellData>().also { dataContainer ->
                        percentiles.forEach { percentile ->
                            val cellData = State.ReportsReady.ReportDataRow.CellData(
                                compactTimeDuration = periodValue(
                                    i,
                                    percentile
                                ).secondsToFormattedTimeDiff(),
                                relativeTimeDiff = convertDiffToPercents(i, percentile),
                            )
                            dataContainer += cellData
                        }
                    }
                )
            }
        }
    }

    private fun createInitRange(): RangeTextFieldState {
        val element = Range(0, System.currentTimeMillis())
        return RangeTextFieldState(
            value = element.asInputString(dateStringConverter),
            valid = true,
            ranges = listOf(element)
        )
    }

    private fun PercentileReport.convertDiffToPercents(
        i: Int,
        percentile: Percentile
    ): Int {
        val diff = compareTwoPeriods(i, (i - 1).coerceAtLeast(0), percentile)
        return (diff * 100 - 100).toInt()
    }

    data class RangeTextFieldState(
        val value: String = "",
        val valid: Boolean = true,
        val ranges: List<Range> = listOf(Range(0, System.currentTimeMillis()))
    )

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