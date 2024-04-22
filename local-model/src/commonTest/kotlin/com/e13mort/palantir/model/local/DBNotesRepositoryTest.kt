package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.stub.StubMergeRequest
import com.e13mort.palantir.model.stub.StubMergeRequestEvent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DBNotesRepositoryTest {

    private val testModel = TestModel()
    private val notesRepository = DBNotesRepository(testModel.model)
    private val mrRepository = DBMergeRequestRepository(testModel.model)

    @BeforeTest
    fun setup() = runBlocking {
        testModel.prepareTestProject()
        mrRepository.addMergeRequests(TEST_PROJECT_ID, listOf(StubMergeRequest("1")))
    }

    @Test
    fun `check one note saved to MR`() = runTest {
        notesRepository.saveMergeRequestEvents(
            TEST_PROJECT_ID,
            1L,
            listOf(StubMergeRequestEvent(1L))
        )
        assertEquals(1, notesRepository.events(TEST_PROJECT_ID, 1L).size)
    }

    @Test
    fun `check two notes saved to MR`() = runTest {
        notesRepository.saveMergeRequestEvents(
            TEST_PROJECT_ID,
            1L,
            listOf(StubMergeRequestEvent(1L), StubMergeRequestEvent(2L))
        )
        assertEquals(2, notesRepository.events(TEST_PROJECT_ID, 1L).size)
    }

    @Test
    fun `check notes are overwritten`() = runTest {
        notesRepository.saveMergeRequestEvents(
            TEST_PROJECT_ID,
            1L,
            listOf(StubMergeRequestEvent(1L), StubMergeRequestEvent(2L))
        )
        notesRepository.saveMergeRequestEvents(
            TEST_PROJECT_ID,
            1L,
            listOf(StubMergeRequestEvent(1L))
        )
        assertEquals(1, notesRepository.events(TEST_PROJECT_ID, 1L).size)
    }

    @Test
    fun `check notes removed with MR`() = runTest {
        notesRepository.saveMergeRequestEvents(
            TEST_PROJECT_ID,
            1L,
            listOf(StubMergeRequestEvent(1L), StubMergeRequestEvent(2L))
        )
        mrRepository.deleteMergeRequests(TEST_PROJECT_ID, setOf(1L))
        assertEquals(0, notesRepository.events(TEST_PROJECT_ID, 1L).size)
    }
}