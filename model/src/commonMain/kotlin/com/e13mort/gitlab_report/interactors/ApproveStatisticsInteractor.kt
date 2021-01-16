package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.ReportsRepository
import com.e13mort.gitlab_report.model.User

class ApproveStatisticsInteractor(private val reportsRepository: ReportsRepository) :
    Interactor<ApproveStatisticsInteractor.Report> {

    interface Report {
        fun users(): List<User>

        fun periods(): List<String>

        fun approvesBy(user: User, period: String): Int
    }

    override suspend fun run(): Report {
        val approversByPeriod = reportsRepository.findApproversByPeriod()
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
}