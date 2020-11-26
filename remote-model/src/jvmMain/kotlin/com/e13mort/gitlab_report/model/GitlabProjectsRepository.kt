package com.e13mort.gitlab_report.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.gitlab4j.api.GitLabApi

class GitlabProjectsRepository(
    private val url: String,
    private val key: String
    ): ProjectRepository {
    @ExperimentalCoroutinesApi
    override suspend fun projects(): Flow<Project> {
        return flow {
            val gitLabApi = GitLabApi(url, key)
            gitLabApi.projectApi.memberProjects.map {
                println("Collect project: ${it.name}")
                object : Project {
                    override fun id(): String {
                        return it.id.toString()
                    }

                    override fun name(): String {
                        return it.name
                    }
                }
            }.forEach {
                emit(it)
            }
        }
    }

    override suspend fun addProject(project: Project) {
        throw IllegalStateException()
    }

    override suspend fun clear() {
        throw IllegalStateException()
    }
}