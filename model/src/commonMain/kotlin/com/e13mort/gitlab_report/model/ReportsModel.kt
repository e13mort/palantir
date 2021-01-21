package com.e13mort.gitlab_report.model

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

    suspend fun findApproversByPeriod(): List<ApproveStatisticsItem>

    suspend fun findFirstApproversByPeriod(): List<ApproveStatisticsItem>

    suspend fun calculateFirstApprovesStatistics(): FirstApproveStatistics
}