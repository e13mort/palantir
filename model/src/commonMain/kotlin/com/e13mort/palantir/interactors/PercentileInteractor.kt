package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ReportsRepository

class PercentileInteractor(
    private val reportsRepository: ReportsRepository,
) : Interactor<PercentileInteractor.PercentileReport> {

    interface PercentileReport {
        fun iterate(block: (ReportsRepository.Percentile, Long) -> Unit)
    }

    override suspend fun run(): PercentileReport {
        val statistics = reportsRepository.calculateFirstApprovesStatistics()

        return object : PercentileReport {
            override fun iterate(block: (ReportsRepository.Percentile, Long) -> Unit) {
                ReportsRepository.Percentile.values().forEach {
                    block(it, statistics.firstApproveTimeSeconds(it))
                }
            }

        }
    }
}