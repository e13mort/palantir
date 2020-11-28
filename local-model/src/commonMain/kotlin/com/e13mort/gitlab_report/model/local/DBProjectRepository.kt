package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.Project
import com.e13mort.gitlab_report.model.SyncableProjectRepository
import com.e13mort.gitlabreport.model.local.DBProject
import com.e13mort.gitlabreport.model.local.ProjectSyncQueries
import com.e13mort.gitlabreport.model.local.SYNCED_PROJECTS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map

class DBProjectRepository(localModel: LocalModel) : SyncableProjectRepository {

    private val projectQueries = localModel.projectQueries
    private val projectSyncQueries = localModel.projectSyncQueries

    override suspend fun projects(): Flow<SyncableProjectRepository.SyncableProject> {
        return projectSyncQueries.selectAll()
            .executeAsList().asFlow().map {
                SyncableProjectImpl(it, projectSyncQueries)
            }
    }

    override suspend fun findProject(id: Long): SyncableProjectRepository.SyncableProject? {
        return projectQueries.findProject(id).executeAsOneOrNull()?.toSyncable(projectSyncQueries)
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
    private val syncQueries: ProjectSyncQueries
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

    override fun id(): String {
        return dbItem.id.toString()
    }

    override fun name(): String {
        return dbItem.name
    }

}

fun DBProject.toSyncable(syncQueries: ProjectSyncQueries): SyncableProjectRepository.SyncableProject {
    return SyncableProjectImpl(SYNCED_PROJECTS(
        this.id, this.name, 0
    ), syncQueries)
}

