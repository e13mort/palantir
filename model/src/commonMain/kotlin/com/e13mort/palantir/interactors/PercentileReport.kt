package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ReportsRepository

interface PercentileReport {
    fun periodsCount(): Int

    fun period(index: Int): Period

    fun periodValue(index: Int, percentile: ReportsRepository.Percentile): Long

    fun totalMRCount(index: Int): Int

    fun compareTwoPeriods(firstIndex: Int, secondIndex: Int, percentile: ReportsRepository.Percentile): Float

    data class Period(val start: Long, val end: Long)
}