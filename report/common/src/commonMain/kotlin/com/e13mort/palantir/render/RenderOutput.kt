package com.e13mort.palantir.render

interface RenderOutput<V> {
    fun write(renderedResult: V)
}