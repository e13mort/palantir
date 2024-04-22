/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Branch
import com.e13mort.palantir.model.Branches
import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.User
import com.e13mort.palantir.model.stub.StubBranch
import com.e13mort.palantir.model.stub.StubBranches
import com.e13mort.palantir.model.stub.StubMergeRequest
import com.e13mort.palantir.model.stub.StubNoteRepository
import com.e13mort.palantir.model.stub.StubProject
import com.e13mort.palantir.model.stub.StubProjectRepository
import com.e13mort.palantir.model.stub.StubUser
import com.e13mort.palantir.repository.ProjectRepository

fun List<RemoteProjectRepositoryBuilder.StubProjectScope.StubMRScope>.asMrs(): List<MergeRequest> {
    return map { scope ->
        StubMergeRequest(
            id = scope.id.toString(),
            localId = scope.localId,
            state = scope.state,
            assignees = scope.assigneesScope.users.toList(),
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

class RemoteProjectRepositoryBuilder(
    private val mrIdGenerator: IdGenerator = IdGenerator(),
    private val mrNotesIdGenerator: IdGenerator = IdGenerator()
) {
    private val projectScopes = mutableListOf<StubProjectScope>()
    val stubNotesRepository = StubNoteRepository()

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
            StubProjectScope(
                id = newId,
                mrIdGenerator = mrIdGenerator,
                mrEventsIdGenerator = mrNotesIdGenerator
            ).also(config)
        )
    }

    private fun updateProject(id: Long, config: StubProjectScope.() -> Unit) {
        projectScopes.find { it.id == id }?.also(config)
            ?: throw IllegalStateException("Project with id $id doesn't exists")
    }

    fun build(): ProjectRepository {
        return StubProjectRepository(
            projectScopes.map { project ->
                stubNotesRepository.data[project.id] =
                    mutableMapOf<Long, MutableList<MergeRequestEvent>>().also { notesMap ->
                        project.mrs.forEach { mr ->
                            notesMap[mr.localId] = mutableListOf<MergeRequestEvent>().also {
                                it.addAll(mr.eventsScope.events)
                            }
                        }
                    }
                StubProject(
                    id = project.id.toString(),
                    mergeRequests = project.mrs.asMrs(),
                    branches = project.branchesScope.asBranches()
                )
            }.toMutableList()
        )
    }

    class StubProjectScope(
        var id: Long,
        val mrs: MutableList<StubMRScope> = mutableListOf(),
        var branchesScope: BranchesScope = BranchesScope(),
        val mrIdGenerator: IdGenerator,
        private val mrEventsIdGenerator: IdGenerator
    ) {

        class UserListScope {
            internal val users = mutableListOf<StubUser>()
            fun addStubUser() {
                val newId = users.lastOrNull()?.id()?.inc() ?: 1L
                users += StubUser(newId)
            }
        }

        class MREventsScope(private val idGenerator: IdGenerator) {
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
                    val newId = idGenerator.nextId()
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
            val id: Long,
            val localId: Long,
            var state: MergeRequest.State = MergeRequest.State.OPEN,
            mrEventsIdGenerator: IdGenerator
        ) {
            internal val assigneesScope = UserListScope()
            internal val eventsScope = MREventsScope(mrEventsIdGenerator)
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
            val newLocalId = mrs.lastOrNull()?.localId?.inc() ?: 1L
            val mrScope = StubMRScope(
                id = mrIdGenerator.nextId(),
                localId = newLocalId,
                mrEventsIdGenerator = mrEventsIdGenerator
            )
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