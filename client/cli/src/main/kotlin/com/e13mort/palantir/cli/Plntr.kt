package com.e13mort.palantir.cli

import com.e13mort.palantir.cli.commands.LongWithRangesCommand
import com.e13mort.palantir.cli.commands.PrintCommand
import com.e13mort.palantir.cli.commands.RemoveCommand
import com.e13mort.palantir.cli.commands.ReportCommand
import com.e13mort.palantir.cli.commands.ReportCommand.ApprovesCommand
import com.e13mort.palantir.cli.commands.ReportCommand.MR
import com.e13mort.palantir.cli.commands.RootCommand
import com.e13mort.palantir.cli.commands.ScanCommand
import com.e13mort.palantir.cli.commands.SyncCommand
import com.e13mort.palantir.cli.commands.asLongCommand
import com.e13mort.palantir.cli.commands.asUnitCommand
import com.e13mort.palantir.cli.commands.asUnitCommandWithUnitCommandParams
import com.e13mort.palantir.cli.commands.asUnitCommandWithUnitRenderParams
import com.e13mort.palantir.cli.render.ASCIIApproveStatisticsRenderer
import com.e13mort.palantir.cli.render.ASCIIBranchesRender
import com.e13mort.palantir.cli.render.ASCIIFullSyncProjectsRender
import com.e13mort.palantir.cli.render.ASCIIMergeRequestRender
import com.e13mort.palantir.cli.render.ASCIIMergeRequestsRender
import com.e13mort.palantir.cli.render.ASCIIPercentileReportRenderer
import com.e13mort.palantir.cli.render.ASCIISyncProjectsRender
import com.e13mort.palantir.cli.render.ASCIITableProjectRender
import com.e13mort.palantir.cli.render.ASCIITableProjectsListRender
import com.e13mort.palantir.client.properties.EnvironmentProperties
import com.e13mort.palantir.client.properties.FileBasedProperties
import com.e13mort.palantir.client.properties.Properties
import com.e13mort.palantir.client.properties.plus
import com.e13mort.palantir.client.properties.safeIntProperty
import com.e13mort.palantir.client.properties.safeStringProperty
import com.e13mort.palantir.interactors.ApproveStatisticsInteractor
import com.e13mort.palantir.interactors.ApproveStatisticsInteractor.StatisticsType
import com.e13mort.palantir.interactors.PercentileInteractor
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import com.e13mort.palantir.interactors.PrintMergeRequestInteractor
import com.e13mort.palantir.interactors.PrintProjectBranchesInteractor
import com.e13mort.palantir.interactors.PrintProjectMergeRequestsInteractor
import com.e13mort.palantir.interactors.PrintProjectSummaryInteractor
import com.e13mort.palantir.interactors.RemoveProjectInteractor
import com.e13mort.palantir.interactors.SyncInteractor
import com.e13mort.palantir.model.GitlabNoteRepository
import com.e13mort.palantir.model.GitlabProjectsRepository
import com.e13mort.palantir.model.ReportsRepository
import com.e13mort.palantir.model.local.DBMergeRequestRepository
import com.e13mort.palantir.model.local.DBNotesRepository
import com.e13mort.palantir.model.local.DBProjectRepository
import com.e13mort.palantir.model.local.DBReportsRepository
import com.e13mort.palantir.model.local.DriverFactory
import com.e13mort.palantir.model.local.LocalModel
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.StringDateConverter
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.text.SimpleDateFormat

