package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Branches
import com.e13mort.palantir.model.ProjectRepository
import kotlinx.coroutines.flow.toList

class PrintProjectBranchesInteractor(
    private val localRepository: ProjectRepository,
    private val projectId: Long
) : Interactor<PrintProjectBranchesInteractor.BranchesReport> {

    class BranchesReport(private val branches: Branches) {

        suspend fun walk(callBack: (String) -> Unit) {
            val toList = branches.values().toList()
            toList.forEach {
                callBack(it.name())
            }
        }
    }

    override suspend fun run(): BranchesReport {
        val project = localRepository.findProject(projectId) ?: throw Exception("Project with id $projectId not found")
        return BranchesReport(project.branches())
    }
}