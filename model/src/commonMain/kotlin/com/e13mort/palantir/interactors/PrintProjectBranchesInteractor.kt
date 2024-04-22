/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Branches
import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

class PrintProjectBranchesInteractor(
    private val localRepository: ProjectRepository
) : Interactor<Long, PrintProjectBranchesInteractor.BranchesReport> {

    class BranchesReport(private val branches: Branches) {

        suspend fun walk(callBack: (String) -> Unit) {
            val toList = branches.values().toList()
            toList.forEach {
                callBack(it.name())
            }
        }
    }

    override fun run(arg: Long): Flow<BranchesReport> {
        return flow {
            val project = localRepository.findProject(arg)
                ?: throw Exception("Project with id $arg not found")
            emit(BranchesReport(project.branches()))
        }
    }
}