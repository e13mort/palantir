package com.e13mort.palantir.render

interface ReportRender<VALUE, RESULT, RENDER_PARAMETER> {
    fun render(value: VALUE, params: RENDER_PARAMETER): RESULT
}

