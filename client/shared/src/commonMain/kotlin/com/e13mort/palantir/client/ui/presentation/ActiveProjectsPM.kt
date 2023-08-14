package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel

class ActiveProjectsPM(
    pmParams: PmParams,
    private val projectsInteractor: PrintAllProjectsInteractor,
    private val main: CoroutineScope,
    private val backgroundDispatcher: CoroutineDispatcher
) : PresentationModel(pmParams) {

    companion object Description : PmDescription

    private val _stateFlow = MutableStateFlow<List<ProjectInfo>>(emptyList())

    val states: StateFlow<List<ProjectInfo>>
        get() = _stateFlow

    fun load() {
        main.launch {
            val projects = async(backgroundDispatcher) {
                projectsInteractor
                    .run()
                    .projects(true)
                    .map {
                        ProjectInfo(
                            it.id(),
                            it.name(),
                            it.branches().count(),
                            it.mergeRequests().count()
                        )
                    }
            }.await()
            _stateFlow.value = projects.toList()
        }
    }

    fun handeConfigureBtn() {
        //todo navigate to ProjectsListPM
    }

    data class ProjectInfo(
        val id: String,
        val name: String,
        val branchCount: Long,
        val mrCount: Long
    )
}