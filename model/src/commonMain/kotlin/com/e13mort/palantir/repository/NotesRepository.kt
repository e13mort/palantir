package com.e13mort.palantir.repository

import com.e13mort.palantir.model.MergeRequestEvent

interface NotesRepository {
    suspend fun saveMergeRequestEvents(
        projectId: Long,
        localMrId: Long,
        events: List<MergeRequestEvent>
    )

    suspend fun events(projectId: Long, localMrId: Long): List<MergeRequestEvent>
}