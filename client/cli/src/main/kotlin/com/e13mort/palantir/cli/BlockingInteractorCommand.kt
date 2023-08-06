package com.e13mort.palantir.cli

import com.e13mort.palantir.interactors.Interactor
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking

class BlockingInteractorCommand(
    private val interactor: Interactor<Unit>,
    name: String
) : CliktCommand(name = name) {

    override fun run() = runBlocking {
        registeredOptions().forEach {
            it.names
        }
        interactor.run()
    }
}

fun Interactor<Unit>.asCLICommand(name: String): CliktCommand {
    return BlockingInteractorCommand(this, name)
}