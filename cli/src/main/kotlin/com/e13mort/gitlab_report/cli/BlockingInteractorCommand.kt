package com.e13mort.gitlab_report.cli

import com.e13mort.gitlab_report.interactors.Interactor
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking

class BlockingInteractorCommand(
    private val interactor: Interactor<Unit>,
    name: String
) : CliktCommand(name = name) {
    override fun run() = runBlocking { interactor.run() }
}

fun Interactor<Unit>.asCLICommand(name: String): CliktCommand {
    return BlockingInteractorCommand(this, name)
}