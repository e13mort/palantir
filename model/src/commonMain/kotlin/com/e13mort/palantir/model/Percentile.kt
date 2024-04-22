/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model

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
    P95(0.95F),
    P99(0.99F),
    P100(1F);

    companion object {
        fun fromString(string: String): List<Percentile> {
            if (string.isEmpty()) return values().toList()
            return string.split(",").map {
                valueOf(it.uppercase())
            }.sortedBy {
                it.ordinal
            }
        }
    }
}