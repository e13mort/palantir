package com.e13mort.gitlab_report.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.gitlab4j.api.Constants
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.MergeRequestApi
import org.gitlab4j.api.RepositoryApi
import org.gitlab4j.api.models.Assignee
import org.gitlab4j.api.models.MergeRequestFilter
import java.util.*
import org.gitlab4j.api.models.Branch as GLBranch
import org.gitlab4j.api.models.MergeRequest as GLMergeRequest

class GitlabProjectsRepository(
    url: String,
    key: String
) : ProjectRepository {

    private val gitLabApi = GitLabApi(url, key)

    @ExperimentalCoroutinesApi
    override suspend fun projects(): Flow<Project> {
        return flow {
            gitLabApi.projectApi.memberProjects.map {
                GitlabProject(it, gitLabApi.repositoryApi, gitLabApi.mergeRequestApi)
            }.forEach {
                emit(it)
            }
        }
    }

    override suspend fun findProject(id: Long): Project {
        return GitlabProject(
            gitLabApi.projectApi.getProject(id.toInt()),
            gitLabApi.repositoryApi,
            gitLabApi.mergeRequestApi
        )
    }

    override suspend fun projectsCount(): Long {
        throw UnsupportedRepositoryOperationException("projectsCount")
    }

    override suspend fun addProject(project: Project): Unit =
        throw UnsupportedRepositoryOperationException("addProject")

    override suspend fun clear(): Unit = throw UnsupportedRepositoryOperationException("clear")
}

internal class GitlabProject(
    private val project: org.gitlab4j.api.models.Project,
    private val repositoryApi: RepositoryApi,
    private val mergeRequestApi: MergeRequestApi
) : Project {
    override fun id(): String {
        return project.id.toString()
    }

    override fun name(): String {
        return project.name
    }

    override fun branches(): Branches {
        return GitlabBranches(repositoryApi, project.id)
    }

    override fun mergeRequests(): MergeRequests {
        return GitlabMergeRequests(mergeRequestApi, this)
    }
}

internal class GitlabMergeRequests(
    private val mergeRequestApi: MergeRequestApi,
    private val gitlabProject: GitlabProject
) : MergeRequests {
    override suspend fun project(): Project {
        return gitlabProject
    }

    override suspend fun count(): Long {
        return mergeRequestApi.getMergeRequests(createFilter()).size.toLong()
    }

    override suspend fun values(): Flow<MergeRequest> {
        return flow {
            mergeRequestApi.getMergeRequests(createFilter()).forEach {
                emit(GitlabMergeRequest(it))
            }
        }
    }

    private fun createFilter() = MergeRequestFilter()
        .withProjectId(gitlabProject.id().toInt())
        .withCreatedAfter(calculateMRStartDate())

    private fun calculateMRStartDate() = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
    }.time

    internal class GitlabMergeRequest(private val mergeRequest: GLMergeRequest) : MergeRequest {
        override fun id(): String {
            return mergeRequest.id.toString()
        }

        override fun state(): MergeRequest.State {
            return when (Constants.MergeRequestState.forValue(mergeRequest.state)) {
                Constants.MergeRequestState.OPENED -> MergeRequest.State.OPEN
                Constants.MergeRequestState.CLOSED -> MergeRequest.State.CLOSED
                Constants.MergeRequestState.MERGED -> MergeRequest.State.MERGED
                else -> MergeRequest.State.OPEN //fix with some unknown constant
            }
        }

        override fun sourceBranch(): Branch {
            return GitlabBranches.GitlabBranch(GLBranch().withName(mergeRequest.sourceBranch))
        }

        override fun targetBranch(): Branch {
            return GitlabBranches.GitlabBranch(GLBranch().withName(mergeRequest.targetBranch))
        }

        override fun createdTime(): Long {
            return mergeRequest.createdAt.time
        }

        override fun closedTime(): Long? {
            return when (Constants.MergeRequestState.forValue(mergeRequest.state)) {
                Constants.MergeRequestState.CLOSED -> mergeRequest.closedAt?.time
                Constants.MergeRequestState.MERGED -> mergeRequest.mergedAt?.time
                else -> null
            }
        }

        override fun assignees(): List<User> {
            return mergeRequest.assignees.map {
                GitlabUser(it)
            }
        }
    }

}

internal class GitlabUser(private val assignee: Assignee) : User {
    override fun name(): String {
        return assignee.name
    }

    override fun userName(): String {
        return assignee.username
    }

    override fun id(): Long {
        return assignee.id.toLong()
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

    internal class GitlabBranch(private val branch: GLBranch) : Branch {
        override fun name(): String {
            return branch.name
        }

    }

}