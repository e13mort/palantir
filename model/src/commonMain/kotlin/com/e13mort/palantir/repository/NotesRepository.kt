package com.e13mort.palantir.repository

import com.e13mort.palantir.model.MergeRequestEvent

interface NotesRepository {
    suspend fun saveMergeRequestEvents(mrId: Long, events: List<MergeRequestEvent>)

    suspend fun events(projectId: Long, mrId: Long): List<MergeRequestEvent>
}