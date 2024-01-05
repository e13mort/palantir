package com.e13mort.palantir.cli.commands

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.render.ReportRender

class UnitCommand<INTERACTOR_INPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>(
    name: String,
    interactor: Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>,
    render: ReportRender<INTERACTOR_OUTPUT, String, RENDER_PARAMS>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandParams) -> RENDER_PARAMS,
    private val commandParamMapper: () -> INTERACTOR_INPUT
) : CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>(
    name,
    interactor,
    render,
    renderValueMapper,
    renderParamsMapper
) {
    override fun calculateArgs() = commandParamMapper()
}

fun <INTERACTOR_INPUT, INTERACTOR_OUTPUT>Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>.asUnitCommandWithUnitRenderParams(
    name: String,
    render: ReportRender<INTERACTOR_OUTPUT, String, Unit>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandWithRender.CommandParams) -> Unit = {},
    commandParamsMapper: () -> INTERACTOR_INPUT,
    config: CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, Unit>.() -> Unit = {}
): CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, Unit> {
    return asUnitCommand(name, render, renderValueMapper, renderParamsMapper, commandParamsMapper, config)
}

fun <INTERACTOR_OUTPUT, RENDER_PARAMS>Interactor<Unit, INTERACTOR_OUTPUT>.asUnitCommandWithUnitCommandParams(
    name: String,
    render: ReportRender<INTERACTOR_OUTPUT, String, RENDER_PARAMS>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandWithRender.CommandParams) -> RENDER_PARAMS,
    commandParamsMapper: () -> Unit = { },
    config: CommandWithRender<Unit, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>.() -> Unit = {}
): CommandWithRender<Unit, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS> {
    return asUnitCommand(name, render, renderValueMapper, renderParamsMapper, commandParamsMapper, config)
}

fun <INTERACTOR_INPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>.asUnitCommand(
    name: String,
    render: ReportRender<INTERACTOR_OUTPUT, String, RENDER_PARAMS>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT = { res -> res },
    renderParamsMapper: (CommandWithRender.CommandParams) -> RENDER_PARAMS,
    commandParamsMapper: () -> INTERACTOR_INPUT,
    config: CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>.() -> Unit = {}
): CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS> {
    return UnitCommand(
        name, this, render, renderValueMapper, renderParamsMapper, commandParamsMapper
    ).also(config)
}
