package com.e13mort.palantir.client.desktop

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.e13mort.palantir.cli.ProgramWorkDirectory
import com.e13mort.palantir.client.properties.EnvironmentProperties
import com.e13mort.palantir.client.properties.FileBasedProperties
import com.e13mort.palantir.client.properties.plus
import com.e13mort.palantir.client.ui.compose.Plntr
import com.e13mort.palantir.client.ui.compose.Render
import com.e13mort.palantir.client.ui.presentation.MainAppPM
import com.e13mort.palantir.client.ui.presentation.PlntrPMFactory
import com.e13mort.palantir.model.local.DriverFactory
import com.e13mort.palantir.model.local.LocalModel
import kotlinx.coroutines.CoroutineScope
import me.dmdev.premo.PmDelegate
import me.dmdev.premo.PmParams
import me.dmdev.premo.saver.PmStateSaver
import me.dmdev.premo.saver.PmStateSaverFactory
import kotlin.reflect.KType

fun main() = singleWindowApplication(
    title = "Palantir",
    state = WindowState()
) {
    val scope = rememberCoroutineScope()
    val app = remember {
        PlntrDesktop.createPMDelegate(scope).let {
            it.onCreate()
            it.onForeground()
            it.presentationModel
        }
    }

    Plntr.Theme {
        app.Render()
    }
}

object PlntrDesktop {
    fun createPMDelegate(mainScope: CoroutineScope): PmDelegate<MainAppPM> {

        val workDirectory = ProgramWorkDirectory().directory()
        val driver = DriverFactory(workDirectory.toString()).createDriver()
        val model = LocalModel(driver)
        val properties =
            EnvironmentProperties() + FileBasedProperties.defaultInHomeDirectory(workDirectory)

        return PmDelegate(
            pmParams = PmParams(
                description = MainAppPM.Description,
                parent = null,
                factory = PlntrPMFactory(properties, model, mainScope),
                stateSaverFactory = object : PmStateSaverFactory {
                    override fun createPmStateSaver(key: String): PmStateSaver {
                        return object : PmStateSaver {
                            override fun <T> restoreState(key: String, kType: KType): T? = null

                            override fun <T> saveState(key: String, kType: KType, value: T?) = Unit

                        }
                    }

                    override fun deletePmStateSaver(key: String) = Unit

                }
            )
        )
    }
}