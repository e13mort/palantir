package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Branch
import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.model.User
import com.e13mort.palantir.model.local.DBMergeRequestRepository
import com.e13mort.palantir.model.local.DBNotesRepository
import com.e13mort.palantir.model.local.DBProjectRepository
import com.e13mort.palantir.model.local.DriverFactory
import com.e13mort.palantir.model.local.DriverType
import com.e13mort.palantir.model.local.LocalModel
import com.e13mort.palantir.repository.ProjectRepository
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

//this tests uses local-model module so it has to be placed higher in module hierarchy
class SyncInteractorTest {
    private val localComponent = LocalComponent(inMemoryModel())
    private val repositoryBuilder = RemoteProjectRepositoryBuilder().also {
        it.addBaseTestProject()
    }

    @Before
    fun setUp() = runTest {
        val scanInteractor = ScanProjectsInteractor(
            localComponent.projectRepository,
            repositoryBuilder.build()
        )
        scanInteractor.run()
        localComponent.projectRepository.projects().collect {
            it.updateSynced(true)
        }
    }

    @Test
    fun `sync a single remote project leads to a single updated project`() = runTest {
        createSyncInteractor().run() should {
            it.projectsUpdated shouldBe 1
        }
    }

    @Test
    fun `sync a single remote project leads to a single local project`() = runTest {
        createSyncInteractor().run()
        localComponent.projectRepository.projectsCount() shouldBe 1
    }

    @Test
    fun `sync leads to correct local mr info`() = runTest {
        createSyncInteractor().run()
        localComponent.mrRepository.mergeRequest(1L) should {
            assertNotNull(it) // keeps nullability info
            it.assignees() shouldMatchEach listOf(matchUser(1L, "StubUser1", "StubUser1"))
            it.state() shouldBe MergeRequest.State.OPEN
        }
    }

    @Test
    fun `second sync with removed mr leads to null local mr`() = runTest {
        createSyncInteractor().run()
        repositoryBuilder.project(1) {
            removeMr(1L)
        }
        createSyncInteractor().run()
        localComponent.mrRepository.mergeRequest(1L) shouldBe null
    }

    @Test
    fun `second sync leads to correct local mr info`() = runTest {
        createSyncInteractor().run()
        repositoryBuilder.project(1L) {
            mr(1L) {
                assignees {
                    addStubUser()
                }
            }
        }
        createSyncInteractor().run()
        localComponent.mrRepository.mergeRequest(1L) should {
            assertNotNull(it) // keeps nullability info
            it.assignees() shouldMatchEach listOf(
                matchUser(1L, "StubUser1", "StubUser1"),
                matchUser(2L, "StubUser2", "StubUser2")
            )
            it.state() shouldBe MergeRequest.State.OPEN
        }
    }

    @Test
    fun `sync project leads to two local event notes`() = runTest {
        createSyncInteractor().run()
        localComponent.notesRepository.events(1L, 1L) shouldMatchEach listOf(
            matchEvent(
                MergeRequestEvent.Type.DISCUSSION,
                matchUser(1L, "StubUser1", "StubUser1")
            ), matchEvent(
                MergeRequestEvent.Type.APPROVE,
                matchUser(1L, "StubUser1", "StubUser1")
            )
        )
    }

    @Test
    fun `second sync project leads to new local event notes`() = runTest {
        createSyncInteractor().run()
        repositoryBuilder.project(1L) {
            mr(1L) {
                events {
                    event {
                        stubUser(2)
                        type = MergeRequestEvent.Type.APPROVE
                        content = "approve"
                    }
                }
            }
        }
        createSyncInteractor().run()
        localComponent.notesRepository.events(1L, 1L) shouldMatchEach listOf(
            matchEvent(
                MergeRequestEvent.Type.DISCUSSION,
                matchUser(1L, "StubUser1", "StubUser1")
            ), matchEvent(
                MergeRequestEvent.Type.APPROVE,
                matchUser(1L, "StubUser1", "StubUser1")
            ), matchEvent(
                MergeRequestEvent.Type.APPROVE,
                matchUser(2L, "StubUser2", "StubUser2")
            )
        )
    }

    @Test
    fun `sync updates branches info`() = runTest {
        createSyncInteractor().run()
        val project = localComponent.projectRepository.findProject(1L)!!
        project.branches().values().toList() shouldMatchEach listOf(
            matchBranch("master"),
            matchBranch("dev")
        )
    }

    @Test
    fun `sync a single remote project updates mr branches info`() = runTest {
        createSyncInteractor().run()
        val mr = localComponent.mrRepository.mergeRequest(1L)!!
        mr.sourceBranch() should matchBranch("dev")
        mr.targetBranch() should matchBranch("master")
    }

    @Test
    fun `second sync updates mr branches info`() = runTest {
        createSyncInteractor().run()
        repositoryBuilder.project(1L) {
            mr(1L) {
                sourceBranch = "dev2"
                targetBranch = "master2"
            }
        }
        createSyncInteractor().run()
        val mr = localComponent.mrRepository.mergeRequest(1L)!!
        mr.sourceBranch() should matchBranch("dev2")
        mr.targetBranch() should matchBranch("master2")
    }

    @Test
    fun `second sync updates branches info`() = runTest {
        createSyncInteractor().run()
        repositoryBuilder.project(1L) {
            branches {
                add("dev2")
                add("master2")
            }
        }
        createSyncInteractor().run()
        val project = localComponent.projectRepository.findProject(1L)!!
        project.branches().values().toList() shouldMatchEach listOf(
            matchBranch("dev2"),
            matchBranch("master2")
        )
    }

    private fun matchBranch(branchName: String): (Branch) -> Unit = {
        it.name() shouldBe branchName
    }

    private fun matchEvent(
        type: MergeRequestEvent.Type, userMatcher: (User) -> Unit
    ): (MergeRequestEvent) -> Unit = { event ->
        event.type() shouldBe type
        event.user() should userMatcher
    }

    @Suppress("SameParameterValue")
    private fun matchUser(id: Long, name: String, userName: String): (User) -> Unit = {
        it.id() shouldBe id
        it.name() shouldBe name
        it.userName() shouldBe userName
    }

    private fun createSyncInteractor(
        sourceRepository: ProjectRepository = repositoryBuilder.build(),
        targetRepository: SyncableProjectRepository = localComponent.projectRepository
    ) = SyncInteractor(
        projectRepository = targetRepository,
        remoteRepository = sourceRepository,
        mergeRequestRepository = localComponent.mrRepository,
        mergeRequestNotesRepository = localComponent.notesRepository
    )

    class LocalComponent(model: LocalModel) {
        val projectRepository = DBProjectRepository(model)
        val mrRepository = DBMergeRequestRepository(model)
        val notesRepository = DBNotesRepository(model)
    }

    private fun inMemoryModel(): LocalModel {
        val driver = DriverFactory("").createDriver(type = DriverType.MEMORY)
        return LocalModel(driver)
    }
}
