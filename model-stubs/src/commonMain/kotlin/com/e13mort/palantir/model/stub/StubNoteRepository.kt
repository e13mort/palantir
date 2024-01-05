package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.UnsupportedRepositoryOperationException
import com.e13mort.palantir.repository.NotesRepository

class StubNoteRepository(
    val data: MutableMap<Long, Map<Long, List<MergeRequestEvent>>> = mutableMapOf()
) : NotesRepository {
    override suspend fun saveMergeRequestEvents(mrId: Long, events: List<MergeRequestEvent>) {
        throw UnsupportedRepositoryOperationException("save")
    }

    override suspend fun events(projectId: Long, mrId: Long): List<MergeRequestEvent> {
        return data[projectId]?.get(mrId) ?: emptyList()
    }

}