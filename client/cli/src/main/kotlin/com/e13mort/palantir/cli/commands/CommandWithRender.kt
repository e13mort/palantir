package com.e13mort.palantir.cli.commands

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.render.ReportRender
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.FlagOption
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

abstract class CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, RENDER_INPUT, RENDER_PARAMS>(
    name: String,
    private val interactor: Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>,
    private val render: ReportRender<RENDER_INPUT, Unit, RENDER_PARAMS>,
    private val renderValueMapper: (INTERACTOR_OUTPUT) -> RENDER_INPUT,
    private val renderParamsMapper: (CommandParams) -> RENDER_PARAMS
) : CliktCommand(name = name) {

    open class CommandParams(
        val flags: Set<String>
    )

    abstract fun calculateArgs(): INTERACTOR_INPUT

    override fun run() = runBlocking {
        interactor.run(calculateArgs()).onEach {
            val commandParams = CommandParams(flags())
            render.render(renderValueMapper(it), renderParamsMapper(commandParams))
        }.collect()
    }

    private fun flags(): Set<String> {
        val allFlags = mutableSetOf<String>().apply {
            registeredOptions().map { option ->
                if (option is FlagOption<*>) {
                    if (option.value is Boolean && (option.value as Boolean)) {
                        addAll(option.names)
                    }
                }
            }
        }
        return allFlags
    }
}