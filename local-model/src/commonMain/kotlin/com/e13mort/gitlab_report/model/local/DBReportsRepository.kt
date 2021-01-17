package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.ReportsRepository
import com.e13mort.gitlab_report.model.User
import com.e13mort.gitlabreport.model.local.reports.FirstApprovesStatistics
import com.e13mort.gitlabreport.model.local.reports.SelectApproversStatisticsByWeek

class DBReportsRepository(localModel: LocalModel) : ReportsRepository {

    private val approversQueries = localModel.approversQueries
    private val mrInterationsQueris = localModel.mr_interractionsQueries

    override suspend fun findApproversByPeriod(): List<ReportsRepository.ApproveStatisticsItem> {
        return approversQueries.selectApproversStatisticsByWeek().executeAsList().map {
            StatItem(it)
        }
    }

    override suspend fun findFirstApproversByPeriod(): List<ReportsRepository.ApproveStatisticsItem> {
        return mrInterationsQueris.firstApprovesStatistics().executeAsList().map {
            FirstApprovesStatItem(it)
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

    internal data class FirstApprovesStatItem(
        private val dbItem: FirstApprovesStatistics
    ) : ReportsRepository.ApproveStatisticsItem {
        override fun user(): User = PlainUser(dbItem.user_id!!.toLong(), dbItem.username!!, dbItem.name!!)

        override fun approvesCount(): Int = dbItem.count.toInt()

        override fun period(): String = dbItem.period
    }

    data class PlainUser(val id: Long, val userName: String, val name: String) : User {
        override fun id(): Long = id

        override fun name(): String = name

        override fun userName(): String = userName
    }
}