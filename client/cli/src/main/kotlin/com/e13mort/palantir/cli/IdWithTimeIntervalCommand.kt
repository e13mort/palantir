package com.e13mort.palantir.cli

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.interactors.Range
import com.e13mort.palantir.utils.StringDateConverter
import com.e13mort.palantir.utils.asRanges
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.coroutines.runBlocking

class IdWithTimeIntervalCommand(
    name: String,
    private val dateFormat: StringDateConverter,
    private val factory: (Long, List<Range>) -> Interactor<Unit>
) : CliktCommand(name = name) {

    private val id by argument("id").long()

    private val ranges by option("--ranges").convert {
        it.asRanges(dateFormat)
    }.default(mutableListOf(Range(0, System.currentTimeMillis())))
    override fun run() = runBlocking {
        factory(id, ranges).run()
    }
}