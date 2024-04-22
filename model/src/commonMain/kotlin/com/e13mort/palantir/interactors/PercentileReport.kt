/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Percentile

interface PercentileReport {
    fun periodsCount(): Int

    fun period(index: Int): Range

    fun periodValue(index: Int, percentile: Percentile): Long

    fun totalMRCount(index: Int): Int

    fun compareTwoPeriods(firstIndex: Int, secondIndex: Int, percentile: Percentile): Float
}