package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.ReportsRepository
import com.e13mort.gitlab_report.model.User
import com.e13mort.gitlabreport.model.local.reports.FirstApprovesStatistics
import com.e13mort.gitlabreport.model.local.reports.SelectApproversStatisticsByWeek
import com.squareup.sqldelight.db.SqlDriver

class DBReportsRepository(localModel: LocalModel, val driver: SqlDriver) : ReportsRepository {

    private val approversQueries = localModel.approversQueries
    private val mrInteractionsQueries = localModel.mr_interractionsQueries

    companion object {
        const val percentileQuery = "select max(time_diff / 1000) as seconds, (rank * 100) as percentile " +
                "from ( " +
                "         select time_diff, " +
                "                round(percent_rank() over (ORDER BY time_diff), 1) as rank " +
                "         from mr_interaction " +
                "         where time_diff is not null " +
                "     ) " +
                "group by rank"
    }

    override suspend fun findApproversByPeriod(): List<ReportsRepository.ApproveStatisticsItem> {
        return approversQueries.selectApproversStatisticsByWeek().executeAsList().map {
            StatItem(it)
        }
    }

    override suspend fun findFirstApproversByPeriod(): List<ReportsRepository.ApproveStatisticsItem> {
        return mrInteractionsQueries.firstApprovesStatistics().executeAsList().map {
            FirstApprovesStatItem(it)
        }
    }

    override suspend fun calculateFirstApprovesStatistics(): ReportsRepository.FirstApproveStatistics {
        val map = mutableMapOf<Int, Long>()
        driver.executeQuery(-1, percentileQuery, 0).let {
            while (it.next()) {
                map[it.getLong(1)!!.toInt()] = it.getLong(0)!!
            }
            it.close()
        }
        return object : ReportsRepository.FirstApproveStatistics {
            override fun firstApproveTimeSeconds(percentile: ReportsRepository.Percentile): Long {
                return map[percentile.ordinal * 10] ?: -1
            }
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