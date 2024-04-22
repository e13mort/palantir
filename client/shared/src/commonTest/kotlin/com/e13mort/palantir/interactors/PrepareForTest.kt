package com.e13mort.palantir.interactors

internal suspend fun SyncInteractor.prepareForTest(localComponent: LocalComponent) {
    run(SyncInteractor.SyncStrategy.UpdateProjects).collect { }
    localComponent.projectRepository.projects().collect {
        it.updateSynced(true)
    }
}