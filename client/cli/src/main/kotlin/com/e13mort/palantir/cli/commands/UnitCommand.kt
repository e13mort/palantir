/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.commands

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.render.ReportRender

class UnitCommand<INTERACTOR_INPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>(
    name: String,
    interactor: Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>,
    renders: Map<RenderType, ReportRender<INTERACTOR_OUTPUT, String, RENDER_PARAMS>>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandParams) -> RENDER_PARAMS,
    private val commandParamMapper: (CommandParams) -> INTERACTOR_INPUT
) : CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>(
    name,
    interactor,
    renders,
    renderValueMapper,
    renderParamsMapper
) {
    override fun calculateArgs() = commandParamMapper(CommandParams(flags(), registeredOptions()))
}

fun <INTERACTOR_INPUT, INTERACTOR_OUTPUT> Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>.asUnitCommandWithUnitRenderParams(
    name: String,
    renders: Map<CommandWithRender.RenderType, ReportRender<INTERACTOR_OUTPUT, String, Unit>>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandWithRender.CommandParams) -> Unit = {},
    commandParamsMapper: (CommandWithRender.CommandParams) -> INTERACTOR_INPUT,
    config: CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, Unit>.() -> Unit = {}
): CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, Unit> {
    return asUnitCommand(
        name,
        renders,
        renderValueMapper,
        renderParamsMapper,
        commandParamsMapper,
        config
    )
}

fun <INTERACTOR_OUTPUT, RENDER_PARAMS> Interactor<Unit, INTERACTOR_OUTPUT>.asUnitCommandWithUnitCommandParams(
    name: String,
    renders: Map<CommandWithRender.RenderType, ReportRender<INTERACTOR_OUTPUT, String, RENDER_PARAMS>>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandWithRender.CommandParams) -> RENDER_PARAMS,
    commandParamsMapper: (CommandWithRender.CommandParams) -> Unit = { },
    config: CommandWithRender<Unit, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>.() -> Unit = {}
): CommandWithRender<Unit, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS> {
    return asUnitCommand(
        name,
        renders,
        renderValueMapper,
        renderParamsMapper,
        commandParamsMapper,
        config
    )
}

fun <INTERACTOR_INPUT, INTERACTOR_OUTPUT, RENDER_PARAMS> Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>.asUnitCommand(
    name: String,
    renders: Map<CommandWithRender.RenderType, ReportRender<INTERACTOR_OUTPUT, String, RENDER_PARAMS>>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandWithRender.CommandParams) -> RENDER_PARAMS,
    commandParamsMapper: (CommandWithRender.CommandParams) -> INTERACTOR_INPUT,
    config: CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>.() -> Unit = {}
): CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS> {
    return UnitCommand(
        name, this, renders, renderValueMapper, renderParamsMapper, commandParamsMapper
    ).also(config)
}
