package com.e13mort.gitlab_report.cli

import com.e13mort.gitlab_report.cli.render.*
import com.e13mort.gitlab_report.interactors.*
import com.e13mort.gitlab_report.model.GitlabProjectsRepository
import com.e13mort.gitlab_report.model.local.*
import com.e13mort.gitlab_report.utils.Console
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    val driver = DriverFactory().createDriver()
    val model = LocalModel(driver)

    val localProjectsRepository = DBProjectRepository(model)
    val mrRepository = DBMergeRequestRepository(model)
    val gitlabProjectsRepository = GitlabProjectsRepository("***REMOVED***/", "***REMOVED***")
    val console = ConsoleImpl()

    val consoleOutput = ConsoleRenderOutput(console)
    val syncCallback = ASCIISyncCallback(console)

    val reportsRepository = DBReportsRepository(model)
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
            LongIdInteractorCommand("mrs") {
                PrintProjectMergeRequestsInteractor(localProjectsRepository, it).withRender(ASCIIMergeRequestsRender(), consoleOutput)
            },
            LongIdInteractorCommand("mr") {
                PrintMergeRequestInteractor(mrRepository, it).withRender(ASCIIMergeRequestRender(), consoleOutput)
            }
        ),
        ScanCommand().subcommands(
            ScanProjectsInteractor(localProjectsRepository, gitlabProjectsRepository, console).asCLICommand("projects"),
            LongIdInteractorCommand("project") {
                ScanProjectInteractor(it, localProjectsRepository, gitlabProjectsRepository, syncCallback).withRender(ASCIISyncProjectResultRender(), consoleOutput)
            }
        ),
        SyncInteractor(localProjectsRepository, gitlabProjectsRepository, syncCallback)
            .withRender(ASCIISyncProjectsRender(), consoleOutput)
            .asCLICommand("sync"),
        ReportCommand().subcommands(
            ApproveStatisticsInteractor(reportsRepository).withRender(ASCIIApproveStatisticsRenderer(), consoleOutput).asCLICommand("approves")
        )
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

class ReportCommand : CliktCommand("report") {
    override fun run() = Unit
}