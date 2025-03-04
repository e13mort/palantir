/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

class ReportCommand : CliktCommand(name = "report") {
    override fun run() = Unit

    override fun help(context: Context): String = "Reports for synced projects and repositories"

    class ApprovesCommand : CliktCommand(name = "approves") {
        override fun run() = Unit

        override fun help(context: Context): String = "MR approves based reports"
    }

    class MR : CliktCommand(name = "mr") {
        override fun run() = Unit

        override fun help(context: Context): String = "Merge Request based reports"

    }
}