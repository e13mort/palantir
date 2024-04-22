/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.ui.presentation

import kotlinx.coroutines.flow.StateFlow
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.handle
import me.dmdev.premo.navigation.BackMessage
import me.dmdev.premo.navigation.StackNavigator
import me.dmdev.premo.navigation.pop
import me.dmdev.premo.navigation.popToRoot
import me.dmdev.premo.navigation.push

class ProjectsScreenPM(pmParams: PmParams) : PresentationModel(pmParams) {
    companion object Description : PmDescription

    private val navigator = StackNavigator(ActiveProjectsPM.Description())

    init {
        messageHandler.handle<BackMessage> {
            navigator.pop()
        }
        messageHandler.handle<ActiveProjectsPM.ConfigureMessage> {
            navigator.push(Child(ConfigureActiveProjectsPM.Description))
            true
        }
        messageHandler.handle<RootChangedMessage> { navigator.popToRoot() }
    }

    fun screens(): StateFlow<PresentationModel?> {
        return navigator.currentTopFlow
    }
}