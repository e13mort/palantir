package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.ProjectRepository
import com.e13mort.gitlab_report.utils.Console
import com.e13mort.gitlab_report.utils.writeTo
import kotlinx.coroutines.flow.collect

class ScanProjectInteractor(
    private val localRepository: ProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val console: Console
) : Interactor<Unit> {
    override suspend fun run() {
        localRepository.clear()
        remoteRepository.projects().collect {
            "Add project to index: ${it.name()}".writeTo(console)
            localRepository.addProject(it)
        }
    }
}
