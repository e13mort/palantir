/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoveProjectInteractor(
    private val projectRepository: ProjectRepository
) : Interactor<Long, Unit> {
    override fun run(arg: Long): Flow<Unit> {
        return flow {
            projectRepository.removeProjects(setOf(arg))
            emit(Unit)
        }
    }
}