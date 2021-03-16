package com.e13mort.palantir.cli

import com.e13mort.palantir.cli.ReportCommand.ApprovesCommand
import com.e13mort.palantir.cli.ReportCommand.MR
import com.e13mort.palantir.cli.properties.*
import com.e13mort.palantir.cli.render.*
import com.e13mort.palantir.interactors.*
import com.e13mort.palantir.interactors.ApproveStatisticsInteractor.StatisticsType
import com.e13mort.palantir.model.GitlabProjectsRepository
import com.e13mort.palantir.model.local.*
import com.e13mort.palantir.utils.Console
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    val workDirectory = ProgramWorkDirectory().directory()
    val driver = DriverFactory(workDirectory.toString()).createDriver()
    val model = LocalModel(driver)
    val properties = EnvironmentProperties() + FileBasedProperties.defaultInHomeDirectory(workDirectory)

    val localProjectsRepository = DBProjectRepository(model)
    val mrRepository = DBMergeRequestRepository(model)
    val gitlabProjectsRepository = GitlabProjectsRepository(
        properties.safeStringProperty(Properties.StringProperty.GITLAB_URL),
        properties.safeStringProperty(Properties.StringProperty.GITLAB_KEY),
        properties.safeIntProperty(Properties.IntProperty.SYNC_PERIOD_MONTHS)
    )
    val dateFormat = properties.safeStringProperty(Properties.StringProperty.PERIOD_DATE_FORMAT)
    val console = createConsole()

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
            ApprovesCommand().subcommands(
                LongIdInteractorCommand("total") {
                    ApproveStatisticsInteractor(reportsRepository, it, StatisticsType.TOTAL_APPROVES).withRender(ASCIIApproveStatisticsRenderer(), consoleOutput)
                },
                LongIdInteractorCommand("first") {
                    ApproveStatisticsInteractor(reportsRepository, it, StatisticsType.FIRST_APPROVES).withRender(ASCIIApproveStatisticsRenderer(), consoleOutput)
                }
            ),
            MR().subcommands(
                IdWithTimeIntervalCommand("start", dateFormat) { id, start, end ->
                    PercentileInteractor(reportsRepository, id, start, end).withRender(
                        ASCIIPercentileReportRenderer(),
                        consoleOutput
                    )
                }
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

class RootCommand : CliktCommand(name = "plntr") {
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