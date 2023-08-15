package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.interactors.AllProjectsResult
import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.interactors.ScanProjectInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.back

class ConfigureActiveProjectsPM(
    pmParams: PmParams,
    private val projectsInteractor: Interactor<AllProjectsResult>,
    private val projectSyncInteractorFactory: suspend (Long) -> ScanProjectInteractor.ScanProjectResult,
    private val main: CoroutineScope,
    private val backgroundDispatcher: CoroutineDispatcher
) : PresentationModel(pmParams) {
    object Description : PmDescription

    private val _state = MutableStateFlow<ListState>(ListState.LOADING)
    val state: StateFlow<ListState>
        get() = _state

    fun load() {
        main.launch {
            _state.value = ListState.LOADING
            val projects = CoroutineScope(backgroundDispatcher).async {
                val resultList = mutableListOf<Project>()
                val projectsReport = projectsInteractor.run()
                resultList += wrapProjects(projectsReport, true)
                resultList += wrapProjects(projectsReport, false)
                resultList.toList()
            }.await()
            _state.value = ListState.ProjectsList(projects)
        }

    }

    fun updateSyncState(projectId: String, newSyncState: Boolean) {
        val listState = _state.value as? ListState.ProjectsList ?: return
        for (project in listState.projects) {
            if (project.id == projectId) {
                project.indexed = newSyncState
                break
            }
        }
        _state.value = ListState.ProjectsList(listState.projects)
    }

    fun handleButton(actionButton: ActionButton) {
        when(actionButton) {
            ActionButton.CANCEL -> back()
            ActionButton.SAVE -> { /* save projects for sync */ }
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

