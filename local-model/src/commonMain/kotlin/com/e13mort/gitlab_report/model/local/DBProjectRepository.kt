package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.Project
import com.e13mort.gitlab_report.model.ProjectRepository
import com.e13mort.gitlabreport.model.local.DBProject
import com.e13mort.gitlabreport.model.local.ProjectQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class DBProjectRepository(private val projectQueries: ProjectQueries) : ProjectRepository {

    override suspend fun projects(): Flow<Project> {
        return projectQueries.selectAll { id, name ->
            DataObjectImpl(id.toString(), name)
        }.executeAsList().asFlow()
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

