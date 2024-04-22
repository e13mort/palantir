/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

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

    fun localId(): Long

    fun state(): State

    fun sourceBranch(): Branch

    fun targetBranch(): Branch

    fun createdTime(): Long

    fun closedTime(): Long?

    fun assignees(): List<User>

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

    suspend fun syncedProjects(): List<SyncableProject>

    interface SyncableProject : Project {
        fun synced(): Boolean

        fun updateSynced(synced: Boolean)

        @Deprecated("this logic should be moved to interactor")
        suspend fun updateBranches(branches: List<Branch>)

    }
}