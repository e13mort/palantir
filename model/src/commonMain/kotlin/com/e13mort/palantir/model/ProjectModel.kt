package com.e13mort.palantir.model

import com.e13mort.palantir.repository.ProjectRepository
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

    @Deprecated("Use dedicated repository")
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

interface SyncableProjectRepository : ProjectRepository {
    override suspend fun projects(): Flow<SyncableProject>

    override suspend fun findProject(id: Long): SyncableProject?

    suspend fun syncedProjects(): Flow<SyncableProject>

    interface SyncableProject : Project {
        fun synced(): Boolean

        fun updateSynced(synced: Boolean)

        @Deprecated("this logic should be moved to interactor")
        suspend fun updateBranches(branches: Branches, callback: UpdateBranchesCallback = UpdateBranchesCallback {})

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

        @Deprecated("use data from flowable")
        interface SyncCallback: UpdateBranchesCallback, UpdateMRCallback {
            companion object Empty : SyncCallback {
                override fun onBranchEvent(branchEvent: UpdateBranchesCallback.BranchEvent) = Unit

                override fun onMREvent(event: UpdateMRCallback.MREvent) = Unit

            }
        }
    }
}