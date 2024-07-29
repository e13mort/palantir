/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.utils

import com.e13mort.palantir.interactors.Range

class RangeParser(private val converter: StringDateConverter) {
    fun convert(input: String): List<Range> {
        val ranges = mutableListOf<Range>()
        val rangeStrings = input.split(":")
        if (rangeStrings.size < 2) throw IllegalArgumentException("there should be at lease two ranges")
        rangeStrings.windowed(2).forEach { rangePair ->
            ranges += Range(
                converter.convertStringToDate(rangePair[0]),
                converter.convertStringToDate(rangePair[1])
            )
        }
        return ranges
    }
}