package com.e13mort.palantir.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

class RemoveCommand : CliktCommand(name = "remove", help = "Remove project from local index") {
    override fun run(): Unit = Unit
}