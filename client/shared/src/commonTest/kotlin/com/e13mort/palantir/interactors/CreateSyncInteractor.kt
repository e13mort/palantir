package com.e13mort.palantir.interactors

internal fun LocalComponent.createSyncInteractor(repositoryBuilder: RemoteProjectRepositoryBuilder): SyncInteractor {
    return SyncInteractor(
        projectRepository = projectRepository,
        remoteRepository = repositoryBuilder.build(),
        mergeRequestRepository = mrRepository,
        mergeRequestLocalNotesRepository = notesRepository,
        mergeRequestRemoteNotesRepository = repositoryBuilder.stubNotesRepository,
    )
}