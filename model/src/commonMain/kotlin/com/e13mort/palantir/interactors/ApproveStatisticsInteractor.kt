package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ReportsRepository
import com.e13mort.palantir.model.User

class ApproveStatisticsInteractor(
    private val reportsRepository: ReportsRepository,
    private val projectId: Long,
    private val type: StatisticsType
) :
    Interactor<ApproveStatisticsInteractor.Report> {

    interface Report {
        fun users(): List<User>

        fun periods(): List<String>

        fun approvesBy(user: User, period: String): Int

        fun totalCount(user: User): Int
    }

    override suspend fun run(): Report {
        val approversByPeriod = readDataFromRepository()
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


        return object : Report {
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

        }
    }

    private suspend fun readDataFromRepository(): List<ReportsRepository.ApproveStatisticsItem> {
        val approversByPeriod = when (type) {
            StatisticsType.TOTAL_APPROVES -> reportsRepository.findApproversByPeriod(projectId)
            StatisticsType.FIRST_APPROVES -> reportsRepository.findFirstApproversByPeriod(projectId)
        }
        return approversByPeriod
    }

    enum class StatisticsType {
        TOTAL_APPROVES, FIRST_APPROVES
    }
}