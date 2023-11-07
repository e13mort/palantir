package com.e13mort.palantir.model.local

import com.e13mort.gitlabreport.model.local.DBProject
import com.e13mort.palantir.model.stub.StubMergeRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val TEST_PROJECT_ID = 1L

class DBMergeRequestRepositoryTest {

    private val driver = DriverFactory("").createDriver(type = DriverType.MEMORY)
    private val model = LocalModel(driver)
    private val repository = DBMergeRequestRepository(model)
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
        prepareTestProject()
        repository.saveMergeRequests(TEST_PROJECT_ID, listOf(StubMergeRequest("1")))
    }

    @Test
    fun `inserted MR returns not null value`() = runTest {
        prepareTestProject()
        repository.saveMergeRequests(TEST_PROJECT_ID, listOf(StubMergeRequest("1")))
        assertNotNull(repository.mergeRequest(1L))
    }

    private fun prepareTestProject() {
        model.projectQueries.insert(DBProject(TEST_PROJECT_ID, "test project", "ssh://test", "https://test"))
    }
}