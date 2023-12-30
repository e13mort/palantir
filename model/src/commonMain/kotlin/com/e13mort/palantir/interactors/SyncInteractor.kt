package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.Project
import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.repository.MergeRequestRepository
import com.e13mort.palantir.repository.NotesRepository
import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

class SyncInteractor(
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val mergeRequestRepository: MergeRequestRepository,
    private val mergeRequestNotesRepository: NotesRepository,
    private val syncCallback: SyncableProjectRepository.SyncableProject.SyncCallback = SyncableProjectRepository.SyncableProject.SyncCallback
) : Interactor<SyncInteractor.SyncStrategy, SyncInteractor.SyncResult> {

    sealed interface SyncStrategy {
        data object UpdateProjects : SyncStrategy
        data object FullSyncForActiveProjects : SyncStrategy
        data class FullSyncForProject(val projectId: Long) : SyncStrategy
    }

    data class SyncResult(
        val state: State,
        val projects: Map<Long, ProjectSyncState> = emptyMap()
    ) {

        data class ProjectSyncState(
            val projectSyncState: State,
            val branchesState: State,
            val mrs: List<MRSyncState>
        )

        data class MRSyncState(val id: Long, val state: State)

        sealed class State {

            enum class ProgressState { LOADING, SAVING }

            data object Pending : State()
            data class InProgress(val state: ProgressState) : State()
            data class Done(val itemsUpdated: Long) : State()
            data object Skipped : State()
        }

        //projectN ->
        //  branches ->
        //      loading...
        //      saving...
        //  mrs ->
        //      loading all info
        //       mr N ->
        //           loading...
        //           saving...
    }

    override suspend fun run(arg: SyncStrategy): Flow<SyncResult> {
        return flow {
            when (arg) {
                SyncStrategy.FullSyncForActiveProjects -> runFullSyncForSelectedProjects(this)
                is SyncStrategy.FullSyncForProject -> runFullSyncForProject(this, arg.projectId)
                SyncStrategy.UpdateProjects -> updateLocalProjects(this)
            }
        }
    }

    private suspend fun updateLocalProjects(flowCollector: FlowCollector<SyncResult>) {
        flowCollector.emit(SyncResult(SyncResult.State.Pending))
        flowCollector.emit(SyncResult(SyncResult.State.InProgress(SyncResult.State.ProgressState.LOADING)))
        val remoteProjects = remoteRepository.projects().toList()
        flowCollector.emit(SyncResult(SyncResult.State.InProgress(SyncResult.State.ProgressState.SAVING)))
        val localProjects = projectRepository.projects().toList()

        val remoteProjectIds = remoteProjects.map { it.id().toLong() }.toSet()
        val localProjectIds = localProjects.map { it.id().toLong() }.toSet()

        val unmodifiedProjectsIds = localProjectIds.intersect(remoteProjectIds)
        val localProjectIdsForRemoval = localProjectIds - unmodifiedProjectsIds
        val remoteProjectsForAddition = remoteProjectIds - unmodifiedProjectsIds

        projectRepository.removeProjects(localProjectIdsForRemoval)

        remoteProjects.filter {
            remoteProjectsForAddition.contains(it.id().toLong())
        }.forEach { project ->
            projectRepository.addProject(project)
        }
        flowCollector.emit(SyncResult(SyncResult.State.Done(remoteProjectsForAddition.size.toLong())))
    }

    private suspend fun runFullSyncForSelectedProjects(flowCollector: FlowCollector<SyncResult>) {
        var syncedCounter = 0L
        projectRepository.syncedProjects().collect {
            remoteRepository.findProject(it.id().toLong())?.let { remoteProject ->
                syncProject(it, remoteProject, flowCollector)
                syncedCounter++
            }
        }
    }

    private suspend fun runFullSyncForProject(
        flowCollector: FlowCollector<SyncResult>,
        projectId: Long
    ) {
        val localProject = projectRepository.projects().filter { it.id().toLong() == projectId }.firstOrNull()
            ?: throw IllegalArgumentException("Local project with id $projectId doesn't exists")
        val remoteProject = remoteRepository.findProject(projectId) ?: throw IllegalArgumentException("Remote project with id $projectId doesn't exists")
        localProject.updateSynced(true)
        syncProject(localProject, remoteProject, flowCollector)
    }

    private suspend fun syncProject(
        localProject: SyncableProjectRepository.SyncableProject,
        remoteProject: Project,
        flowCollector: FlowCollector<SyncResult>
    ) {
        flowCollector.emit(SyncResult(state = SyncResult.State.Pending))
//        val remoteBranches = remoteProject.branches().values().toList()
        localProject.updateBranches(remoteProject.branches(), syncCallback)
        syncMRs(remoteProject, syncCallback)
        flowCollector.emit(SyncResult(state = SyncResult.State.Done(1))) //fixme
    }

    private suspend fun syncMRs(
        remoteProject: Project,
        syncCallback: SyncableProjectRepository.SyncableProject.SyncCallback
    ) {
        val projectId = remoteProject.id().toLong()
        val mergeRequests = remoteProject.mergeRequests()

        mergeRequestRepository.deleteMergeRequestsForProject(projectId)
        syncCallback.onMREvent(SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent.RemoteLoadingStarted)

        val mergeRequestList = mergeRequests.values().toList()
        mergeRequestRepository.saveMergeRequests(projectId, mergeRequestList)

        mergeRequestList.forEachIndexed {index, mergeRequest ->
            notifyMRProcessing(mergeRequest, syncCallback, index, mergeRequestList.size)
            mergeRequestNotesRepository.saveMergeRequestEvents(mergeRequest.id().toLong(), mergeRequest.events())
        }
    }

    private fun notifyMRProcessing(
        mergeRequest: MergeRequest,
        callback: SyncableProjectRepository.SyncableProject.UpdateMRCallback,
        index: Int,
        totalSize: Int
    ) {
        callback.onMREvent(
            SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent.LoadMR(
                mergeRequest.id(),
                index,
                totalSize
            )
        )
    }

}