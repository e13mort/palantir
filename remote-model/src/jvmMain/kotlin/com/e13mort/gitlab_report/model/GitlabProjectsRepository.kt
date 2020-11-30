package com.e13mort.gitlab_report.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.RepositoryApi

class GitlabProjectsRepository(
    url: String,
    key: String
) : ProjectRepository {

    private val gitLabApi = GitLabApi(url, key)

    @ExperimentalCoroutinesApi
    override suspend fun projects(): Flow<Project> {
        return flow {
            gitLabApi.projectApi.memberProjects.map {
                GitlabProject(it, gitLabApi.repositoryApi)
            }.forEach {
                emit(it)
            }
        }
    }

    override suspend fun findProject(id: Long): Project {
        return GitlabProject(gitLabApi.projectApi.getProject(id.toInt()), gitLabApi.repositoryApi)
    }

    override suspend fun projectsCount(): Long {
        throw UnsupportedRepositoryOperationException("projectsCount")
    }

    override suspend fun addProject(project: Project): Unit =
        throw UnsupportedRepositoryOperationException("addProject")

    override suspend fun clear(): Unit = throw UnsupportedRepositoryOperationException("clear")
}

internal class GitlabProject(private val project: org.gitlab4j.api.models.Project,
                             private val repositoryApi: RepositoryApi) : Project {
    override fun id(): String {
        return project.id.toString()
    }

    override fun name(): String {
        return project.name
    }

    override fun branches(): Branches {
        return GitlabBranches(repositoryApi, project.id)
    }
}

internal class GitlabBranches(private val repositoryApi: RepositoryApi, private val id: Int) : Branches {
    override suspend fun count(): Long {
        return repositoryApi.getBranches(id).size.toLong()
    }

    override suspend fun values(): Flow<Branch> {
        return flow {
            repositoryApi.getBranches(id).forEach {
                emit(GitlabBranch(it))
            }
        }
    }

    internal class GitlabBranch(private val branch: org.gitlab4j.api.models.Branch) : Branch {
        override fun name(): String {
            return branch.name
        }

    }

}