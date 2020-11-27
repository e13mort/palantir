package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.Project
import com.e13mort.gitlab_report.model.SyncableProjectRepository
import com.e13mort.gitlabreport.model.local.DBProject
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
                SyncableProjectImpl(it)
            }
    }

    override suspend fun findProject(id: Long): Project? {
        return projectQueries.findProject(id) { _id, _name ->
            DataObjectImpl(_id.toString(), _name)
        }.executeAsOneOrNull()
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
internal class DataObjectImpl(
    private val id: String,
    private val name: String
) : Project {
    override fun id(): String = id

    override fun name(): String = name
}

internal class SyncableProjectImpl(private val dbItem: SYNCED_PROJECTS) : SyncableProjectRepository.SyncableProject {
    override fun synced(): Boolean {
        return dbItem.synced != 0L
    }

    override fun id(): String {
        return dbItem.name
    }

    override fun name(): String {
        return dbItem.name
    }

}

