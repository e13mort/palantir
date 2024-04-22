/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

class ReportCommand :
    CliktCommand(name = "report", help = "Reports for synced projects and repositories") {
    override fun run() = Unit

    class ApprovesCommand : CliktCommand(name = "approves", help = "MR approves based reports") {
        override fun run() = Unit
    }

    class MR : CliktCommand(name = "mr", help = "Merge Request based reports") {
        override fun run() = Unit
    }
}