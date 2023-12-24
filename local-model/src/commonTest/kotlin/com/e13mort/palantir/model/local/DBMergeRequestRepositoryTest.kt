package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.stub.StubMergeRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DBMergeRequestRepositoryTest {

    private val testModel = TestModel()
    private val repository = DBMergeRequestRepository(testModel.model)
    @Test
    fun `empty repository returns empty MR`() = runTest {
        assertNull(repository.mergeRequest(TEST_PROJECT_ID))
    }

    @Test
    fun `invalid project leads to failed save operation`() = runTest {
        assertFailsWith<Exception> {
            repository.saveMergeRequests(
                TEST_PROJECT_ID, listOf(
                StubMergeRequest("1")
            ))
        }
    }

    @Test
    fun `insert with valid project id`() = runTest {
        testModel.prepareTestProject()
        repository.saveMergeRequests(TEST_PROJECT_ID, listOf(StubMergeRequest("1")))
    }

    @Test
    fun `inserted MR returns not null value`() = runTest {
        testModel.prepareTestProject()
        repository.saveMergeRequests(TEST_PROJECT_ID, listOf(StubMergeRequest("1")))
        assertNotNull(repository.mergeRequest(1L))
    }

    @Test
    fun `created MR deletes after host project removal`() = runTest {
        testModel.prepareTestProject()
        repository.saveMergeRequests(TEST_PROJECT_ID, listOf(StubMergeRequest("1")))
        testModel.model.projectQueries.clear()
        assertNull(repository.mergeRequest(TEST_PROJECT_ID))
    }

    @Test
    fun `created MRs deletes after remove operation`() = runTest {
        testModel.prepareTestProject()
        repository.saveMergeRequests(
            TEST_PROJECT_ID, listOf(
                StubMergeRequest("1"),
                StubMergeRequest("2")
            )
        )
        repository.deleteMergeRequestsForProject(TEST_PROJECT_ID)
        assertNull(repository.mergeRequest(1))
        assertNull(repository.mergeRequest(2))
    }
    @Test
    fun `assignees are empty`() = runTest {
        testModel.prepareTestProject()
        repository.saveMergeRequests(TEST_PROJECT_ID, listOf(StubMergeRequest("1")))
        assertEquals(0, repository.assignees(1L).size)
    }
    @Test
    fun `assignees are saved`() = runTest {
        testModel.prepareTestProject()
        repository.saveMergeRequests(TEST_PROJECT_ID, listOf(
            StubMergeRequest(
                id = "1",
                assignees = listOf(DBReportsRepository.PlainUser(1, "test.user", "Test User"))
            ))
        )
        assertEquals(1, repository.assignees(1L).size)
    }

    @Test
    fun `assignees are removed after MR removal`() = runTest {
        testModel.prepareTestProject()
        repository.saveMergeRequests(TEST_PROJECT_ID, listOf(
            StubMergeRequest(
                id = "1",
                assignees = listOf(DBReportsRepository.PlainUser(1, "test.user", "Test User"))
            ))
        )
        repository.deleteMergeRequest(1)
        assertEquals(0, repository.assignees(1L).size)
    }
}