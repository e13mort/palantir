package com.e13mort.palantir.interactors

import app.cash.turbine.test
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
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertNotNull

//this tests uses local-model module so it has to be placed higher in module hierarchy
class SyncInteractorTest {
    private val localComponent = LocalComponent(inMemoryModel())
    private val repositoryBuilder = RemoteProjectRepositoryBuilder().also {
        it.addBaseTestProject()
    }

    @Test
    fun `sync a single remote project leads to a single updated project`() = testSyncResult { syncResult ->
        syncResult should {
            (it.state as SyncInteractor.SyncResult.State.Done).itemsUpdated shouldBe 1
        }
    }

    @Test
    fun `sync a single remote project leads to a single local project`() = testSyncResult {
        localComponent.projectRepository.projectsCount() shouldBe 1
    }

    @Test
    fun `sync leads to correct local mr info`() = testSyncResult {
        localComponent.mrRepository.mergeRequest(1L) should {
            assertNotNull(it) // keeps nullability info
            it.assignees() shouldMatchEach listOf(matchUser(1L, "StubUser1", "StubUser1"))
            it.state() shouldBe MergeRequest.State.OPEN
        }
    }

    @Test
    fun `second sync with removed mr leads to null local mr`() = testSyncResult {
        repositoryBuilder.project(1) {
            removeMr(1L)
        }
        createSyncInteractor().run()
        localComponent.mrRepository.mergeRequest(1L) shouldBe null
    }

    @Test
    fun `second sync leads to correct local mr info`() = testSyncResult {
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
    fun `sync project leads to two local event notes`() = testSyncResult {
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
    fun `second sync project leads to new local event notes`() = testSyncResult {
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
    fun `sync updates branches info`() = testSyncResult {
        val project = localComponent.projectRepository.findProject(1L)!!
        project.branches().values().toList() shouldMatchEach listOf(
            matchBranch("master"),
            matchBranch("dev")
        )
    }

    @Test
    fun `sync a single remote project updates mr branches info`() = testSyncResult {
        val mr = localComponent.mrRepository.mergeRequest(1L)!!
        mr.sourceBranch() should matchBranch("dev")
        mr.targetBranch() should matchBranch("master")
    }

    @Test
    fun `second sync updates mr branches info`() = testSyncResult {
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
    fun `second sync updates branches info`() = testSyncResult {
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

    @Test
    fun `interactor with update strategy syncs one project`() = runTest {
        val interactorForProjectUpdate = createSyncInteractor()
        interactorForProjectUpdate.run(SyncInteractor.SyncStrategy.UpdateProjects).collect{}
        localComponent.projectRepository.projects().toList() shouldMatchEach listOf(
            matchProjectWithId(1)
        )
    }

    @Test
    fun `interactor with update strategy syncs two projects on second request`() = runTest {
        createSyncInteractor().run(SyncInteractor.SyncStrategy.UpdateProjects).collect{}
        repositoryBuilder.project { }
        repositoryBuilder.project { }
        repositoryBuilder.removeProject(1)
        createSyncInteractor().run(SyncInteractor.SyncStrategy.UpdateProjects).collect{}
        localComponent.projectRepository.projects().toList() shouldMatchEach listOf(
            matchProjectWithId(2),
            matchProjectWithId(3),
        )
    }

    @Test
    fun `interactor with update strategy removes missed project`() = runTest {
        repositoryBuilder.project { }
        createSyncInteractor().run(SyncInteractor.SyncStrategy.UpdateProjects).collect{}
        repositoryBuilder.removeProject(1)
        createSyncInteractor().run(SyncInteractor.SyncStrategy.UpdateProjects).collect{}
        localComponent.projectRepository.projects().toList() shouldMatchEach listOf(
            matchProjectWithId(2)
        )
    }

    private fun testSyncResult(prepare: Boolean = true, block: suspend (SyncInteractor.SyncResult) -> Unit) = runTest {
        val syncInteractor = createSyncInteractor()
        if (prepare)
            syncInteractor.prepareForTest()
        syncInteractor.run(SyncInteractor.SyncStrategy.FullSyncForActiveProjects).test {
            skipItems(1)
            val item = awaitItem()
            block(item)
            awaitComplete()
        }
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

    private fun matchProjectWithId(id: Long): (SyncableProjectRepository.SyncableProject) -> Unit =
        { it.id() shouldBe id.toString() }

    private suspend fun SyncInteractor.prepareForTest() {
        run(SyncInteractor.SyncStrategy.UpdateProjects).collect {  }
        localComponent.projectRepository.projects().collect {
            it.updateSynced(true)
        }
    }

    private suspend fun SyncInteractor.run(): SyncInteractor.SyncResult {
        return this.run(SyncInteractor.SyncStrategy.FullSyncForActiveProjects).toList()[1]
    }

    private fun createSyncInteractor(): SyncInteractor {
        return SyncInteractor(
            projectRepository = localComponent.projectRepository,
            remoteRepository = repositoryBuilder.build(),
            mergeRequestRepository = localComponent.mrRepository,
            mergeRequestNotesRepository = localComponent.notesRepository
        )
    }

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
