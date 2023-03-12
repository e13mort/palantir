package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.*
import com.e13mort.gitlabreport.model.local.*
import kotlinx.coroutines.flow.*
import com.e13mort.palantir.model.SyncableProjectRepository.SyncableProject.UpdateBranchesCallback.BranchEvent.BranchAdded as BranchAdded
import com.e13mort.palantir.model.SyncableProjectRepository.SyncableProject.UpdateBranchesCallback.BranchEvent.RemoteLoadingStarted as BRRemoteLoadingStarted
import com.e13mort.palantir.model.SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent.LoadMR as LoadMREvent
import com.e13mort.palantir.model.SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent.RemoteLoadingStarted as MRRemoteLoadingStarted

class DBProjectRepository(localModel: LocalModel) : SyncableProjectRepository {

    private val projectQueries = localModel.projectQueries
    private val projectSyncQueries = localModel.projectSyncQueries
    private val branchesQueries = localModel.branchesQueries
    private val mergeRequestsQueries = localModel.mergeRequestsQueries
    private val userQueries = localModel.userQueries
    private val mrAssigneesQueries = localModel.mr_assigneesQueries
    private val mrNotes = localModel.notesQueries

    override suspend fun projects(): Flow<SyncableProjectRepository.SyncableProject> {
        return projectSyncQueries.selectAll()
            .executeAsList().asFlow().map {
                SyncableProjectImpl(it, projectSyncQueries, branchesQueries, mergeRequestsQueries, userQueries, mrAssigneesQueries, mrNotes)
            }
    }

    override suspend fun findProject(id: Long): SyncableProjectRepository.SyncableProject? {
        return projectQueries.findProject(id).executeAsOneOrNull()
            ?.toSyncable(projectSyncQueries, branchesQueries, mergeRequestsQueries, userQueries, mrAssigneesQueries, mrNotes)
    }

    override suspend fun syncedProjects(): Flow<SyncableProjectRepository.SyncableProject> {
        return projectSyncQueries.selectSynced()
            .executeAsList().asFlow().map {
                SyncableProjectImpl(
                    it,
                    projectSyncQueries,
                    branchesQueries,
                    mergeRequestsQueries,
                    userQueries,
                    mrAssigneesQueries,
                    mrNotes
                )
            }
    }

    override suspend fun projectsCount(): Long {
        return projectQueries.projectsCount().executeAsOne()
    }

    override suspend fun addProject(project: Project) {
        projectQueries.insert(DBProject(project.id().toLong(), project.name(), project.clonePaths().ssh(), project.clonePaths().http()))
    }

    override suspend fun clear() {
        projectQueries.clear()
    }

}

internal class SyncableProjectImpl(
    private val dbItem: SYNCED_PROJECTS,
    private val syncQueries: ProjectSyncQueries,
    private val branchesQueries: BranchesQueries,
    private val mergeRequestsQueries: MergeRequestsQueries,
    private val userQueries: UserQueries,
    private val mrAssigneesQueries: Mr_assigneesQueries,
    private val mrNotes: NotesQueries
) : SyncableProjectRepository.SyncableProject, ClonePaths {
    override fun synced(): Boolean {
        return dbItem.synced != 0L
    }

    override fun updateSynced(synced: Boolean) {
        if (synced)
            syncQueries.setProjectIsSynced(projectId())
        else
            syncQueries.removeSyncedProject(projectId())
    }

    override suspend fun updateBranches(branches: Branches, callback: SyncableProjectRepository.SyncableProject.UpdateBranchesCallback) {
        branchesQueries.removeProjectsBranches(projectId())
        callback.onBranchEvent(BRRemoteLoadingStarted)
        branches.values().collect {
            branchesQueries.insert(projectId(), it.name())
            callback.onBranchEvent(BranchAdded(it.name()))
        }
    }

    override suspend fun updateMergeRequests(mergeRequests: MergeRequests, callback: SyncableProjectRepository.SyncableProject.UpdateMRCallback) {
        mergeRequestsQueries.removeProjectsMergeRequests(projectId())
        callback.onMREvent(MRRemoteLoadingStarted)
        val values = mergeRequests.values()
        val mrList = values.toList()
        mrList.forEachIndexed { index, mr ->
            notifyMRProcessing(mr, callback, index, mrList.size)
            val mrId = mr.id().toLong()
            mergeRequestsQueries.insert(
                mergeRequests.project().id().toLong(),
                mrId,
                mr.state().ordinal.toLong(),
                mr.sourceBranch().name(),
                mr.targetBranch().name(),
                mr.createdTime(),
                mr.closedTime()
            )
            mrAssigneesQueries.removeByMR(mrId)
            mr.assignees().forEach { user ->
                userQueries.put(user.id(), user.name(), user.userName())
                mrAssigneesQueries.add(mrId, user.id())
            }
            mrNotes.clearForMR(mrId)
            mr.events().forEach {
                val user = it.user()
                userQueries.put(user.id(), user.name(), user.userName())
                mrNotes.add(it.id(), mrId, it.type().ordinal.toLong(), user.id(), it.content(), it.timeMillis())
            }
        }
    }

    override fun id(): String {
        return projectId().toString()
    }

    override fun name(): String {
        return dbItem.name
    }

    override fun branches(): Branches {
        return object : Branches {
            override suspend fun count(): Long {
                return branchesQueries.branchesCount(projectId()).executeAsOne()
            }

            override suspend fun values(): Flow<Branch> {
                return branchesQueries.selectAll(projectId()) { _, _, name ->
                    DBBranch(name)
                }.executeAsList().asFlow()
            }

        }
    }

    override fun mergeRequests(): MergeRequests {
        return object : MergeRequests {
            override suspend fun project(): Project {
                return this@SyncableProjectImpl
            }

            override suspend fun count(): Long {
                return mergeRequestsQueries.mergeRequestCount(this@SyncableProjectImpl.id().toLong()).executeAsOne()
            }

            override suspend fun values(): Flow<MergeRequest> {
                return mergeRequestsQueries.selectAll(projectId()).executeAsList().asFlow().map {
                    DBMergeRequest(it, mrAssigneesQueries, mrNotes)
                }
            }

        }
    }

    override fun clonePaths(): ClonePaths {
        return this
    }

    private fun projectId() = dbItem.id

    private fun notifyMRProcessing(
        mergeRequest: MergeRequest,
        callback: SyncableProjectRepository.SyncableProject.UpdateMRCallback,
        index: Int,
        totalSize: Int
    ) {
        callback.onMREvent(LoadMREvent(mergeRequest.id(), index, totalSize))
    }

    override fun ssh(): String {
        return dbItem.sshClonePath
    }

    override fun http(): String {
        return dbItem.httpClonePath
    }

}

