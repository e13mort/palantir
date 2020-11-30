package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.Branch
import com.e13mort.gitlab_report.model.Branches
import com.e13mort.gitlab_report.model.Project
import com.e13mort.gitlab_report.model.SyncableProjectRepository
import com.e13mort.gitlabreport.model.local.BranchesQueries
import com.e13mort.gitlabreport.model.local.DBProject
import com.e13mort.gitlabreport.model.local.ProjectSyncQueries
import com.e13mort.gitlabreport.model.local.SYNCED_PROJECTS
import kotlinx.coroutines.flow.*

class DBProjectRepository(localModel: LocalModel) : SyncableProjectRepository {

    private val projectQueries = localModel.projectQueries
    private val projectSyncQueries = localModel.projectSyncQueries
    private val branchesQueries = localModel.branchesQueries

    override suspend fun projects(): Flow<SyncableProjectRepository.SyncableProject> {
        return projectSyncQueries.selectAll()
            .executeAsList().asFlow().map {
                SyncableProjectImpl(it, projectSyncQueries, branchesQueries)
            }
    }

    override suspend fun findProject(id: Long): SyncableProjectRepository.SyncableProject? {
        return projectQueries.findProject(id).executeAsOneOrNull()?.toSyncable(projectSyncQueries, branchesQueries)
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
    private val branchesQueries: BranchesQueries
    ) : SyncableProjectRepository.SyncableProject {
    override fun synced(): Boolean {
        return dbItem.synced != 0L
    }

    override fun updateSynced(synced: Boolean) {
        if (synced)
            syncQueries.setProjectIsSynced(dbItem.id)
        else
            syncQueries.removeSyncedProject(dbItem.id)
    }

    override suspend fun updateBranches(branches: Branches) {
        branches.values().collect {
            branchesQueries.insert(dbItem.id, it.name())
        }
    }

    override fun id(): String {
        return dbItem.id.toString()
    }

    override fun name(): String {
        return dbItem.name
    }

    override fun branches(): Branches {
        return object : Branches {
            override suspend fun count(): Long {
                return branchesQueries.branchesCount().executeAsOne()
            }

            override suspend fun values(): Flow<Branch> {
                return flow {
                    branchesQueries.selectAll().executeAsList().asFlow().map {
                        emit(object : Branch {
                            override fun name(): String {
                                return it.name
                            }

                        })
                    }
                }
            }

        }
    }

}

fun DBProject.toSyncable(syncQueries: ProjectSyncQueries, branchesQueries: BranchesQueries): SyncableProjectRepository.SyncableProject {
    return SyncableProjectImpl(SYNCED_PROJECTS(
        this.id, this.name, 0
    ), syncQueries, branchesQueries)
}

