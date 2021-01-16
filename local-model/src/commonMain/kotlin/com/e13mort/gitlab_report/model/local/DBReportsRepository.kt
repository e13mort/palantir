package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.ReportsRepository
import com.e13mort.gitlab_report.model.User
import com.e13mort.gitlabreport.model.local.reports.SelectApproversStatisticsByWeek

class DBReportsRepository(localModel: LocalModel) : ReportsRepository {

    private val approversQueries = localModel.approversQueries

    override suspend fun findApproversByPeriod(): List<ReportsRepository.ApproveStatisticsItem> {
        return approversQueries.selectApproversStatisticsByWeek().executeAsList().map {
            StatItem(it)
        }
    }

    internal data class StatItem(
        private val dbObject: SelectApproversStatisticsByWeek
    ) : ReportsRepository.ApproveStatisticsItem {

        override fun user(): User = PlainUser(dbObject.user_id, dbObject.user_userName!!, dbObject.user_name!!)

        override fun approvesCount(): Int {
            return dbObject.aproves_count.toInt()
        }

        override fun period(): String = dbObject.period
    }

    data class PlainUser(val id: Long, val userName: String, val name: String) : User {
        override fun id(): Long = id

        override fun name(): String = name

        override fun userName(): String = userName
    }
}