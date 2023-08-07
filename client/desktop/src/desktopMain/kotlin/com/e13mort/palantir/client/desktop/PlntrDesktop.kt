package com.e13mort.palantir.client.desktop

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.e13mort.palantir.cli.ProgramWorkDirectory
import com.e13mort.palantir.client.properties.EnvironmentProperties
import com.e13mort.palantir.client.properties.FileBasedProperties
import com.e13mort.palantir.client.properties.Properties
import com.e13mort.palantir.client.properties.plus
import com.e13mort.palantir.client.properties.safeIntProperty
import com.e13mort.palantir.client.properties.safeStringProperty
import com.e13mort.palantir.interactors.AllProjectsReport
import com.e13mort.palantir.interactors.Interactor
import com.e13mort.palantir.interactors.PrintAllProjectsInteractor
import com.e13mort.palantir.model.GitlabProjectsRepository
import com.e13mort.palantir.model.Project
import com.e13mort.palantir.model.local.DBMergeRequestRepository
import com.e13mort.palantir.model.local.DBProjectRepository
import com.e13mort.palantir.model.local.DriverFactory
import com.e13mort.palantir.model.local.LocalModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun main() = singleWindowApplication(
    title = "Palantir",
    state = WindowState()
) {
    val scope = rememberCoroutineScope()
    val app = remember { //for a start
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
        val requestedPercentilesProperty = properties.stringProperty(Properties.StringProperty.PERCENTILES_IN_REPORTS).orEmpty()


        PlntrDesktop(
            scope,
            PrintAllProjectsInteractor(localProjectsRepository)
        )
    }

    LaunchedEffect("start") {
        app.start()
    }

    val state = app.state.collectAsState().value
    RenderLoading(state.loading)
    RenderProjects(state.project)

//    PalantirView()
}

class PlntrDesktop(
    private val coroutineScope: CoroutineScope,
    private val allProjectsInteractor: Interactor<AllProjectsReport>
) {
    data class State(
        var loading: Boolean,
        var project: List<Project> = emptyList()
    )

    private val _state = MutableStateFlow(State(true))
    val state: StateFlow<State> = _state

    fun start() {
        coroutineScope.launch {
            val projects = async {
                withContext(Dispatchers.IO) {
                    val projectsReport = allProjectsInteractor.run()
                    mutableListOf<Project>().apply {
                        projectsReport.walk { project, synced ->
                            add(project)
                        }
                    }
                }
            }.await()
            _state.value = State(false, projects)
        }

    }
}

@Composable
private fun RenderLoading(loading: Boolean) {
    if (loading) {
        Text("Loading")
    }
}

@Composable
private fun RenderProjects(projects: List<Project>) {
    LazyColumn {
        items(projects) {
            Text(it.name())
        }
    }
}

