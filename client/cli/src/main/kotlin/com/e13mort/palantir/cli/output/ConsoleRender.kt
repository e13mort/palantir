package com.e13mort.palantir.cli.output

import com.e13mort.palantir.render.RenderOutput
import com.e13mort.palantir.render.ReportRender

fun <T, B> ReportRender<T, String, B>.asConsole(output: RenderOutput<String>): ConsoleRender<T, B> {
    return ConsoleRender(output, this)
}

class ConsoleRender<T, B>(
    private val renderOutput: RenderOutput<String>,
    private val sourceRender: ReportRender<T, String, B>
) : ReportRender<T, Unit, B> {
    override fun render(value: T, params: B) {
        renderOutput.write(sourceRender.render(value, params))
    }
}