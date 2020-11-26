package com.e13mort.gitlab_report.cli

import com.e13mort.gitlab_report.interactors.Interactor
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking

class IdInteractorCommand(
    name: String,
    private val factory: (String) -> Interactor<Unit>
) : CliktCommand(name = name) {

    private val id: String by argument("id")

    override fun run() = runBlocking {
        factory(id).run()
    }
}