internal data class DBBranch(private val name: String) : Branch {
    override fun name(): String = name
}

internal class DBUser(private val assignees: Assignees) : User {
    override fun id(): Long = assignees.user_id!!

    override fun name(): String = assignees.name!!

    override fun userName(): String = assignees.username!!

}

internal const val UNSPECIFIED_BRANCH_NAME = "unspecified"

internal class DBMergeRequest(
    private val storedMR: Merge_requests,
    private val mrAssigneesQueries: Mr_assigneesQueries,
    private val mrNotes: NotesQueries
) : MergeRequest {

    override fun id(): String = storedMR.id.toString()

    override fun state(): MergeRequest.State = MergeRequest.State.values()[storedMR.state.toInt()]

    override fun sourceBranch(): Branch = DBBranch(storedMR.source_branch_name ?: UNSPECIFIED_BRANCH_NAME)

    override fun targetBranch(): Branch = DBBranch(storedMR.target_branch_name ?: UNSPECIFIED_BRANCH_NAME)

    override fun createdTime(): Long = storedMR.created_time

    override fun closedTime(): Long? = storedMR.closed_time

    override fun assignees(): List<User> {
        return mrAssigneesQueries.assignees(storedMR.id).executeAsList().map {
            DBUser(it)
        }
    }

    override fun events(): List<MergeRequestEvent> {
        return mrNotes.notes(storedMR.id).executeAsList().map {
            DBMergeRequestEvent(it)
        }
    }
}

internal class DBMergeRequestEvent(private val mrNotesView: Mr_notes_view) : MergeRequestEvent {
    override fun id(): Long {
        return mrNotesView.id
    }

    override fun type(): MergeRequestEvent.Type {
        return MergeRequestEvent.Type.values()[mrNotesView.type.toInt()]
    }

    override fun timeMillis(): Long {
        return mrNotesView.created_time_millis ?: 0
    }

    override fun user(): User {
        return object : User {
            override fun id(): Long {
                return mrNotesView.user_id
            }

            override fun name(): String {
                return mrNotesView.user_name ?: ""
            }

            override fun userName(): String {
                return mrNotesView.user_userName ?: ""
            }

        }
    }

    override fun content(): String {
        return mrNotesView.content ?: ""
    }
}

fun DBProject.toSyncable(
    syncQueries: ProjectSyncQueries,
    branchesQueries: BranchesQueries,
    mergeRequestsQueries: MergeRequestsQueries,
    userQueries: UserQueries,
    mrAssigneesQueries: Mr_assigneesQueries,
    mrNotes: NotesQueries,
): SyncableProjectRepository.SyncableProject {
    return SyncableProjectImpl(
        SYNCED_PROJECTS(
            this.id, this.name, this.sshClonePath, this.httpClonePath, 0
        ), syncQueries, branchesQueries, mergeRequestsQueries, userQueries, mrAssigneesQueries, mrNotes,
    )
}

