package com.e13mort.palantir.cli.output

import com.e13mort.palantir.render.RenderOutput
import com.e13mort.palantir.utils.Console
import com.e13mort.palantir.utils.writeTo

class ConsoleRenderOutput(
    private val console: Console,
    private val style: Console.WriteStyle = Console.WriteStyle.ADD
) : RenderOutput<String> {
    override fun write(renderedResult: String) {
        renderedResult.writeTo(console, style)
    }
}