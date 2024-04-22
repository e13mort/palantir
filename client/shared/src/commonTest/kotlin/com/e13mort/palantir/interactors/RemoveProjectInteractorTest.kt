/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RemoveProjectInteractorTest {
    private val localComponent = LocalComponent(inMemoryModel())
    private val repositoryBuilder = RemoteProjectRepositoryBuilder().also {
        it.addBaseTestProject()
    }

    @Test
    fun `remove test project should clear project from repository`() = runTestOnRemovedProject {
        localComponent.projectRepository.projects().toList() shouldHaveSize 0
    }

    @Test
    fun `remove test project should clear MR data`() = runTestOnRemovedProject {
        localComponent.mrRepository.mergeRequestsForProject(1) shouldHaveSize 0
    }

    private fun runTestOnRemovedProject(block: suspend () -> Unit) = runTest {
        val syncInteractor = localComponent.createSyncInteractor(repositoryBuilder)
            .also { it.prepareForTest(localComponent) }
        syncInteractor.run(SyncInteractor.SyncStrategy.FullSyncForActiveProjects(true)).collect()
        localComponent.projectRepository.projects()
            .toList() shouldHaveSize 1 // verify sync succeeded
        localComponent.mrRepository.mergeRequestsForProject(1L) shouldHaveSize 1 // verify sync succeeded
        RemoveProjectInteractor(localComponent.projectRepository).run(1).collect()
        block()
    }
}