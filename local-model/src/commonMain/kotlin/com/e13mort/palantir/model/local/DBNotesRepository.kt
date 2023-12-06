package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.repository.NotesRepository

class DBNotesRepository(localModel: LocalModel) : NotesRepository {

    private val notesQueries = localModel.notesQueries
    private val userQueries = localModel.userQueries

    override suspend fun saveMergeRequestEvents(mrId: Long, events: List<MergeRequestEvent>) {
        notesQueries.clearForMR(mrId)
        events.forEach { event ->
            event.user().let { user ->
                userQueries.put(
                    id = user.id(),
                    name = user.name(),
                    username = user.userName()
                )
            }
            notesQueries.add(
                id = event.id(),
                mr_id = mrId,
                type = event.type().ordinal.toLong(),
                user_id = event.user().id(),
                content = event.content(),
                created_time_millis = event.timeMillis()
            )
        }
    }

    override suspend fun events(projectId: Long, mrId: Long): List<MergeRequestEvent> {
        return notesQueries.notes(mrId).executeAsList().map {
            DBMergeRequestEvent(it)
        }
    }

}