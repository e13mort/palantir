/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.local.DriverFactory
import com.e13mort.palantir.model.local.DriverType
import com.e13mort.palantir.model.local.LocalModel

internal fun inMemoryModel(): LocalModel {
    val driver = DriverFactory("").createDriver(type = DriverType.MEMORY)
    return LocalModel(driver)
}