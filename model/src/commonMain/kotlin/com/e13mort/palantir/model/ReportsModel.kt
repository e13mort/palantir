/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

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