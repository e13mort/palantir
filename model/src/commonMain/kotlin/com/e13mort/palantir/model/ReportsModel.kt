package com.e13mort.palantir.model

interface ReportsRepository {
    enum class Percentile {
        P1, P10, P20, P30, P40, P50, P60, P70, P80, P90, P100
    }

    interface ApproveStatisticsItem {
        fun user(): User
        fun approvesCount(): Int
        fun period(): String
    }

    interface FirstApproveStatistics {
        fun firstApproveTimeSeconds(percentile: Percentile): Long
    }

    suspend fun findApproversByPeriod(projectId: Long): List<ApproveStatisticsItem>

    suspend fun findFirstApproversByPeriod(projectId: Long): List<ApproveStatisticsItem>

    suspend fun firstApprovesStatistics(projectId: Long, createFromMillis: Long, createBeforeMillis: Long): FirstApproveStatistics
}