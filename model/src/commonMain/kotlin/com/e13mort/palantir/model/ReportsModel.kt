package com.e13mort.palantir.model

interface ReportsRepository {
    enum class Percentile(val factor: Float) {
        P1(0.01F),
        P10(0.1F),
        P20(0.2F),
        P30(0.3F),
        P40(0.4F),
        P50(0.5F),
        P60(0.6F),
        P70(0.7F),
        P80(0.8F),
        P90(0.9F),
        P100(1F)
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

    suspend fun firstApprovesStatistics(
        projectId: Long,
        createFromMillis: Long,
        createBeforeMillis: Long
    ): FirstApproveStatistics
}