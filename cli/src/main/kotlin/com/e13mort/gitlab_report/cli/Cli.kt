package com.e13mort.gitlab_report.cli

import com.e13mort.gitlab_report.cli.render.ASCIITableProjectsListRender
import com.e13mort.gitlab_report.interactors.*
import com.e13mort.gitlab_report.model.GitlabProjectsRepository
import com.e13mort.gitlab_report.model.local.DBProjectRepository
import com.e13mort.gitlab_report.model.local.DriverFactory
import com.e13mort.gitlab_report.model.local.LocalModel
import com.e13mort.gitlab_report.utils.Console
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    val model = LocalModel(DriverFactory().createDriver())
    val localProjectsRepository = DBProjectRepository(model.projectQueries)
    val gitlabProjectsRepository = GitlabProjectsRepository("***REMOVED***/", "***REMOVED***")
    val console = Console { message -> println(message) }

    val renderOutput = ConsoleRenderOutput(console)

    RootCommand().subcommands(
        PrintAllProjectsInteractor(localProjectsRepository).withRender(ASCIITableProjectsListRender(), renderOutput)
            .asCLICommand("print"),
        ScanProjectInteractor(localProjectsRepository, gitlabProjectsRepository, console).asCLICommand("scan")
    ).main(args)
}

class RootCommand : CliktCommand(name = "cli") {
    override fun run() = Unit
}

class ConsoleRenderOutput(private val console: Console) : RenderOutput<String> {
    override fun write(renderedResult: String) {
        console.write(renderedResult)
    }
}

