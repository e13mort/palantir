/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli

import com.e13mort.palantir.cli.commands.CommandWithRender
import com.e13mort.palantir.cli.commands.LongWithRangesCommand
import com.e13mort.palantir.cli.commands.PrintCommand
import com.e13mort.palantir.cli.commands.RemoveCommand
import com.e13mort.palantir.cli.commands.ReportCommand
import com.e13mort.palantir.cli.commands.ReportCommand.ApprovesCommand
import com.e13mort.palantir.cli.commands.ReportCommand.MR
import com.e13mort.palantir.cli.commands.RepositoryCommand
import com.e13mort.palantir.cli.commands.RootCommand
import com.e13mort.palantir.cli.commands.ScanCommand
import com.e13mort.palantir.cli.commands.StringWithRangesCommand
import com.e13mort.palantir.cli.commands.SyncCommand
import com.e13mort.palantir.cli.commands.asLongCommand
import com.e13mort.palantir.cli.commands.asUnitCommand
import com.e13mort.palantir.cli.commands.asUnitCommandWithUnitCommandParams
import com.e13mort.palantir.cli.commands.asUnitCommandWithUnitRenderParams
import com.e13mort.palantir.cli.render.ASCIIApproveStatisticsRenderer
import com.e13mort.palantir.cli.render.ASCIIBranchesRender
import com.e13mort.palantir.cli.render.ASCIICodeAuthorsReportRender
import com.e13mort.palantir.cli.render.ASCIICodeChangesReportRender
import com.e13mort.palantir.cli.render.ASCIICodeLinesCountReportRender
import com.e13mort.palantir.cli.render.ASCIIFullSyncProjectsRender
import com.e13mort.palantir.cli.render.ASCIIMergeRequestRender
import com.e13mort.palantir.cli.render.ASCIIMergeRequestsRender
import com.e13mort.palantir.cli.render.ASCIIPercentileReportRenderer
import com.e13mort.palantir.cli.render.ASCIISyncProjectsRender
import com.e13mort.palantir.cli.render.ASCIITableProjectRender
import com.e13mort.palantir.cli.render.ASCIITableProjectsListRender
import com.e13mort.palantir.cli.render.ASCIIUserImpactReportRender
import com.e13mort.palantir.cli.render.CSVCodeAuthorsReportRender
import com.e13mort.palantir.cli.render.CSVCodeChangesReportRender
import com.e13mort.palantir.cli.render.CSVCodeLinesCountReportRender
import com.e13mort.palantir.cli.render.CSVUserImpactReportRender
import com.e13mort.palantir.cli.render.CodeChangesReportParams
import com.e13mort.palantir.cli.render.DataColumn
import com.e13mort.palantir.client.properties.EnvironmentProperties
import com.e13mort.palantir.client.properties.FileBasedProperties
import com.e13mort.palantir.client.properties.Properties
import com.e13mort.palantir.client.properties.plus
import com.e13mort.palantir.client.properties.safeIntProperty
import com.e13mort.palantir.client.properties.safeStringProperty
import com.e13mort.palantir.cloc.ClocAdapter
import com.e13mort.palantir.git.GitMailMapFactory
import com.e13mort.palantir.interactors.ApproveStatisticsInteractor
import com.e13mort.palantir.interactors.ApproveStatisticsInteractor.StatisticsType
import com.e13mort.palantir.interactors.CodeChangesReportCalculator
import com.e13mort.palantir.interactors.CodeLinesReportCalculator
import com.e13mort.palantir.interactors.PercentileInteractor
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import com.e13mort.palantir.interactors.PrintMergeRequestInteractor
import com.e13mort.palantir.interactors.PrintProjectBranchesInteractor
import com.e13mort.palantir.interactors.PrintProjectMergeRequestsInteractor
import com.e13mort.palantir.interactors.PrintProjectSummaryInteractor
import com.e13mort.palantir.interactors.Range
import com.e13mort.palantir.interactors.RemoveProjectInteractor
import com.e13mort.palantir.interactors.RepositoryAnalyticsInteractor
import com.e13mort.palantir.interactors.SyncInteractor
import com.e13mort.palantir.model.GitlabNoteRepository
import com.e13mort.palantir.model.GitlabProjectsRepository
import com.e13mort.palantir.model.Percentile
import com.e13mort.palantir.model.local.DBMergeRequestRepository
import com.e13mort.palantir.model.local.DBNotesRepository
import com.e13mort.palantir.model.local.DBProjectRepository
import com.e13mort.palantir.model.local.DBReportsRepository
import com.e13mort.palantir.model.local.DriverFactory
import com.e13mort.palantir.model.local.LocalModel
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.StringDateConverter
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.optionalValue
import com.github.ajalt.clikt.parameters.types.enum
import java.text.SimpleDateFormat

