package com.e13mort.gitlab_report.cli

import com.e13mort.gitlab_report.cli.render.*
import com.e13mort.gitlab_report.interactors.*
import com.e13mort.gitlab_report.model.GitlabProjectsRepository
import com.e13mort.gitlab_report.model.local.DBProjectRepository
import com.e13mort.gitlab_report.model.local.DriverFactory
import com.e13mort.gitlab_report.model.local.LocalModel
import com.e13mort.gitlab_report.utils.Console
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    val driver = DriverFactory().createDriver()
    val model = LocalModel(driver)

    val localProjectsRepository = DBProjectRepository(model)
    val gitlabProjectsRepository = GitlabProjectsRepository("***REMOVED***/", "***REMOVED***")
    val console = Console { message -> println(message) }

    val consoleOutput = ConsoleRenderOutput(console)

    RootCommand().subcommands(
        PrintCommand().subcommands(
            PrintAllProjectsInteractor(localProjectsRepository).withRender(ASCIITableProjectsListRender(), consoleOutput)
                .asCLICommand("projects"),
            LongIdInteractorCommand("project") {
                PrintProjectSummaryInteractor(localProjectsRepository, it).withRender(ASCIITableProjectRender(), consoleOutput)
            },
            LongIdInteractorCommand("branches") {
                PrintProjectBranchesInteractor(localProjectsRepository, it).withRender(ASCIIBranchesRender(), consoleOutput)
            },
            LongIdInteractorCommand("mr") {
                PrintProjectMergeRequestsInteractor(localProjectsRepository, it).withRender(ASCIIMergeRequestsRender(), consoleOutput)
            }
        ),
        ScanCommand().subcommands(
            ScanProjectsInteractor(localProjectsRepository, gitlabProjectsRepository, console).asCLICommand("projects"),
            LongIdInteractorCommand("project") {
                ScanProjectInteractor(it, localProjectsRepository, gitlabProjectsRepository).withRender(ASCIISyncProjectResultRender(), consoleOutput)
            }
        ),
        SyncInteractor(localProjectsRepository, gitlabProjectsRepository)
            .withRender(ASCIISyncProjectsRender(), consoleOutput)
            .asCLICommand("sync")
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

class PrintCommand : CliktCommand("print") {
    override fun run(): Unit = Unit
}

class ScanCommand : CliktCommand("scan") {
    override fun run() = Unit
}