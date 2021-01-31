package com.e13mort.palantir.cli

import com.e13mort.palantir.interactors.Interactor
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
    private val factory: (Long, Long, Long) -> Interactor<Unit>
) : CliktCommand(name = name) {

    private val id by argument("id").long()

    private val intervalStartMillis by option("--from").convert {
        stringToTimeMillis(it)
    }.default(0)

    private val intervalEndMillis by option("--until").convert {
        stringToTimeMillis(it)
    }.default(System.currentTimeMillis())

    override fun run() = runBlocking {
        factory(id, intervalStartMillis, intervalEndMillis).run()
    }

    private fun stringToTimeMillis(it: String) = SimpleDateFormat(dateFormat).parse(it).time
}