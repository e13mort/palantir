package com.e13mort.palantir.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

class SyncCommand : CliktCommand(name = "sync", help = "Sync remote data to local index") {
    override fun run() = Unit
}