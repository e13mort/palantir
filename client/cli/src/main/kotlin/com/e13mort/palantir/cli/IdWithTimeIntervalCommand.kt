package com.e13mort.palantir.cli

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.interactors.Range
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat

class IdWithTimeIntervalCommand(
    name: String,
    private val dateFormat: String,
    private val factory: (Long, List<Range>) -> Interactor<Unit>
) : CliktCommand(name = name) {

    private val id by argument("id").long()

    private val ranges by option("--ranges").convert {
        val ranges = mutableListOf<Range>()
        val rangeStrings = it.split(":")
        if (rangeStrings.size < 2) throw IllegalArgumentException("there should be at lease two ranges")
        rangeStrings.windowed(2).forEach { rangePair ->
            ranges += Range(stringToTimeMillis(rangePair[0]), stringToTimeMillis(rangePair[1]))
        }
        ranges
    }.default(mutableListOf(Range(0, System.currentTimeMillis())))

    override fun run() = runBlocking {
        factory(id, ranges).run()
    }

    private fun stringToTimeMillis(it: String) = SimpleDateFormat(dateFormat).parse(it).time

}