package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ReportsRepository

class PercentileInteractor(
    private val reportsRepository: ReportsRepository,
    private val projectId: Long,
    private val ranges: List<Range>
) : Interactor<PercentileReport> {

    override suspend fun run(): PercentileReport {
        val reports = ranges.map {
            reportsRepository.firstApprovesStatistics(projectId, it.start, it.end)
        }

        return object : PercentileReport {
            override fun periodsCount(): Int {
                return reports.size
            }

            override fun period(index: Int): PercentileReport.Period {
                return ranges[index].let {
                    PercentileReport.Period(it.start, it.end)
                }
            }

            override fun periodValue(index: Int, percentile: ReportsRepository.Percentile): Long {
                return reports[index].firstApproveTimeSeconds(percentile)
            }

            override fun totalMRCount(index: Int): Int {
                return reports[index].totalMRCount()
            }

            override fun compareTwoPeriods(
                firstIndex: Int,
                secondIndex: Int,
                percentile: ReportsRepository.Percentile
            ): Float {
                if (firstIndex < 0 || secondIndex < 0) throw IllegalArgumentException("Wrong indexes: $firstIndex $secondIndex")
                return (reports[firstIndex].firstApproveTimeSeconds(percentile).toFloat() / reports[secondIndex].firstApproveTimeSeconds(percentile).toFloat())
            }

        }
    }
}