package com.e13mort.palantir.interactors

class SyncProjectPlan(
    private val syncBranchesPlan: SyncBranchesPlan,
    private val syncMRsPlan: SyncMRsPlan
) : SyncInteractor.SyncPlan<SyncInteractor.SyncResult.ProjectSyncState> {
    override suspend fun sync(callback: suspend (SyncInteractor.SyncResult.ProjectSyncState) -> Unit): SyncInteractor.SyncResult.ProjectSyncState {
        var projectSyncState = SyncInteractor.SyncResult.ProjectSyncState(
            projectSyncState = SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.COMPLEX),
            branchesState = SyncInteractor.SyncResult.State.Pending,
            mrs = SyncInteractor.SyncResult.MrsSyncState()
        )
        callback(projectSyncState)
        val branchesSyncState = syncBranchesPlan.sync {
            projectSyncState = projectSyncState.copy(
                branchesState = it
            ).apply {
                callback(this)
            }
        }
        val mrsSyncState = syncMRsPlan.sync {
            projectSyncState = projectSyncState.copy(
                mrs = it
            ).apply {
                callback(this)
            }
        }
        projectSyncState = projectSyncState.copy(
            projectSyncState = SyncInteractor.SyncResult.State.Done(1),
            branchesState = branchesSyncState,
            mrs = mrsSyncState
        )
        return projectSyncState

    }

}