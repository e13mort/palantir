/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

class RepositoryCommand : CliktCommand(name = "repository", help = "Repository based reports") {
    override fun run() = Unit
}