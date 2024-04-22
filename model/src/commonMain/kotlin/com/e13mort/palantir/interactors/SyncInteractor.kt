package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.repository.MergeRequestRepository
import com.e13mort.palantir.repository.NotesRepository
import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class SyncInteractor(
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val mergeRequestRepository: MergeRequestRepository,
    private val mergeRequestLocalNotesRepository: NotesRepository,
    private val mergeRequestRemoteNotesRepository: NotesRepository,
) : Interactor<SyncInteractor.SyncStrategy, SyncInteractor.SyncResult> {

    sealed interface SyncStrategy {
        data object UpdateProjects : SyncStrategy
        data class FullSyncForActiveProjects(val force: Boolean = false) : SyncStrategy
        data class FullSyncForProject(val projectId: Long, val force: Boolean = false) :
            SyncStrategy
    }

    interface SyncPlan<T> {
        suspend fun sync(callback: suspend (T) -> Unit): T
    }

    data class SyncResult(
        val state: State,
        val projects: Map<Long, ProjectSyncState> = emptyMap()
    ) {

        data class ProjectSyncState(
            val projectSyncState: State,
            val branchesState: State,
            val mrs: MrsSyncState
        )

        data class MrsSyncState(
            val state: State = State.Pending,
            val mergeRequests: Map<Long, State> = emptyMap()
        )

        sealed class State {
            enum class ProgressState { LOADING, SAVING, COMPLEX }
            data object Pending : State()
            data class InProgress(val state: ProgressState) : State()
            data class Done(val itemsUpdated: Long) : State()
            data object Skipped : State()
            data object Removed : State()
        }
    }

    override fun run(arg: SyncStrategy): Flow<SyncResult> {
        return flow {
            when (arg) {
                is SyncStrategy.FullSyncForActiveProjects -> runFullSyncForSelectedProjects(
                    this,
                    arg.force
                )

                is SyncStrategy.FullSyncForProject -> runFullSyncForProject(
                    this,
                    arg.projectId,
                    arg.force
                )

                SyncStrategy.UpdateProjects -> updateLocalProjects(this)
            }
        }
    }

    private suspend fun updateLocalProjects(flowCollector: FlowCollector<SyncResult>) {
        val projectsListSyncPlan =
            UpdateProjectsListSyncPlan(projectRepository, remoteRepository)
        val result = projectsListSyncPlan.sync {
            flowCollector.emit(SyncResult(it))
        }
        flowCollector.emit(SyncResult(result))
    }

    private suspend fun runFullSyncForSelectedProjects(
        flowCollector: FlowCollector<SyncResult>,
        force: Boolean
    ) {
        syncProjects(projectRepository.syncedProjects(), flowCollector, force)
    }

    private suspend fun runFullSyncForProject(
        flowCollector: FlowCollector<SyncResult>,
        projectId: Long,
        force: Boolean
    ) {
        val localProject =
            projectRepository.projects().filter { it.id().toLong() == projectId }.firstOrNull()
                ?: throw IllegalArgumentException("Local project with id $projectId doesn't exists")

        val localProjects = listOf(localProject)
        syncProjects(localProjects, flowCollector, force)
    }

    private suspend fun syncProjects(
        localProjects: List<SyncableProjectRepository.SyncableProject>,
        flowCollector: FlowCollector<SyncResult>,
        force: Boolean
    ) {

        var syncResult = SyncResult(state = SyncResult.State.Pending)
        flowCollector.emit(syncResult)
        val projectsSyncStates = localProjects.associate {
            it.id().toLong() to SyncResult.ProjectSyncState(
                SyncResult.State.Pending,
                branchesState = SyncResult.State.Pending,
                mrs = SyncResult.MrsSyncState()
            )
        }.toMutableMap()
        syncResult = syncResult.copy(
            state = SyncResult.State.InProgress(SyncResult.State.ProgressState.COMPLEX),
            projects = projectsSyncStates.toMap()
        )
        flowCollector.emit(syncResult)
        localProjects.forEach { localProject ->
            val projectId = localProject.id().toLong()
            val remoteProject = remoteRepository.findProject(projectId)
                ?: throw IllegalArgumentException("Remote project with id $projectId doesn't exists")

            localProject.updateSynced(true)
            val syncBranchesPlan = SyncBranchesPlan(remoteProject, localProject)
            val syncMRsPlan = SyncMRsPlan(
                projectId,
                remoteProject,
                mergeRequestRepository,
                mergeRequestLocalNotesRepository,
                mergeRequestRemoteNotesRepository,
                if (force) SyncMRsPlan.SyncType.Forced else SyncMRsPlan.SyncType.Incremental
            )

            val syncProjectPlan = SyncProjectPlan(syncBranchesPlan, syncMRsPlan)
            val projectsSyncState = syncProjectPlan.sync {
                projectsSyncStates[projectId] = it
                syncResult = syncResult.copy(
                    projects = projectsSyncStates.toMap()
                )
                flowCollector.emit(syncResult)
            }
            projectsSyncStates[projectId] = projectsSyncState.copy()

        }
        syncResult = syncResult.copy(
            state = SyncResult.State.Done(localProjects.size.toLong()),
            projects = projectsSyncStates.toMap()
        )
        flowCollector.emit(syncResult)
    }

}