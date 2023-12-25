package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Branch
import com.e13mort.palantir.model.Branches
import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.User
import com.e13mort.palantir.model.stub.StubBranch
import com.e13mort.palantir.model.stub.StubBranches
import com.e13mort.palantir.model.stub.StubMergeRequest
import com.e13mort.palantir.model.stub.StubProject
import com.e13mort.palantir.model.stub.StubProjectRepository
import com.e13mort.palantir.model.stub.StubUser
import com.e13mort.palantir.repository.ProjectRepository

fun List<RemoteProjectRepositoryBuilder.StubProjectScope.StubMRScope>.asMrs(): List<MergeRequest> {
    return map { scope ->
        StubMergeRequest(
            id = scope.id.toString(),
            state = scope.state,
            assignees = scope.assigneesScope.users.toList(),
            events = scope.eventsScope.events.toList(),
            sourceBranch = scope.sourceBranch.asBranch(),
            targetBranch = scope.targetBranch.asBranch()
        )
    }
}

fun RemoteProjectRepositoryBuilder.StubProjectScope.BranchesScope.asBranches(): Branches {
    return StubBranches(branches)
}

fun String.asBranch(): Branch {
    return StubBranch(this)
}

class RemoteProjectRepositoryBuilder {
    private val projectScopes = mutableListOf<StubProjectScope>()

    fun project(id: Long? = null, config: StubProjectScope.() -> Unit) {
        if (id == null) {
            createProject(config)
        } else {
            updateProject(id, config)
        }
    }

    fun removeProject(id: Long) {
        projectScopes.removeIf { it.id == id }
    }

    private fun createProject(config: StubProjectScope.() -> Unit) {
        val newId = (projectScopes.lastOrNull()?.id?.inc()) ?: 1L
        projectScopes.add(
            StubProjectScope(id = newId).also(config)
        )
    }

    private fun updateProject(id: Long, config: StubProjectScope.() -> Unit) {
        projectScopes.find { it.id == id }?.also(config)
            ?: throw IllegalStateException("Project with id $id doesn't exists")
    }

    fun build(): ProjectRepository {
        return StubProjectRepository(
            projectScopes.map {
                StubProject(
                    id = it.id.toString(),
                    mergeRequests = it.mrs.asMrs(),
                    branches = it.branchesScope.asBranches()
                )
            }.toMutableList()
        )
    }

    class StubProjectScope(
        var id: Long,
        val mrs: MutableList<StubMRScope> = mutableListOf(),
        var branchesScope: BranchesScope = BranchesScope()
    ) {

        class UserListScope {
            internal val users = mutableListOf<StubUser>()
            fun addStubUser() {
                val newId = users.lastOrNull()?.id()?.inc() ?: 1L
                users += StubUser(newId)
            }
        }

        class MREventsScope {
            class MREventScope(private val id: Long) : MergeRequestEvent {
                var type: MergeRequestEvent.Type = MergeRequestEvent.Type.GENERAL_NOTE
                private var user: StubUser = StubUser(1L)
                var content = "TestContent"
                var time = 0L
                override fun id(): Long = id

                override fun type(): MergeRequestEvent.Type = type

                override fun timeMillis(): Long = time

                override fun user(): User = user

                override fun content(): String = content

                fun stubUser(id: Long) {
                    user = StubUser(id)
                }
            }

            internal val events = mutableListOf<MREventScope>()
            fun event(id: Long? = null, config: MREventScope.() -> Unit) {
                if (id == null) {
                    val newId = events.maxByOrNull { it.id() }?.id()?.inc() ?: 1L
                    events += MREventScope(newId).also(config)
                } else {
                    events.first { it.id() == id }.also(config)
                }
            }
        }

        class BranchesScope {
            internal val branches = mutableListOf<StubBranch>()
            fun add(name: String) {
                branches += StubBranch(name)
            }
        }

        class StubMRScope(
            var id: Long,
            var state: MergeRequest.State = MergeRequest.State.OPEN,
        ) {
            internal val assigneesScope = UserListScope()
            internal val eventsScope = MREventsScope()
            var sourceBranch = "dev"
            var targetBranch = "master"
            fun assignees(config: UserListScope.() -> Unit) {
                config(assigneesScope)
            }

            fun events(config: MREventsScope.() -> Unit) {
                config(eventsScope)
            }
        }

        fun mr(id: Long? = null, config: StubMRScope.() -> Unit) {
            if (id == null) {
                createMr(config)
            } else {
                updateMR(id, config)
            }

        }

        private fun createMr(config: StubMRScope.() -> Unit) {
            val newId = mrs.lastOrNull()?.id?.inc() ?: 1L
            val mrScope = StubMRScope(id = newId)
            config(mrScope)
            mrs += mrScope
        }

        private fun updateMR(id: Long, config: StubMRScope.() -> Unit) {
            mrs.find { it.id == id }?.also(config)
                ?: throw IllegalStateException("MR with id $id should exists")
        }

        fun branches(config: BranchesScope.() -> Unit) {
            branchesScope = BranchesScope()
            config(branchesScope)
        }

        fun removeMr(id: Long) {
            mrs.removeIf { it.id == id }
        }
    }
}