package com.e13mort.palantir.client.ui.presentation

import kotlinx.coroutines.flow.StateFlow
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.SetNavigator

class MainAppPM(pmParams: PmParams) : PresentationModel(pmParams) {
    companion object Description : PmDescription

    enum class TopLevelItems(val description: PmDescription) {
        PROJECTS(ProjectsScreenPM.Description),
        REPORTS(MRReportsPM.Description),
        SETTINGS(SettingsPM.Description)
    }

    private val navigator = SetNavigator(
        initialDescriptions = TopLevelItems.values().map {
            it.description
        }.toTypedArray(),
        onChangeCurrent = { index: Int, navigator: SetNavigator ->

        }
    )

    val states: StateFlow<PresentationModel?>
        get() = navigator.currentFlow

    val menuItems: List<TopLevelItems>
        get() = TopLevelItems.values().asList()

    fun switchToItem(item: TopLevelItems) {
        navigator.current?.messageHandler?.send(RootChangedMessage)
        navigator.changeCurrent(item.ordinal)
    }
}