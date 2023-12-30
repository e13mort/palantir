package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ReportsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PercentileInteractor(
    private val reportsRepository: ReportsRepository
) : Interactor<Pair<Long, List<Range>>, PercentileReport> {
    override suspend fun run(arg: Pair<Long, List<Range>>): Flow<PercentileReport> {
        return flow {
            val reports = arg.second.map {
                reportsRepository.firstApprovesStatistics(arg.first, it.start, it.end)
            }
            object : PercentileReport {
                override fun periodsCount(): Int {
                    return reports.size
                }

                override fun period(index: Int): Range {
                    return arg.second[index].let {
                        Range(it.start, it.end)
                    }
                }

                override fun periodValue(
                    index: Int,
                    percentile: ReportsRepository.Percentile
                ): Long {
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
                    return (reports[firstIndex].firstApproveTimeSeconds(percentile)
                        .toFloat() / reports[secondIndex].firstApproveTimeSeconds(percentile)
                        .toFloat())
                }
            }.apply { emit(this) }
        }
    }
}