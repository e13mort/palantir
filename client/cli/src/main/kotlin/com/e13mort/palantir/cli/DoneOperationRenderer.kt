package com.e13mort.palantir.cli

import com.e13mort.palantir.render.ReportRender

object DoneOperationRenderer : ReportRender<Unit, String, Unit> {
    override fun render(value: Unit, params: Unit): String {
        return "Done"
    }
}