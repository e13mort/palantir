package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.ReportsRepository
import com.e13mort.gitlab_report.model.User

class ApproveStatisticsInteractor(
    private val reportsRepository: ReportsRepository,
    private val type: StatisticsType
) :
    Interactor<ApproveStatisticsInteractor.Report> {

    interface Report {
        fun users(): List<User>

        fun periods(): List<String>

        fun approvesBy(user: User, period: String): Int
    }

    override suspend fun run(): Report {
        val approversByPeriod = readDataFromRepository()
        val periodSet = mutableSetOf<String>()
        val approvesMap = mutableMapOf<User, MutableMap<String, Int>>()
        for (item in approversByPeriod) {
            periodSet.add(item.period())
            approvesMap.getOrPut(item.user()) {
                mutableMapOf()
            }[item.period()] = item.approvesCount()
        }


        return object : Report {
            override fun users(): List<User> {
                return approvesMap.keys.toList()
            }

            override fun periods(): List<String> {
                return periodSet.toList()
            }

            override fun approvesBy(user: User, period: String): Int {
                return approvesMap[user]?.get(period) ?: 0
            }

        }
    }

    private suspend fun readDataFromRepository(): List<ReportsRepository.ApproveStatisticsItem> {
        val approversByPeriod = when (type) {
            StatisticsType.TOTAL_APPROVES -> reportsRepository.findApproversByPeriod()
            StatisticsType.FIRST_APPROVES -> reportsRepository.findFirstApproversByPeriod()
        }
        return approversByPeriod
    }

    enum class StatisticsType {
        TOTAL_APPROVES, FIRST_APPROVES
    }
}