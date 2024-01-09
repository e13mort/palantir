package com.e13mort.palantir.model

import com.e13mort.palantir.repository.NotesRepository
import org.gitlab4j.api.GitLabApi

class GitlabNoteRepository(
    url: String,
    key: String,
) : NotesRepository {

    private val gitLabApi = GitLabApi(url, key)
    override suspend fun saveMergeRequestEvents(
        projectId: Long,
        localMrId: Long,
        events: List<MergeRequestEvent>
    ) {
        throw UnsupportedRepositoryOperationException("save")
    }

    override suspend fun events(projectId: Long, localMrId: Long): List<MergeRequestEvent> {
        return gitLabApi.notesApi.getMergeRequestNotes(projectId, localMrId).map {
            GitlabEvent(it)
        }
    }

}