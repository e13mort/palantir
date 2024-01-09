package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Project
import com.e13mort.palantir.model.SyncableProjectRepository
import kotlinx.coroutines.flow.toList

class SyncBranchesPlan(
    private val remoteProject: Project,
    private val localProject: SyncableProjectRepository.SyncableProject
) : SyncInteractor.SyncPlan<SyncInteractor.SyncResult.State> {
    override suspend fun sync(callback: suspend (SyncInteractor.SyncResult.State) -> Unit): SyncInteractor.SyncResult.State {
        callback(SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.LOADING))
        val remoteBranches = remoteProject.branches().values().toList()
        callback(SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.SAVING))
        localProject.updateBranches(remoteBranches)
        return SyncInteractor.SyncResult.State.Done(remoteBranches.size.toLong()).also {
            callback(it.copy())
        }
    }
}