fun main(args: Array<String>) {
    val workDirectory = ProgramWorkDirectory().directory()
    val driver = DriverFactory(workDirectory.toString()).createDriver()
    val model = LocalModel(driver)
    val properties =
        EnvironmentProperties() + FileBasedProperties.defaultInHomeDirectory(workDirectory)

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
    val stringToDateConverter =
        StringDateConverter { string -> SimpleDateFormat(dateFormat).parse(string).time }
    val dateToStringConverter = createDateConverter(dateFormat)
    val requestedPercentilesProperty =
        properties.stringProperty(Properties.StringProperty.PERCENTILES_IN_REPORTS).orEmpty()

    val reportsRepository = DBReportsRepository(model)
    val syncInteractor = SyncInteractor(
        localProjectsRepository,
        gitlabProjectsRepository,
        mrRepository,
        localNotesRepository,
        remoteNotesRepository
    )
    val mapMapFactory = GitMailMapFactory()
    val projectSummaryInteractor = PrintProjectSummaryInteractor(localProjectsRepository)
    val printBranchesInteractor = PrintProjectBranchesInteractor(localProjectsRepository)
    val printMergeRequestsInteractor = PrintProjectMergeRequestsInteractor(localProjectsRepository)
    val printMergeRequestInteractor =
        PrintMergeRequestInteractor(mrRepository, localNotesRepository)
    val printAllProjectsInteractor = PrintAllProjectsInteractor(localProjectsRepository)
    val approveStatisticsInteractor = ApproveStatisticsInteractor(reportsRepository)
    val projectStatisticsInteractor = PercentileInteractor(reportsRepository)
    val removeProjectInteractor = RemoveProjectInteractor(localProjectsRepository)
    val codeLinesInteractor =
        RepositoryAnalyticsInteractor(CodeLinesReportCalculator(ClocAdapter.create()))
    val codeIncrementInteractor =
        RepositoryAnalyticsInteractor(CodeChangesReportCalculator(CodeChangesReportCalculator.CalculationType.FULL, mapMapFactory))
    val codeAuthorsInteractor =
        RepositoryAnalyticsInteractor(CodeChangesReportCalculator(CodeChangesReportCalculator.CalculationType.AUTHORS, mapMapFactory))

    RootCommand().subcommands(
        PrintCommand().subcommands(
            printAllProjectsInteractor.asUnitCommandWithUnitCommandParams(
                name = "projects",
                renders = ASCIITableProjectsListRender().asTableRender(),
                renderParamsMapper = { params ->
                    params.flags.contains("-a")
                },
            ) {
                registerOption(option("-a").flag())
            },
            projectSummaryInteractor.asLongCommand(
                "project",
                ASCIITableProjectRender().asTableRender(),
            ),
            printBranchesInteractor.asLongCommand(
                name = "branches",
                renders = ASCIIBranchesRender().asTableRender()
            ),
            printMergeRequestsInteractor.asLongCommand(
                name = "mrs",
                renders = ASCIIMergeRequestsRender().asTableRender()
            ),
            printMergeRequestInteractor.asLongCommand(
                name = "mr",
                renders = ASCIIMergeRequestRender().asTableRender(),
            )
        ),
        ScanCommand().subcommands(
            syncInteractor.asUnitCommandWithUnitRenderParams(
                name = "projects",
                renders = ASCIISyncProjectsRender().asTableRender(),
                commandParamsMapper = { SyncInteractor.SyncStrategy.UpdateProjects },
            ),
        ),
        SyncCommand().subcommands(
            syncInteractor.asLongCommand(
                name = "project",
                renders = ASCIIFullSyncProjectsRender().asTableRender(),
                commandParamMapper = { params, projectId ->
                    val forceSync = params.flags.contains("-f") || params.flags.contains("--force")
                    SyncInteractor.SyncStrategy.FullSyncForProject(projectId, forceSync)
                }
            ).apply {
                registerOption(option("-f", "--force", "Force sync all content").flag())
            },
            syncInteractor.asUnitCommand(
                name = "active",
                renders = ASCIIFullSyncProjectsRender().asTableRender(),
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
                renders = DoneOperationRenderer.asTableRender()
            ),
        ),
        ReportCommand().subcommands(
            ApprovesCommand().subcommands(
                approveStatisticsInteractor.asLongCommand(
                    name = "total",
                    renders = ASCIIApproveStatisticsRenderer().asTableRender(),
                    commandParamMapper = { _, projectId ->
                        ApproveStatisticsInteractor.Params(
                            projectId,
                            StatisticsType.TOTAL_APPROVES
                        )
                    }
                ),
                approveStatisticsInteractor.asLongCommand(
                    name = "first",
                    renders = ASCIIApproveStatisticsRenderer().asTableRender(),
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
                    renders = ASCIIPercentileReportRenderer(dateToStringConverter).asTableRender(),
                    renderValueMapper = { it },
                    renderParamsMapper = { Percentile.fromString(requestedPercentilesProperty) },
                    commandParamMapper = { a, b -> a to b },
                    dateFormat = stringToDateConverter
                )
            ),
            RepositoryCommand().subcommands(
                StringWithRangesCommand(
                    name = "codelines",
                    interactor = codeLinesInteractor,
                    renders = mapOf(
                        CommandWithRender.RenderType.Table to ASCIICodeLinesCountReportRender(
                            dateToStringConverter
                        ),
                        CommandWithRender.RenderType.CSV to CSVCodeLinesCountReportRender(
                            dateToStringConverter
                        )
                    ),
                    renderValueMapper = { it },
                    commandParamMapper = { params, ranges ->
                        RepositoryAnalyticsInteractor.Arguments(
                            ranges,
                            params.allOptions.findOption<String>("--group")
                        )

                    },
                    renderParamsMapper = {},
                    dateFormat = stringToDateConverter
                ),
                StringWithRangesCommand(
                    name = "codeincrement",
                    interactor = codeIncrementInteractor,
                    renders = mapOf(
                        CommandWithRender.RenderType.Table to ASCIICodeChangesReportRender(
                            dateToStringConverter
                        ),
                        CommandWithRender.RenderType.CSV to CSVCodeChangesReportRender(
                            dateToStringConverter
                        )
                    ),
                    renderValueMapper = { it },
                    commandParamMapper = { params, ranges ->
                        RepositoryAnalyticsInteractor.Arguments(
                            ranges,
                            params.allOptions.findOptionSafe<String>("--group")
                        )
                    },
                    renderParamsMapper = { commandParams ->
                        commandParams.flags.mapNotNull {
                            when (it) {
                                "--full-commits" -> CodeChangesReportParams.ShowFullCommitsList
                                else -> null
                            }
                        }.toSet()
                    },
                    dateFormat = stringToDateConverter
                ).apply {
                    registerOption(option("--full-commits").flag())
                    registerOption(option("--group"))
                },
                StringWithRangesCommand(
                    name = "impact",
                    interactor = codeIncrementInteractor,
                    renders = mapOf(
                        CommandWithRender.RenderType.Table to ASCIIUserImpactReportRender(
                            dateToStringConverter
                        ),
                        CommandWithRender.RenderType.CSV to CSVUserImpactReportRender(
                            dateToStringConverter
                        ),
                    ),
                    renderValueMapper = { it },
                    commandParamMapper = { params, ranges ->
                        RepositoryAnalyticsInteractor.Arguments(
                            ranges,
                            params.allOptions.findOptionSafe<String>("--group")
                        )
                    },
                    renderParamsMapper = {
                        it.allOptions.findOption("--type")
                    },
                    dateFormat = stringToDateConverter
                ).apply {
                    registerOption(option("--type").enum<DataColumn>().default(DataColumn.Changed) )
                    registerOption(option("--group"))
                },
                StringWithRangesCommand(
                    name = "authors",
                    interactor = codeAuthorsInteractor,
                    renders = mapOf(
                        CommandWithRender.RenderType.Table to ASCIICodeAuthorsReportRender(
                            dateToStringConverter
                        ),
                        CommandWithRender.RenderType.CSV to CSVCodeAuthorsReportRender(
                            dateToStringConverter
                        )
                    ),
                    renderValueMapper = { it },
                    commandParamMapper = { params, ranges ->
                        RepositoryAnalyticsInteractor.Arguments(
                            ranges,
                            params.allOptions.findOptionSafe<String>("--group")
                        )
                    },
                    renderParamsMapper = {
                        emptySet()
                    },
                    dateFormat = stringToDateConverter
                ).apply {
                    registerOption(option("--group"))
                }


            ),
        )
    ).main(args)
}

private fun createDateConverter(dateFormat: String): DateStringConverter {
    return DateStringConverter { date -> SimpleDateFormat(dateFormat).format(date) }
}

private fun <A, B, C> ReportRender<A, B, C>.asTableRender(): Map<CommandWithRender.RenderType, ReportRender<A, B, C>> {
    return mapOf(CommandWithRender.RenderType.Table to this)
}