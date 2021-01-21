package com.e13mort.gitlab_report.cli

import com.e13mort.gitlab_report.cli.ReportCommand.ApprovesCommand
import com.e13mort.gitlab_report.cli.ReportCommand.MR
import com.e13mort.gitlab_report.cli.render.*
import com.e13mort.gitlab_report.interactors.*
import com.e13mort.gitlab_report.interactors.ApproveStatisticsInteractor.StatisticsType
import com.e13mort.gitlab_report.model.GitlabProjectsRepository
import com.e13mort.gitlab_report.model.local.*
import com.e13mort.gitlab_report.utils.Console
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import kotlin.IllegalStateException

fun main(args: Array<String>) {
    val driver = DriverFactory().createDriver()
    val model = LocalModel(driver)
    val properties = EnvironmentProperties()

    val localProjectsRepository = DBProjectRepository(model)
    val mrRepository = DBMergeRequestRepository(model)
    val gitlabProjectsRepository = GitlabProjectsRepository(
        properties.stringProperty(Properties.StringProperty.GITLAB_URL),
        properties.stringProperty(Properties.StringProperty.GITLAB_KEY)
    )
    val console = createConsole()

    val consoleOutput = ConsoleRenderOutput(console)
    val syncCallback = ASCIISyncCallback(console)

    val reportsRepository = DBReportsRepository(model, driver)
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
            ApprovesCommand().subcommands(
                ApproveStatisticsInteractor(reportsRepository, StatisticsType.TOTAL_APPROVES).withRender(ASCIIApproveStatisticsRenderer(), consoleOutput).asCLICommand("total"),
                ApproveStatisticsInteractor(reportsRepository, StatisticsType.FIRST_APPROVES).withRender(ASCIIApproveStatisticsRenderer(), consoleOutput).asCLICommand("first")
            ),
            MR().subcommands(
                PercentileInteractor(reportsRepository).withRender(ASCIIPercentileReportRenderer(), consoleOutput).asCLICommand("first")
            )
        )
    ).main(args)
}

private fun createConsole() : Console {
    return System.console().let {
        if (it != null) ConsoleImpl(it) else object : Console {
            override fun write(message: String, writeStyle: Console.WriteStyle) {
                println(message)
            }
        }
    }
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

    class ApprovesCommand : CliktCommand("approves") {
        override fun run() = Unit
    }

    class MR : CliktCommand("mr") {
        override fun run() = Unit
    }
}

interface Properties {
    enum class StringProperty {
        GITLAB_KEY,
        GITLAB_URL
    }

    fun stringProperty(property: StringProperty): String
}

class EnvironmentProperties : Properties {

    override fun stringProperty(property: Properties.StringProperty): String {
        val propertyName = "PALANTIR_${property.name.toUpperCase()}"
        return System.getenv(propertyName) ?: throw IllegalStateException("Please provide property $propertyName via env vars")
    }

}