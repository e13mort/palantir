/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

internal suspend fun SyncInteractor.prepareForTest(localComponent: LocalComponent) {
    run(SyncInteractor.SyncStrategy.UpdateProjects).collect { }
    localComponent.projectRepository.projects().collect {
        it.updateSynced(true)
    }
}