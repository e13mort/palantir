/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.commands

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.render.ReportRender
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.jakewharton.mosaic.runMosaicBlocking
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.last

abstract class CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, RENDER_INPUT, RENDER_PARAMS>(
    name: String,
    private val interactor: Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>,
    private val renders: Map<RenderType, ReportRender<RENDER_INPUT, String, RENDER_PARAMS>>,
    private val renderValueMapper: (INTERACTOR_OUTPUT) -> RENDER_INPUT,
    private val renderParamsMapper: (CommandParams) -> RENDER_PARAMS
) : CliktCommand(name = name) {

    open class CommandParams(
        @Deprecated("get flags from allOptions object")
        val flags: Set<String>,
        val allOptions: List<Option>
    )

    enum class RenderType {
        Table, CSV
    }

    private val renderType by option().switch(
        calculateAvailableRenderOptions()
    ).default(RenderType.Table)

    private val blocking by option("--blocking").flag(default = false)

    abstract fun calculateArgs(): INTERACTOR_INPUT

    override fun run() = runMosaicBlocking {
        val interactorOutputFlow: Flow<INTERACTOR_OUTPUT?> = interactor.run(calculateArgs())
        var state by remember {
            mutableStateOf<INTERACTOR_OUTPUT?>(null)
        }
        LaunchedEffect(Unit) {
            if (blocking) {
                state = interactorOutputFlow.last()
            } else {
                interactorOutputFlow.flowOn(Dispatchers.IO).collect {
                    state = it
                }
            }
        }
        RenderContent(state)
    }

    @Composable
    private fun RenderContent(state: INTERACTOR_OUTPUT?) {
        if (state != null) {
            Text(renderState(state))
        } else if (!blocking) {
            Text("Waiting...")
        }
    }

    private fun renderState(state: INTERACTOR_OUTPUT): String {
        val commandParams = CommandParams(flags(), registeredOptions())
        return renders[renderType]!!.render(
            renderValueMapper(state),
            renderParamsMapper(commandParams)
        )
    }

    private fun calculateAvailableRenderOptions(): Map<String, RenderType> {
        return renders.keys.associateBy { "--${it.name.toLowerCase(Locale.current)}" }
    }

    protected fun flags(): Set<String> {
        val allFlags = mutableSetOf<String>().apply {
            registeredOptions().map { option ->
                if (option is OptionWithValues<*, *, *>) {
                    if (option.value is Boolean && (option.value as Boolean)) {
                        addAll(option.names)
                    }
                }
            }
        }
        return allFlags
    }
}