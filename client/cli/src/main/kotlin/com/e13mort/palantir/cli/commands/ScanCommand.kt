package com.e13mort.palantir.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

class ScanCommand : CliktCommand(name = "scan", help = "Scan remote projects to local index") {
    override fun run() = Unit
}