package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.interactors.AllProjectsResult
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmMessage
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel

class ActiveProjectsPM(
    pmParams: PmParams,
    private val projectsInteractor: PrintAllProjectsInteractor,
    private val backgroundDispatcher: CoroutineDispatcher
) : PresentationModel(pmParams) {

    class Description : PmDescription

    private val _stateFlow = MutableStateFlow<List<ProjectInfo>>(emptyList())

    val states: StateFlow<List<ProjectInfo>>
        get() = _stateFlow

    fun load() {
        scope.launch {
            val projectsResultFlow: Flow<AllProjectsResult> = projectsInteractor.run(Unit)
            projectsResultFlow.flowOn(backgroundDispatcher).map {
                it.projects(true)
            }.onEach {
                _stateFlow.value = it.map { project ->
                    ProjectInfo(
                        project.id(),
                        project.name(),
                        project.branches().count(),
                        project.mergeRequests().count()
                    )

                }
            }.collect()
        }
    }

    fun handeConfigureBtn() {
        messageHandler.send(ConfigureMessage)
    }

    data class ProjectInfo(
        val id: String,
        val name: String,
        val branchCount: Long,
        val mrCount: Long
    )

    object ConfigureMessage : PmMessage
}