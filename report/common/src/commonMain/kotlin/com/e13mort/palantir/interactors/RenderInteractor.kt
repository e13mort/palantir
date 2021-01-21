package com.e13mort.palantir.interactors

class RenderInteractor<T, V>(
    private val source: Interactor<T>,
    private val render: ReportRender<T, V>,
    private val output: RenderOutput<V>
) : Interactor<Unit> {
    override suspend fun run() {
        output.write(
            render.render(
                source.run()
            )
        )
    }
}

interface ReportRender<T, V> {
    fun render(value: T): V
}

interface RenderOutput<V> {
    fun write(renderedResult: V)
}

fun <T, V> Interactor<T>.withRender(render: ReportRender<T, V>, renderOutput: RenderOutput<V>): Interactor<Unit> {
    return RenderInteractor(this, render, renderOutput)
}