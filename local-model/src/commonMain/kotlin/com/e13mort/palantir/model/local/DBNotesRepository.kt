package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.repository.NotesRepository

class DBNotesRepository(localModel: LocalModel) : NotesRepository {

    private val notesQueries = localModel.notesQueries
    private val userQueries = localModel.userQueries
    private val mrQueries = localModel.mergeRequestsQueries

    override suspend fun saveMergeRequestEvents(
        projectId: Long,
        localMrId: Long,
        events: List<MergeRequestEvent>
    ) {
        val mrId = mrQueries.id(projectId, localMrId).executeAsOneOrNull()
            ?: throw IllegalArgumentException("MR with projectId $projectId and local $localMrId doesn't exists")
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
                created_time_millis = event.timeMillis(),
                mr_project_id = projectId
            )
        }
    }

    override suspend fun events(projectId: Long, localMrId: Long): List<MergeRequestEvent> {
        val mrId = mrQueries.id(projectId, localMrId).executeAsOneOrNull() ?: return emptyList()
        return notesQueries.notes(mrId).executeAsList().map {
            DBMergeRequestEvent(it)
        }
    }

}