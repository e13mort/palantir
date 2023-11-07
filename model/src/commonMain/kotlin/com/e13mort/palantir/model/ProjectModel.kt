package com.e13mort.palantir.model

import kotlinx.coroutines.flow.Flow

interface Project {
    fun id(): String

    fun name(): String

    fun branches(): Branches

    fun mergeRequests(): MergeRequests

    fun clonePaths(): ClonePaths
}

interface Branches {
    suspend fun count(): Long

    suspend fun values(): Flow<Branch>
}

interface MergeRequests {
    suspend fun project(): Project

    suspend fun count(): Long

    suspend fun values(): Flow<MergeRequest>
}

interface MergeRequest {
    fun id(): String

    fun state(): State

    fun sourceBranch(): Branch

    fun targetBranch(): Branch

    fun createdTime(): Long

    fun closedTime(): Long?

    fun assignees(): List<User>

    fun events(): List<MergeRequestEvent>

    enum class State {
        OPEN, MERGED, CLOSED
    }
}

interface MergeRequestEvent {
    enum class Type {
        APPROVE, DISCUSSION, GENERAL_NOTE
    }

    fun id(): Long

    fun type(): Type

    fun timeMillis(): Long

    fun user(): User

    fun content(): String
}

interface User {
    fun id(): Long

    fun name(): String

    fun userName(): String
}

interface Branch {
    fun name(): String
}

interface ClonePaths {
    fun ssh(): String

    fun http(): String
}

interface ProjectRepository {
    suspend fun projects(): Flow<Project>

    suspend fun findProject(id: Long): Project?

    suspend fun addProject(project: Project)

    suspend fun projectsCount(): Long

    suspend fun clear()
}

interface MergeRequestRepository {
    suspend fun mergeRequest(id: Long): MergeRequest?

    suspend fun saveMergeRequests(projectId: Long, mergeRequests: List<MergeRequest>)
}

interface SyncableProjectRepository : ProjectRepository {
    override suspend fun projects(): Flow<SyncableProject>

    override suspend fun findProject(id: Long): SyncableProject?

    suspend fun syncedProjects(): Flow<SyncableProject>

    interface SyncableProject : Project {
        fun synced(): Boolean

        fun updateSynced(synced: Boolean)

        @Deprecated("this logic should be moved to interactor")
        suspend fun updateBranches(branches: Branches, callback: UpdateBranchesCallback = UpdateBranchesCallback {})

        @Deprecated("this logic should be moved to interactor")
        suspend fun updateMergeRequests(mergeRequests: MergeRequests, callback: UpdateMRCallback = UpdateMRCallback {})

        fun interface UpdateMRCallback {
            fun onMREvent(event: MREvent)

            sealed class MREvent {
                object RemoteLoadingStarted : MREvent()

                data class LoadMR(val mrDescription: String, val index: Int, val totalSize: Int) : MREvent()
            }
        }

        fun interface UpdateBranchesCallback {
            sealed class BranchEvent {
                object RemoteLoadingStarted : BranchEvent()

                data class BranchAdded(val branchName: String) : BranchEvent()
            }

            fun onBranchEvent(branchEvent: BranchEvent)
        }

        interface SyncCallback: UpdateBranchesCallback, UpdateMRCallback
    }
}