/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ReportsRepository
import com.e13mort.palantir.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApproveStatisticsInteractor(
    private val reportsRepository: ReportsRepository
) :
    Interactor<ApproveStatisticsInteractor.Params, ApproveStatisticsInteractor.Report> {

    interface Report {
        fun users(): List<User>

        fun periods(): List<String>

        fun approvesBy(user: User, period: String): Int

        fun totalCount(user: User): Int
    }

    data class Params(val projectId: Long, val type: StatisticsType)

    override fun run(arg: Params): Flow<Report> {
        return flow {
            val approversByPeriod = readDataFromRepository(arg.projectId, arg.type)
            val periodSet = mutableSetOf<String>()
            val approvesMap = mutableMapOf<User, MutableMap<String, Int>>()
            val totalCountMap = mutableMapOf<User, Int>()
            for (item in approversByPeriod) {
                periodSet.add(item.period())
                val approvesCount = item.approvesCount()
                val user = item.user()
                approvesMap.getOrPut(user) {
                    mutableMapOf()
                }[item.period()] = approvesCount
                totalCountMap[user] = totalCountMap.getOrElse(user, { 0 }) + approvesCount
            }
            object : Report {
                override fun users(): List<User> {
                    return approvesMap.keys.toList().sortedByDescending {
                        totalCount(it)
                    }
                }

                override fun periods(): List<String> {
                    return periodSet.toList()
                }

                override fun approvesBy(user: User, period: String): Int {
                    return approvesMap[user]?.get(period) ?: 0
                }

                override fun totalCount(user: User): Int {
                    return totalCountMap[user] ?: 0
                }

            }.apply {
                emit(this)
            }
        }
    }

    private suspend fun readDataFromRepository(
        projectId: Long, statisticsType: StatisticsType
    ): List<ReportsRepository.ApproveStatisticsItem> {
        val approversByPeriod = when (statisticsType) {
            StatisticsType.TOTAL_APPROVES -> reportsRepository.findApproversByPeriod(projectId)
            StatisticsType.FIRST_APPROVES -> reportsRepository.findFirstApproversByPeriod(projectId)
        }
        return approversByPeriod
    }

    enum class StatisticsType {
        TOTAL_APPROVES, FIRST_APPROVES
    }
}