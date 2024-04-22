/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.repository.MergeRequestRepository
import com.e13mort.palantir.model.User
import com.e13mort.palantir.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PrintMergeRequestInteractor(
    private val mrRepository: MergeRequestRepository,
    private val mrNotesRepository: NotesRepository
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

    override fun run(arg: Long): Flow<MergeRequestsReport> {
        return flow {
            val mr = mrRepository.mergeRequest(arg)
                ?: throw Exception("MR with id $arg not found")
            MergeRequestsReport(
                mr.localId().toString(),
                mr.state().name,
                mr.sourceBranch().name(),
                mr.targetBranch().name(),
                mr.createdTime(),
                mr.closedTime(),
                mr.assignees(),
                mrNotesRepository.events(arg, mr.localId())
            ).apply {
                emit(this)
            }
        }
    }
}