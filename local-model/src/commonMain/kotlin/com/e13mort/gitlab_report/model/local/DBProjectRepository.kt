package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.*
import com.e13mort.gitlabreport.model.local.*
import kotlinx.coroutines.flow.*

class DBProjectRepository(localModel: LocalModel) : SyncableProjectRepository {

    private val projectQueries = localModel.projectQueries
    private val projectSyncQueries = localModel.projectSyncQueries
    private val branchesQueries = localModel.branchesQueries
    private val mergeRequestsQueries = localModel.mergeRequestsQueries

    override suspend fun projects(): Flow<SyncableProjectRepository.SyncableProject> {
        return projectSyncQueries.selectAll()
            .executeAsList().asFlow().map {
                SyncableProjectImpl(it, projectSyncQueries, branchesQueries, mergeRequestsQueries)
            }
    }

    override suspend fun findProject(id: Long): SyncableProjectRepository.SyncableProject? {
        return projectQueries.findProject(id).executeAsOneOrNull()?.toSyncable(projectSyncQueries, branchesQueries, mergeRequestsQueries)
    }

    override suspend fun syncedProjects(): Flow<SyncableProjectRepository.SyncableProject> {
        return projectSyncQueries.selectSynced()
            .executeAsList().asFlow().map {
                SyncableProjectImpl(it, projectSyncQueries, branchesQueries, mergeRequestsQueries)
            }
    }

    override suspend fun projectsCount(): Long {
        return projectQueries.projectsCount().executeAsOne()
    }

    override suspend fun addProject(project: Project) {
        projectQueries.insert(DBProject(project.id().toLong(), project.name()))
    }

    override suspend fun clear() {
        projectQueries.clear()
    }

}

internal class SyncableProjectImpl(
    private val dbItem: SYNCED_PROJECTS,
    private val syncQueries: ProjectSyncQueries,
    private val branchesQueries: BranchesQueries,
    private val mergeRequestsQueries: MergeRequestsQueries
    ) : SyncableProjectRepository.SyncableProject {
    override fun synced(): Boolean {
        return dbItem.synced != 0L
    }

    override fun updateSynced(synced: Boolean) {
        if (synced)
            syncQueries.setProjectIsSynced(projectId())
        else
            syncQueries.removeSyncedProject(projectId())
    }

    override suspend fun updateBranches(branches: Branches) {
        branchesQueries.removeProjectsBranches(projectId())
        branches.values().collect {
            branchesQueries.insert(projectId(), it.name())
        }
    }

    override suspend fun updateMergeRequests(mergeRequests: MergeRequests) {
        mergeRequestsQueries.removeProjectsMergeRequests(projectId())
        mergeRequests.values().collect {
            mergeRequestsQueries.insert(
                mergeRequests.project().id().toLong(),
                it.id().toLong(),
                it.state().ordinal.toLong(),
                it.sourceBranch().name(),
                it.targetBranch().name(),
                it.createdTime()
            )
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
                return mergeRequestsQueries.selectAll(projectId()) { id: Long, state: Long, source_branch_name: String?, target_branch_name: String?, created_time: Long?, project_id ->
                    return@selectAll DBMergeRequest(id, state, source_branch_name, target_branch_name, created_time)
                }.executeAsList().asFlow()
            }

        }
    }

    private fun projectId() = dbItem.id

}

internal data class DBBranch(private val name: String) : Branch {
    override fun name(): String = name
}

internal const val UNSPECIFIED_BRANCH_NAME = "unspecified"

internal data class DBMergeRequest(val id: Long, val state: Long, val source_branch_name: String?, val target_branch_name: String?, val created_time: Long?) : MergeRequest {
    override fun id(): String = id.toString()

    override fun state(): MergeRequest.State = MergeRequest.State.values()[state.toInt()]

    override fun sourceBranch(): Branch = DBBranch(source_branch_name ?: UNSPECIFIED_BRANCH_NAME)

    override fun targetBranch(): Branch = DBBranch(target_branch_name ?: UNSPECIFIED_BRANCH_NAME)

    override fun createdTime(): Long = created_time ?: 0
}

fun DBProject.toSyncable(syncQueries: ProjectSyncQueries, branchesQueries: BranchesQueries, mergeRequestsQueries: MergeRequestsQueries): SyncableProjectRepository.SyncableProject {
    return SyncableProjectImpl(SYNCED_PROJECTS(
        this.id, this.name, 0
    ), syncQueries, branchesQueries, mergeRequestsQueries)
}

