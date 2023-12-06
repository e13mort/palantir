package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.repository.MergeRequestRepository
import com.e13mort.palantir.model.User

class PrintMergeRequestInteractor(
    private val mrRepository: MergeRequestRepository,
    private val mergeRequestId: Long
) : Interactor<PrintMergeRequestInteractor.MergeRequestsReport> {

    data class MergeRequestsReport(
        val id: String,
        val state: String,
        val from: String,
        val to: String,
        val createdMillis: Long,
        val closedMillis: Long?,
        val assignees: List<User>,
        val events: List<MergeRequestEvent>
    )

    override suspend fun run(): MergeRequestsReport {
        val mr = mrRepository.mergeRequest(mergeRequestId) ?: throw Exception("MR with id $mergeRequestId not found")
        return MergeRequestsReport(
            mr.id(),
            mr.state().name,
            mr.sourceBranch().name(),
            mr.targetBranch().name(),
            mr.createdTime(),
            mr.closedTime(),
            mr.assignees(),
            mr.events()
        )
    }
}