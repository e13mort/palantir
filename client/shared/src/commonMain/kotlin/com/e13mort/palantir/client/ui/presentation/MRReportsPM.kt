package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.interactors.PercentileReport
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import com.e13mort.palantir.interactors.Range
import com.e13mort.palantir.interactors.ReportRender
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
    val reportRenderer: ReportRender<PercentileReport, String>,
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
                val resultMap = mutableMapOf<String, PercentileReport>()
                syncedProjects.forEach {
                    val report = reportsInteractorFactory(
                        it.id().toLong(),
                        rangeTextFieldState.ranges()
                    ).run()
                    resultMap[it.name()] = report
                    scope.launch {
                        _states.value = State.ReportsReady(resultMap.toMap())
                    }
                }
            }
        }
    }

    data class RangeTextFieldState(
        val value: String,
        val valid: Boolean = true
    ) {
        fun ranges() : List<Range> {
            return listOf(Range(0, System.currentTimeMillis()))
        }
    }

    sealed interface State {
        object READY : State
        object LOADING : State
        data class ReportsReady(val reports: Map<String, PercentileReport>) : State

    }

}