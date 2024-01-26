package com.e13mort.palantir.model

interface ReportsRepository {

    interface ApproveStatisticsItem {
        fun user(): User
        fun approvesCount(): Int
        fun period(): String
    }

    interface FirstApproveStatistics {
        fun firstApproveTimeSeconds(percentile: Percentile): Long

        fun totalMRCount(): Int
    }

    suspend fun findApproversByPeriod(projectId: Long): List<ApproveStatisticsItem>

    suspend fun findFirstApproversByPeriod(projectId: Long): List<ApproveStatisticsItem>

    suspend fun firstApprovesStatistics(
        projectId: Long,
        createFromMillis: Long,
        createBeforeMillis: Long
    ): FirstApproveStatistics
}