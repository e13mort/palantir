package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.repository.MergeRequestRepository
import com.e13mort.palantir.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PrintMergeRequestInteractor(
    private val mrRepository: MergeRequestRepository
) : Interactor<Long, PrintMergeRequestInteractor.MergeRequestsReport> {

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

    override suspend fun run(arg: Long): Flow<MergeRequestsReport> {
        return flow {
            val mr = mrRepository.mergeRequest(arg)
                ?: throw Exception("MR with id $arg not found")
            MergeRequestsReport(
                mr.id(),
                mr.state().name,
                mr.sourceBranch().name(),
                mr.targetBranch().name(),
                mr.createdTime(),
                mr.closedTime(),
                mr.assignees(),
                mr.events()
            ).apply {
                emit(this)
            }
        }
    }
}