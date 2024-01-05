package com.e13mort.palantir.cli.commands

import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.interactors.Range
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.StringDateConverter
import com.e13mort.palantir.utils.asRanges
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long

class LongWithRangesCommand<INTERACTOR_INPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>(
    name: String,
    interactor: Interactor<INTERACTOR_INPUT, INTERACTOR_OUTPUT>,
    render: ReportRender<INTERACTOR_OUTPUT, String, RENDER_PARAMS>,
    renderValueMapper: (INTERACTOR_OUTPUT) -> INTERACTOR_OUTPUT,
    renderParamsMapper: (CommandParams) -> RENDER_PARAMS,
    private val commandParamMapper: (Long, List<Range>) -> INTERACTOR_INPUT,
    private val dateFormat: StringDateConverter,
) : CommandWithRender<INTERACTOR_INPUT, INTERACTOR_OUTPUT, INTERACTOR_OUTPUT, RENDER_PARAMS>(
    name,
    interactor,
    render,
    renderValueMapper,
    renderParamsMapper
) {
    private val id by argument("id").long()
    private val ranges: List<Range> by option("--ranges").convert {
        it.asRanges(dateFormat)
    }.default(mutableListOf(Range(0, System.currentTimeMillis())))
    override fun calculateArgs() = commandParamMapper(id, ranges)
}