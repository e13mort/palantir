package com.e13mort.gitlab_report.model

import kotlinx.coroutines.flow.Flow

interface Project {
    fun id(): String

    fun name(): String

    fun branches(): Branches

    fun mergeRequests(): MergeRequests
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

    enum class State {
        OPEN, MERGED, CLOSED
    }
}

interface Branch {
    fun name(): String
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
}

interface SyncableProjectRepository : ProjectRepository {
    override suspend fun projects(): Flow<SyncableProject>

    override suspend fun findProject(id: Long): SyncableProject?

    suspend fun syncedProjects(): Flow<SyncableProject>

    interface SyncableProject : Project {
        fun synced(): Boolean

        fun updateSynced(synced: Boolean)

        suspend fun updateBranches(branches: Branches)

        suspend fun updateMergeRequests(mergeRequests: MergeRequests)
    }
}