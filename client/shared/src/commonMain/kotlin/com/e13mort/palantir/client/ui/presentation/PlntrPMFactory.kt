package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.client.properties.Properties
import com.e13mort.palantir.client.properties.safeIntProperty
import com.e13mort.palantir.client.properties.safeStringProperty
import com.e13mort.palantir.interactors.PercentileInteractor
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import com.e13mort.palantir.interactors.ScanProjectInteractor
import com.e13mort.palantir.model.GitlabProjectsRepository
import com.e13mort.palantir.model.ReportsRepository
import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.model.local.DBMergeRequestRepository
import com.e13mort.palantir.model.local.DBProjectRepository
import com.e13mort.palantir.model.local.DBReportsRepository
import com.e13mort.palantir.model.local.LocalModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.dmdev.premo.PmFactory
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import java.text.SimpleDateFormat

class PlntrPMFactory(
    properties: Properties,
    model: LocalModel,
    private val mainScope: CoroutineScope
) : PmFactory {

    private val localProjectsRepository = DBProjectRepository(model)
    private val mrRepository = DBMergeRequestRepository(model)
    private val reportsRepository = DBReportsRepository(model)
    private val gitlabProjectsRepository = GitlabProjectsRepository(
        properties.safeStringProperty(Properties.StringProperty.GITLAB_URL),
        properties.safeStringProperty(Properties.StringProperty.GITLAB_KEY),
        properties.safeIntProperty(Properties.IntProperty.SYNC_PERIOD_MONTHS)
    )
    private val dateFormat =
        properties.safeStringProperty(Properties.StringProperty.PERIOD_DATE_FORMAT)
    private val requestedPercentilesProperty =
        properties.stringProperty(Properties.StringProperty.PERCENTILES_IN_REPORTS).orEmpty()
    private val dateToStringConverter = DateStringConverter { date -> SimpleDateFormat(dateFormat).format(date) }
    private val requestedPercentiles = ReportsRepository.Percentile.fromString(requestedPercentilesProperty)
    private val allProjectsInteractor = PrintAllProjectsInteractor(localProjectsRepository)
    private val backgroundDispatcher = Dispatchers.IO

    override fun createPm(params: PmParams): PresentationModel {
        return when (params.description) {
            is ConfigureActiveProjectsPM.Description -> createProjectListPM(params)
            is SettingsPM.Description -> SettingsPM(params)
            is MainAppPM.Description -> MainAppPM(params)
            is ProjectsScreenPM.Description -> ProjectsScreenPM(params)
            is MRReportsPM.Description -> MRReportsPM(
                params,
                backgroundDispatcher,
                allProjectsInteractor,
                dateToStringConverter,
                requestedPercentiles,
            ) { projectId, ranges ->
                PercentileInteractor(reportsRepository, projectId, ranges)
            }

            is ActiveProjectsPM.Description -> ActiveProjectsPM(
                params,
                allProjectsInteractor,
                mainScope,
                backgroundDispatcher
            )

            else ->
                throw IllegalArgumentException("Missed description handler: ${params.description}")
        }
    }

    private fun createProjectListPM(params: PmParams): ConfigureActiveProjectsPM {
        return ConfigureActiveProjectsPM(
            pmParams = params,
            PrintAllProjectsInteractor(localProjectsRepository),
            projectSyncInteractorFactory = { projectId -> //todo: refactor
                ScanProjectInteractor(
                    projectId,
                    localProjectsRepository,
                    gitlabProjectsRepository,
                    object : SyncableProjectRepository.SyncableProject.SyncCallback {
                        override fun onBranchEvent(branchEvent: SyncableProjectRepository.SyncableProject.UpdateBranchesCallback.BranchEvent) {

                        }

                        override fun onMREvent(event: SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent) {

                        }

                    }).run()
            },
            main = mainScope,
            backgroundDispatcher = backgroundDispatcher
        )
    }

}