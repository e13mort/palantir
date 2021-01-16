package com.e13mort.gitlab_report.model

interface ReportsRepository {
    interface ApproveStatisticsItem {
        fun user(): User
        fun approvesCount(): Int
        fun period(): String
    }

    suspend fun findApproversByPeriod(): List<ApproveStatisticsItem>
}