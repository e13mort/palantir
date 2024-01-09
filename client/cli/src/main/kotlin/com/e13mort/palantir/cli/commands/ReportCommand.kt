package com.e13mort.palantir.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

class ReportCommand : CliktCommand("report") {
    override fun run() = Unit

    class ApprovesCommand : CliktCommand("approves") {
        override fun run() = Unit
    }

    class MR : CliktCommand("mr") {
        override fun run() = Unit
    }
}