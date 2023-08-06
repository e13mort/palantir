package com.e13mort.palantir.cli

import com.e13mort.palantir.interactors.Interactor
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.FlagOption
import kotlinx.coroutines.runBlocking

class FlagsCommand(
    name: String,
    private val factory: (Set<String>) -> Interactor<Unit>
) : CliktCommand(name = name) {
    override fun run() = runBlocking {
        val allFlags = mutableSetOf<String>().apply {
            registeredOptions().map { option ->
                if (option is FlagOption<*>) {
                    if (option.value is Boolean && (option.value as Boolean)) {
                        addAll(option.names)
                    }
                }
            }
        }
        factory(allFlags).run()
    }
}