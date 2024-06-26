/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Project
import com.e13mort.palantir.model.SyncableProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

class PrintAllProjectsInteractor(
    private val projectRepository: SyncableProjectRepository
) : Interactor<Unit, AllProjectsResult> {
    override fun run(arg: Unit): Flow<AllProjectsResult> {
        return flow {
            val result = projectRepository.projects().toList()
            object : AllProjectsResult {
                override fun projects(synced: Boolean): List<Project> {
                    return result.filter {
                        synced == it.synced()
                    }
                }
            }.apply { emit(this) }
        }
    }

}