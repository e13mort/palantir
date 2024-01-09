package com.e13mort.palantir.interactors

import com.e13mort.palantir.interactors.SyncInteractor.SyncResult.State
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
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.collections.shouldMatchInOrderSubset
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldMatchAll
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
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
            (it.state as State.Done).itemsUpdated shouldBe 1
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

    @Test
    fun `sync process emits correct high level states`() = runTestWithResultList { syncEvents ->
        syncEvents shouldMatchInOrderSubset listOf(
            matchSyncState(State.Pending),
            matchSyncState(State.InProgress(State.ProgressState.COMPLEX)),
            matchSyncState(State.Done(1))
        )
    }

    @Test
    fun `sync process emits correct branch sync states`() = runTestWithResultList { syncEvents ->
        syncEvents shouldMatchInOrderSubset listOf(
            projectsSyncResultsExists(1),
            matchBranchesState(1, State.Pending),
            matchBranchesState(1, State.InProgress(State.ProgressState.LOADING)),
            matchBranchesState(1, State.InProgress(State.ProgressState.SAVING)),
            matchBranchesState(1, State.Done(2)),
        )
    }

    @Test
    fun `sync process emits correct MR sync states`() = runTestWithResultList { syncEvents ->
        syncEvents shouldMatchInOrderSubset listOf(
            projectsSyncResultsExists(1),
            matchMRsState(1, State.Pending),
            matchMRsState(1, State.InProgress(State.ProgressState.LOADING)),
            matchMRsState(1, State.InProgress(State.ProgressState.SAVING)),
            matchMRsState(1, State.Done(1)),
        )
    }

    @Test
    fun `sync process emits correct MR notes sync states`() = runTestWithResultList { syncEvents ->
        syncEvents shouldMatchInOrderSubset listOf(
            projectsSyncResultsExists(1),
            mrSyncResultsPending(1, 1),
            matchMRNotesState(1, 1, State.InProgress(State.ProgressState.LOADING)),
            matchMRNotesState(1, 1, State.InProgress(State.ProgressState.SAVING)),
            matchMRNotesState(1, 1, State.Done(1)),
        )
    }

    @Test
    fun `sync process emits correct MR notes sync states for incremental sync with new merged mr`() = runTest {
        repositoryBuilder.project(1) {
            mr {
                events {
                    event {
                        type = MergeRequestEvent.Type.APPROVE
                        content = "approved"
                    }
                }
                state = MergeRequest.State.MERGED
            }
        }
        val interactor = createSyncInteractor()
        interactor.prepareForTest()
        val firstSyncEvents = interactor.run(SyncInteractor.SyncStrategy.FullSyncForProject(1)).toList()
        firstSyncEvents shouldMatchInOrderSubset listOf(
            projectsSyncResultsExists(1),
            mrSyncResultsPending(1, 1),
            matchMRNotesState(1, 1, State.InProgress(State.ProgressState.LOADING)),
            matchMRNotesState(1, 1, State.InProgress(State.ProgressState.SAVING)),
            matchMRNotesState(1, 1, State.Done(1)),
            matchMRNotesState(1, 2, State.InProgress(State.ProgressState.LOADING)),
            matchMRNotesState(1, 2, State.InProgress(State.ProgressState.SAVING)),
            matchMRNotesState(1, 2, State.Done(1)),
        )
        val incrementalSyncEvents = interactor.run(SyncInteractor.SyncStrategy.FullSyncForProject(1)).toList()
        incrementalSyncEvents shouldMatchInOrderSubset listOf(
            projectsSyncResultsExists(1),
            mrSyncResultsPending(1, 1),
            matchMRNotesState(1, 1, State.InProgress(State.ProgressState.LOADING)),
            matchMRNotesState(1, 1, State.InProgress(State.ProgressState.SAVING)),
            matchMRNotesState(1, 1, State.Done(1)),
            matchMRNotesState(1, 2, State.Skipped),
        )
    }

    @Test
    fun `sync process emits correct MR notes sync states for incremental sync with skipped state`() = runTest {
        repositoryBuilder.project(1) {
            mr(1) {
                state = MergeRequest.State.MERGED
            }
            mr {
                state = MergeRequest.State.MERGED
                events {
                    event {
                        type = MergeRequestEvent.Type.APPROVE
                        content = "approved"
                    }
                }
            }
            mr {
                state = MergeRequest.State.MERGED
            }
        }
        val interactor = createSyncInteractor()
        interactor.prepareForTest()
        interactor.run(SyncInteractor.SyncStrategy.FullSyncForProject(1, true)).collect()

        val incrementalSyncEvents = interactor.run(SyncInteractor.SyncStrategy.FullSyncForProject(1, false)).toList()
        incrementalSyncEvents shouldMatchInOrderSubset listOf(
            projectsSyncResultsExists(1),
            mrSyncResultsSkipped(1, setOf(1, 2, 3)),
        )
    }

    @Test
    fun `incremental sync process success for two projects with mrs`() = runTest {
        repositoryBuilder.project {
            mr { }
        }
        val interactor = createSyncInteractor()
        interactor.prepareForTest()
        interactor.run(SyncInteractor.SyncStrategy.FullSyncForActiveProjects(true)).collect()
        interactor.run(SyncInteractor.SyncStrategy.FullSyncForActiveProjects()).collect()
    }

    @Test
    fun `force sync process success for two projects with mrs and events`() = runTest {
        repositoryBuilder.removeProject(1)
        repositoryBuilder.project {
            mr {
                state = MergeRequest.State.MERGED
            }
            mr {
                state = MergeRequest.State.MERGED
            }
            mr {
                state = MergeRequest.State.MERGED
            }
        }
        val interactor = createSyncInteractor()
        interactor.prepareForTest()
        interactor.run(SyncInteractor.SyncStrategy.FullSyncForProject(1,true)).collect()
        interactor.run(SyncInteractor.SyncStrategy.FullSyncForProject(1,false)).collect()
    }

    @Test
    fun `force sync process success for two projects with mrs and events2`() = runTest {
        repositoryBuilder.project(1) {
            mr {
                events {
                    event { }
                }
            }
            mr {
                events {
                    event { }
                    event { }
                    event { }
                }
            }
        }
        val interactor = createSyncInteractor()
        interactor.run(SyncInteractor.SyncStrategy.UpdateProjects).collect()
        interactor.run(SyncInteractor.SyncStrategy.FullSyncForProject(1, true)).collect()
        val mrsForTestProject = localComponent.mrRepository.mergeRequestsForProject(1)
        mrsForTestProject shouldHaveSize 3
        localComponent.notesRepository.events(1, 1) shouldHaveSize 2
        localComponent.notesRepository.events(1, 2) shouldHaveSize 1
        localComponent.notesRepository.events(1, 3) shouldHaveSize 3
    }

    @Test
    fun `next incremental sync removes mr`() = runTest {
        createSyncInteractor().also { it.prepareForTest() }.run()
        repositoryBuilder.project(1L) {
            removeMr(1L)
        }
        createSyncInteractor().also { it.prepareForTest() }.run()
        localComponent.mrRepository.mergeRequestsForProject(1) shouldHaveSize 0
    }

    @Test
    fun `next incremental sync adds mr with correct states`() = runTest {
        createSyncInteractor().also { it.prepareForTest() }.run()
        repositoryBuilder.project(1L) {
             mr {
                 state = MergeRequest.State.MERGED
             }
        }
        repositoryBuilder.project {
            mr { state = MergeRequest.State.CLOSED }
        }
        createSyncInteractor().also { it.prepareForTest() }.run()
        localComponent.mrRepository.mergeRequestsForProject(1) shouldMatchEach listOf(
            { mr ->
                mr.id().toLong() shouldBe 1
                mr.localId() shouldBe 1
                mr.state() shouldBe MergeRequest.State.OPEN
            },
            { mr ->
                mr.id().toLong() shouldBe 2
                mr.localId() shouldBe 2
                mr.state() shouldBe MergeRequest.State.MERGED
            },
        )
        localComponent.mrRepository.mergeRequestsForProject(2) shouldMatchEach listOf { mr ->
            mr.id().toLong() shouldBe 3
            mr.localId() shouldBe 1
            mr.state() shouldBe MergeRequest.State.CLOSED
        }
    }

    private fun runTestWithResultList(block: (List<SyncInteractor.SyncResult>) -> Unit) = runTest {
        val interactor = createSyncInteractor()
        interactor.prepareForTest()
        val syncResults = interactor.run(SyncInteractor.SyncStrategy.FullSyncForProject(1)).toList()
        block(syncResults)
    }

    private fun projectsSyncResultsExists(projectId: Long): (SyncInteractor.SyncResult) -> Unit =
        { it.projects shouldContainKey projectId }

    private fun mrSyncResultsPending(projectId: Long, mrId: Long): (SyncInteractor.SyncResult) -> Unit =
        {
            it.projects shouldContainKey projectId
            it.projects[projectId]!!.mrs.mergeRequests shouldContainKey mrId
            it.projects[projectId]!!.mrs.mergeRequests[mrId]!! should matchState(State.Pending)
        }

    private fun mrSyncResultsSkipped(
        projectId: Long,
        mrId: Set<Long>
    ): (SyncInteractor.SyncResult) -> Unit =
        { syncResult ->
            syncResult.projects shouldContainKey projectId
            syncResult.projects[projectId]!!.mrs.mergeRequests.keys shouldContainAll mrId
            syncResult.projects[projectId]!!.mrs.mergeRequests shouldMatchAll mrId.associateWith { { it shouldBe State.Skipped } }
        }


    private fun matchBranchesState(projectId: Long, state: State): (SyncInteractor.SyncResult) -> Unit {
        return { syncResult ->
            syncResult.projects[projectId]!!.branchesState should matchState(state)
        }
    }

    private fun matchMRsState(projectId: Long, state: State): (SyncInteractor.SyncResult) -> Unit {
        return { syncResult ->
            syncResult.projects[projectId]!!.mrs.state should matchState(state)
        }
    }

    private fun matchMRNotesState(projectId: Long, mrId: Long, state: State): (SyncInteractor.SyncResult) -> Unit {
        return { syncResult ->
            syncResult.projects[projectId]!!.mrs.mergeRequests[mrId]!! should matchState(state)
        }
    }

    private fun testSyncResult(prepare: Boolean = true, block: suspend (SyncInteractor.SyncResult) -> Unit) = runTest {
        val syncInteractor = createSyncInteractor()
        if (prepare)
            syncInteractor.prepareForTest()
        val syncResultFlow = syncInteractor.run(SyncInteractor.SyncStrategy.FullSyncForActiveProjects())
        val results = syncResultFlow.toList()
        block(results.last())
    }

    private fun matchSyncState(state: State): (SyncInteractor.SyncResult) -> Unit = {
        it.state should matchState(state)
    }

    private fun matchState(state: State): (State) -> Unit =
        { it shouldBe state }

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
        return this.run(SyncInteractor.SyncStrategy.FullSyncForActiveProjects()).toList()[1]
    }

    private fun createSyncInteractor(): SyncInteractor {
        return SyncInteractor(
            projectRepository = localComponent.projectRepository,
            remoteRepository = repositoryBuilder.build(),
            mergeRequestRepository = localComponent.mrRepository,
            mergeRequestLocalNotesRepository = localComponent.notesRepository,
            mergeRequestRemoteNotesRepository = repositoryBuilder.stubNotesRepository,
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
