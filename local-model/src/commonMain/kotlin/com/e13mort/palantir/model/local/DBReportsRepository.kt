package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.ReportsRepository
import com.e13mort.palantir.model.User
import com.e13mort.gitlabreport.model.local.reports.FirstApprovesStatistics
import com.e13mort.gitlabreport.model.local.reports.SelectApproversStatisticsByWeek
import kotlin.math.ceil

class DBReportsRepository(localModel: LocalModel) : ReportsRepository {

    private val approversQueries = localModel.approversQueries
    private val mrInteractionsQueries = localModel.mr_interractionsQueries

    override suspend fun findApproversByPeriod(projectId: Long): List<ReportsRepository.ApproveStatisticsItem> {
        return approversQueries.selectApproversStatisticsByWeek(projectId).executeAsList().map {
            StatItem(it)
        }
    }

    override suspend fun findFirstApproversByPeriod(projectId: Long): List<ReportsRepository.ApproveStatisticsItem> {
        return mrInteractionsQueries.firstApprovesStatistics(projectId).executeAsList().map {
            FirstApprovesStatItem(it)
        }
    }

    override suspend fun firstApprovesStatistics(
        projectId: Long,
        createFromMillis: Long,
        createBeforeMillis: Long
    ): ReportsRepository.FirstApproveStatistics {
        val mrsWithApproves = mrInteractionsQueries.mrsWithApproves(projectId, createBeforeMillis, createFromMillis)
        val results = mrsWithApproves.executeAsList()

        return object : ReportsRepository.FirstApproveStatistics {
            override fun firstApproveTimeSeconds(percentile: ReportsRepository.Percentile): Long {
                if (results.isEmpty()) return 0
                //nearest-rank method: https://en.wikipedia.org/wiki/Percentile
                val index = ceil(percentile.factor * (results.size)).toInt()
                return results[index - 1].create_to_first_interaction_time_diff / 1000
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