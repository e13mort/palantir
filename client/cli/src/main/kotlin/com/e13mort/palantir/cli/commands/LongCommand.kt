/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.commands

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.render.ReportRender
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.long

class LongCommand<INTERACTOR_INPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>(
    name: String,
    interactor: Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>,
    renders: Map<RenderType, ReportRender<INTERACTOR_OUTPUT, String, RENDER_PARAMS>>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT,
    renderParamsMapper: (CommandParams) -> RENDER_PARAMS,
    private val commandParamMapper: (CommandParams, Long) -> INTERACTOR_INPUT
) : CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>(
    name,
    interactor,
    renders,
    renderValueMapper,
    renderParamsMapper
) {
    private val id by argument("id").long()
    override fun calculateArgs() = commandParamMapper(CommandParams(flags()), id)
}

fun <INTERACTOR_INPUT, INTERACTOR_OUTPUT> Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>.asLongCommand(
    name: String,
    renders: Map<CommandWithRender.RenderType, ReportRender<INTERACTOR_OUTPUT, String, Unit>>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandWithRender.CommandParams) -> Unit = {},
    commandParamMapper: (CommandWithRender.CommandParams, Long) -> INTERACTOR_INPUT,
    config: CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, Unit>.() -> Unit = { },
): CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, Unit> {
    return LongCommand(
        name, this, renders, renderValueMapper, renderParamsMapper, commandParamMapper
    ).also(config)
}

fun <INTERACTOR_OUTPUT> Interactor<Long, INTERACTOR_OUTPUT>.asLongCommand(
    name: String,
    renders: Map<CommandWithRender.RenderType, ReportRender<INTERACTOR_OUTPUT, String, Unit>>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandWithRender.CommandParams) -> Unit = { },
    config: CommandWithRender<Long, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, Unit>.() -> Unit = { },
): CommandWithRender<Long, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, Unit> {
    return LongCommand(
        name, this, renders, renderValueMapper, renderParamsMapper
    ) { _, it -> it }.also(config)
}