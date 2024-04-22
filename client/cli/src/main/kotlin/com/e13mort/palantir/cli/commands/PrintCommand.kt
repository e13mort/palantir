package com.e13mort.palantir.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

class PrintCommand : CliktCommand(name = "print", help = "Print various data from local index") {
    override fun run(): Unit = Unit
}