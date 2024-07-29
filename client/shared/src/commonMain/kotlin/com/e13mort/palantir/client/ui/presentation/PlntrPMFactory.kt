/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.ui.presentation

import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.client.properties.Properties
import com.e13mort.palantir.client.properties.safeIntProperty
import com.e13mort.palantir.client.properties.safeStringProperty
import com.e13mort.palantir.interactors.PercentileInteractor
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import com.e13mort.palantir.model.GitlabProjectsRepository
import com.e13mort.palantir.model.Percentile
import com.e13mort.palantir.model.local.DBMergeRequestRepository
import com.e13mort.palantir.model.local.DBProjectRepository
import com.e13mort.palantir.model.local.DBReportsRepository
import com.e13mort.palantir.model.local.LocalModel
import com.e13mort.palantir.utils.RangeParser
import com.e13mort.palantir.utils.StringDateConverter
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
    private val dateToStringConverter =
        DateStringConverter { date -> SimpleDateFormat(dateFormat).format(date) }
    private val stringToDateConverter =
        StringDateConverter { string -> SimpleDateFormat(dateFormat).parse(string).time }
    private val requestedPercentiles = Percentile.fromString(requestedPercentilesProperty)
    private val allProjectsInteractor = PrintAllProjectsInteractor(localProjectsRepository)
    private val percentileInteractor = PercentileInteractor(reportsRepository)
    private val rangeParser = RangeParser(stringToDateConverter)
    private val backgroundDispatcher = Dispatchers.IO

    override fun createPm(params: PmParams): PresentationModel {
        return when (params.description) {
            is ConfigureActiveProjectsPM.Description -> createProjectListPM(params)
            is SettingsPM.Description -> SettingsPM(params)
            is MainAppPM.Description -> MainAppPM(params)
            is ProjectsScreenPM.Description -> ProjectsScreenPM(params)
            is MRReportsPM.Description -> {
                MRReportsPM(
                    params,
                    backgroundDispatcher,
                    allProjectsInteractor,
                    dateToStringConverter,
                    rangeParser,
                    requestedPercentiles,
                    percentileInteractor
                )
            }

            is ActiveProjectsPM.Description -> ActiveProjectsPM(
                params,
                allProjectsInteractor,
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
            backgroundDispatcher = backgroundDispatcher
        )
    }

}