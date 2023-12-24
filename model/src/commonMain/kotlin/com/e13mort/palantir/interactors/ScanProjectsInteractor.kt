package com.e13mort.palantir.interactors

import com.e13mort.palantir.repository.ProjectRepository
import com.e13mort.palantir.utils.Console
import com.e13mort.palantir.utils.writeTo

class ScanProjectsInteractor(
    private val localRepository: ProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val console: Console = Console.Empty
) : Interactor<Unit> {
    override suspend fun run() {
        localRepository.clear()
        remoteRepository.projects().collect {
            localRepository.addProject(it)
        }
        "${localRepository.projectsCount()} projects added to index".writeTo(console)
    }
}
