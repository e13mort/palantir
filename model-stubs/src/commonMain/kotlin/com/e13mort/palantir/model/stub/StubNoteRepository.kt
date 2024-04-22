/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.UnsupportedRepositoryOperationException
import com.e13mort.palantir.repository.NotesRepository

class StubNoteRepository(
    val data: MutableMap<Long, Map<Long, MutableList<MergeRequestEvent>>> = mutableMapOf()
) : NotesRepository {
    override suspend fun saveMergeRequestEvents(
        projectId: Long,
        localMrId: Long,
        events: List<MergeRequestEvent>
    ) {
        throw UnsupportedRepositoryOperationException("save")
    }

    override suspend fun events(projectId: Long, localMrId: Long): List<MergeRequestEvent> {
        return data[projectId]?.get(localMrId) ?: emptyList()
    }

}