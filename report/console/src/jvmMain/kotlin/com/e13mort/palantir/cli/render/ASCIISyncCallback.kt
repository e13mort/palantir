package com.e13mort.palantir.cli.render

import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.utils.Console
import com.e13mort.palantir.utils.writeTo
import com.e13mort.palantir.model.SyncableProjectRepository.SyncableProject.UpdateBranchesCallback.BranchEvent.BranchAdded as BRBranchAdded
import com.e13mort.palantir.model.SyncableProjectRepository.SyncableProject.UpdateBranchesCallback.BranchEvent.RemoteLoadingStarted as BRRemoteLoadingStarted

class ASCIISyncCallback(private val console: Console) : SyncableProjectRepository.SyncableProject.SyncCallback {
    override fun onBranchEvent(branchEvent: SyncableProjectRepository.SyncableProject.UpdateBranchesCallback.BranchEvent) {
        when (branchEvent) {
            BRRemoteLoadingStarted -> "Loading remote branches"
            is BRBranchAdded -> "Branch ${branchEvent.branchName} added"
        }.writeTo(console, Console.WriteStyle.REPLACE_LAST)
    }

    override fun onMREvent(event: SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent) {
        when (event) {
            is SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent.LoadMR -> "${event.index + 1} of ${event.totalSize} Loading MR: ${event.mrDescription}"
            SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent.RemoteLoadingStarted -> "Loading remote merge requests"
        }.writeTo(console, Console.WriteStyle.REPLACE_LAST)
    }

}