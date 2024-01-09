package com.e13mort.palantir.interactors

import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoveProjectInteractor(
    private val projectRepository: ProjectRepository
) : Interactor<Long, Unit> {
    override fun run(arg: Long): Flow<Unit> {
        return flow {
            projectRepository.removeProjects(setOf(arg))
            emit(Unit)
        }
    }
}