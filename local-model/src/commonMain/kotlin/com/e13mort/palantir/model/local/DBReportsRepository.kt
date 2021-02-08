package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.ReportsRepository
import com.e13mort.palantir.model.User
import com.e13mort.gitlabreport.model.local.reports.FirstApprovesStatistics
import com.e13mort.gitlabreport.model.local.reports.SelectApproversStatisticsByWeek
import com.squareup.sqldelight.db.SqlDriver

class DBReportsRepository(localModel: LocalModel, val driver: SqlDriver) : ReportsRepository {

    private val approversQueries = localModel.approversQueries
    private val mrInteractionsQueries = localModel.mr_interractionsQueries

    companion object {
        const val percentileQuery = "select max(create_to_first_interaction_time_diff / 1000) as seconds, (rank * 100) as percentile " +
                "from ( " +
                "         select create_to_first_interaction_time_diff, " +
                "                round(percent_rank() over (ORDER BY create_to_first_interaction_time_diff), 1) as rank " +
                "         from mr_interaction " +
                "         where create_to_first_interaction_time_diff is not null and project_id=? and created_time < ? and created_time > ?" +
                "     ) " +
                "group by rank"
    }

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
        val map = mutableMapOf<Int, Long>()
        driver.executeQuery(-1, percentileQuery, 3){
            bindLong(1, projectId)
            bindLong(2, createBeforeMillis)
            bindLong(3, createFromMillis)
        }.let {
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