package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequests
import com.e13mort.palantir.model.ProjectRepository
import kotlinx.coroutines.flow.toList

class PrintProjectMergeRequestsInteractor(
    private val localRepository: ProjectRepository,
    private val projectId: Long
) : Interactor<PrintProjectMergeRequestsInteractor.MergeRequestsReport> {

    class MergeRequestsReport(private val mrs: MergeRequests) {

        suspend fun walk(callBack: (id: String, sourceBranch: String, targetBranch: String, created: Long, closed: Long?, state: String) -> Unit) {
            val toList = mrs.values().toList()
            toList.forEach {
                callBack(
                    it.id(),
                    it.sourceBranch().name(),
                    it.targetBranch().name(),
                    it.createdTime(),
                    it.closedTime(),
                    it.state().toString()
                )
            }
        }
    }

    override suspend fun run(): MergeRequestsReport {
        val project = localRepository.findProject(projectId) ?: throw Exception("Project with id $projectId not found")
        return MergeRequestsReport(project.mergeRequests())
    }
}