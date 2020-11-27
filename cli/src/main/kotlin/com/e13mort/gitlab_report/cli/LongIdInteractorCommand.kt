package com.e13mort.gitlab_report.cli

import com.e13mort.gitlab_report.interactors.Interactor
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.coroutines.runBlocking

class LongIdInteractorCommand(
    name: String,
    private val factory: (Long) -> Interactor<Unit>
) : CliktCommand(name = name) {

    private val id by argument("id").long()

    override fun run() = runBlocking {
        factory(id).run()
    }
}