fun main(args: Array<String>) {
    val workDirectory = ProgramWorkDirectory().directory()
    val driver = DriverFactory(workDirectory.toString()).createDriver()
    val model = LocalModel(driver)
    val properties = EnvironmentProperties() + FileBasedProperties.defaultInHomeDirectory(workDirectory)

    val localProjectsRepository = DBProjectRepository(model)
    val mrRepository = DBMergeRequestRepository(model)
    val localNotesRepository = DBNotesRepository(model)
    val gitlabProjectsRepository = GitlabProjectsRepository(
        properties.safeStringProperty(Properties.StringProperty.GITLAB_URL),
        properties.safeStringProperty(Properties.StringProperty.GITLAB_KEY),
        properties.safeIntProperty(Properties.IntProperty.SYNC_PERIOD_MONTHS)
    )
    val remoteNotesRepository = GitlabNoteRepository(
        properties.safeStringProperty(Properties.StringProperty.GITLAB_URL),
        properties.safeStringProperty(Properties.StringProperty.GITLAB_KEY)
    )
    val dateFormat = properties.safeStringProperty(Properties.StringProperty.PERIOD_DATE_FORMAT)
    val stringToDateConverter = StringDateConverter { string -> SimpleDateFormat(dateFormat).parse(string).time }
    val requestedPercentilesProperty = properties.stringProperty(Properties.StringProperty.PERCENTILES_IN_REPORTS).orEmpty()

    val reportsRepository = DBReportsRepository(model)
    val syncInteractor = SyncInteractor(
        localProjectsRepository,
        gitlabProjectsRepository,
        mrRepository,
        localNotesRepository,
        remoteNotesRepository
    )
    val projectSummaryInteractor = PrintProjectSummaryInteractor(localProjectsRepository)
    val printBranchesInteractor = PrintProjectBranchesInteractor(localProjectsRepository)
    val printMergeRequestsInteractor = PrintProjectMergeRequestsInteractor(localProjectsRepository)
    val printMergeRequestInteractor = PrintMergeRequestInteractor(mrRepository, localNotesRepository)
    val printAllProjectsInteractor = PrintAllProjectsInteractor(localProjectsRepository)
    val approveStatisticsInteractor = ApproveStatisticsInteractor(reportsRepository)
    val projectStatisticsInteractor = PercentileInteractor(reportsRepository)
    val removeProjectInteractor = RemoveProjectInteractor(localProjectsRepository)

    RootCommand().subcommands(
        PrintCommand().subcommands(
            printAllProjectsInteractor.asUnitCommandWithUnitCommandParams(
                name = "projects",
                render = ASCIITableProjectsListRender(),
                renderParamsMapper = { params ->
                    params.flags.contains("-a")
                },
            ) {
                registerOption(option("-a").flag())
            },
            projectSummaryInteractor.asLongCommand(
                "project",
                ASCIITableProjectRender(),
            ),
            printBranchesInteractor.asLongCommand(
                name = "branches",
                render = ASCIIBranchesRender()
            ),
            printMergeRequestsInteractor.asLongCommand(
                name = "mrs",
                render = ASCIIMergeRequestsRender()
            ),
            printMergeRequestInteractor.asLongCommand(
                name = "mr",
                render = ASCIIMergeRequestRender(),
            )
        ),
        ScanCommand().subcommands(
            syncInteractor.asUnitCommandWithUnitRenderParams(
                name = "projects",
                render = ASCIISyncProjectsRender(),
                commandParamsMapper = { SyncInteractor.SyncStrategy.UpdateProjects },
            ),
        ),
        SyncCommand().subcommands(
            syncInteractor.asLongCommand(
                name = "project",
                render = ASCIIFullSyncProjectsRender(),
                commandParamMapper = { params, projectId ->
                    val forceSync = params.flags.contains("-f") || params.flags.contains("--force")
                    SyncInteractor.SyncStrategy.FullSyncForProject(projectId, forceSync)
                }
            ).apply {
                registerOption(option("-f", "--force", "Force sync all content").flag())
            },
            syncInteractor.asUnitCommand(
                name = "active",
                render = ASCIIFullSyncProjectsRender(),
                renderParamsMapper = {},
                commandParamsMapper = { params ->
                    val forceSync = params.flags.contains("-f") || params.flags.contains("--force")
                    SyncInteractor.SyncStrategy.FullSyncForActiveProjects(forceSync)
                }
            ).apply {
                registerOption(option("-f", "--force", "Force sync all content").flag())
            }
        ),
        RemoveCommand().subcommands(
            removeProjectInteractor.asLongCommand(
                name = "project",
                render = DoneOperationRenderer
            ),
        ),
        ReportCommand().subcommands(
            ApprovesCommand().subcommands(
                approveStatisticsInteractor.asLongCommand(
                    name = "total",
                    render = ASCIIApproveStatisticsRenderer(),
                    commandParamMapper = { _, projectId ->
                        ApproveStatisticsInteractor.Params(
                            projectId,
                            StatisticsType.TOTAL_APPROVES
                        )
                    }
                ),
                approveStatisticsInteractor.asLongCommand(
                    name = "first",
                    render = ASCIIApproveStatisticsRenderer(),
                    commandParamMapper = { _, projectId ->
                        ApproveStatisticsInteractor.Params(
                            projectId,
                            StatisticsType.FIRST_APPROVES
                        )
                    }
                ),
            ),
            MR().subcommands(
                LongWithRangesCommand(
                    name = "start",
                    interactor = projectStatisticsInteractor,
                    render = ASCIIPercentileReportRenderer(
                        createDateConverter(dateFormat)
                    ),
                    renderValueMapper = { it },
                    renderParamsMapper = { ReportsRepository.Percentile.fromString(requestedPercentilesProperty) },
                    commandParamMapper = { a, b -> a to b },
                    dateFormat = stringToDateConverter
                )
            )
        )
    ).main(args)
}

private fun createDateConverter(dateFormat: String) : DateStringConverter {
    return DateStringConverter { date -> SimpleDateFormat(dateFormat).format(date) }
}