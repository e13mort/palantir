/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

import com.e13mort.palantir.utils.RangeParser
import com.e13mort.palantir.utils.StringDateConverter
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.shouldBe
import java.text.SimpleDateFormat
import kotlin.test.Test

class Test {
    @Test
    fun testRanges() {
        val testRange =
            "01-01-2024:01-02-2024:01-03-2024:01-04-2024:01-05-2024:01-06-2024:01-07-2024"
        val converter =
            StringDateConverter { string -> SimpleDateFormat("dd-MM-yyyy").parse(string).time }
        val ranges = RangeParser(converter).convert(testRange)
        ranges shouldMatchEach listOf(
            {
                it.start shouldBe converter.convertStringToDate("01-01-2024")
                it.end shouldBe converter.convertStringToDate("01-02-2024")
            },
            {
                it.start shouldBe converter.convertStringToDate("01-02-2024")
                it.end shouldBe converter.convertStringToDate("01-03-2024")
            },
            {
                it.start shouldBe converter.convertStringToDate("01-03-2024")
                it.end shouldBe converter.convertStringToDate("01-04-2024")
            },
            {
                it.start shouldBe converter.convertStringToDate("01-04-2024")
                it.end shouldBe converter.convertStringToDate("01-05-2024")
            },
            {
                it.start shouldBe converter.convertStringToDate("01-05-2024")
                it.end shouldBe converter.convertStringToDate("01-06-2024")
            },
            {
                it.start shouldBe converter.convertStringToDate("01-06-2024")
                it.end shouldBe converter.convertStringToDate("01-07-2024")
            },
        )
    }
}