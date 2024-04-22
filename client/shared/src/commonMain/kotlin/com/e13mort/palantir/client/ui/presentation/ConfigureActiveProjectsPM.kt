package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.interactors.AllProjectsResult
import com.e13mort.palantir.interactors.Interactor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.back

class ConfigureActiveProjectsPM(
    pmParams: PmParams,
    private val projectsInteractor: Interactor<Unit, AllProjectsResult>,
    private val backgroundDispatcher: CoroutineDispatcher
) : PresentationModel(pmParams) {
    object Description : PmDescription

    private val _state = MutableStateFlow<ListState>(ListState.LOADING)
    val state: StateFlow<ListState>
        get() = _state

    fun load() {
        scope.launch {
            _state.value = ListState.LOADING
            projectsInteractor.run(Unit)
                .flowOn(backgroundDispatcher)
                .map {
                    val resultList = mutableListOf<Project>()
                    resultList += wrapProjects(it, true)
                    resultList += wrapProjects(it, false)
                    ListState.ProjectsList(resultList.toList())
                }
                .collect {
                    _state.value = it
                }
        }

    }

    fun updateSyncState(projectId: String) {
        val listState = _state.value as? ListState.ProjectsList ?: return
        for (project in listState.projects) {
            if (project.id == projectId) {
                project.indexed = !project.indexed
                break
            }
        }
        _state.value = ListState.ProjectsList(listState.projects)
    }

    fun handleButton(actionButton: ActionButton) {
        when (actionButton) {
            ActionButton.CANCEL -> back()
            ActionButton.SAVE -> { /* save projects for sync */
            }
        }
    }

    private fun wrapProjects(projectsReport: AllProjectsResult, synced: Boolean) =
        projectsReport.projects(synced).map {
            Project(it.id(), it.name(), synced)
        }

    enum class ActionButton {
        CANCEL, SAVE
    }

    sealed interface ListState {
        object READY : ListState
        object LOADING : ListState

        class ProjectsList(val projects: List<Project>) : ListState
    }

    data class Project(
        val id: String,
        val name: String,
        var indexed: Boolean
    )
}

