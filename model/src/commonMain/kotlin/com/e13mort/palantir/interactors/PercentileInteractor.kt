package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ReportsRepository

class PercentileInteractor(
    private val reportsRepository: ReportsRepository,
    private val projectId: Long,
    private val createdFromMillis: Long,
    private val createdBeforeMillis: Long
) : Interactor<PercentileInteractor.PercentileReport> {

    interface PercentileReport {
        fun periodsCount(): Int = 1

        fun period(index: Int): Period

        fun periodValue(index: Int, percentile: ReportsRepository.Percentile): Long

        data class Period(val start: Long, val end: Long)
    }

    override suspend fun run(): PercentileReport {
        val statistics = reportsRepository.firstApprovesStatistics(projectId, createdFromMillis, createdBeforeMillis)

        return object : PercentileReport {
            override fun period(index: Int): PercentileReport.Period {
                return PercentileReport.Period(createdFromMillis, createdBeforeMillis)
            }

            override fun periodValue(index: Int, percentile: ReportsRepository.Percentile): Long {
                return statistics.firstApproveTimeSeconds(percentile)
            }

        }
    }
}