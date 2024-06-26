/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.User

data class StubUser(
    val id: Long,
    var name: String = "StubUser$id",
    var userName: String = "StubUser$id"
) : User {
    override fun id(): Long = id

    override fun name(): String = name

    override fun userName(): String = userName

}