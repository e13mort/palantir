/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.git

interface MailMap {
    data class FullMappedIdentity(val name: String, val email: String)

    fun mapIdentity(commitEmail: String, commitName: String): FullMappedIdentity

    companion object Empty : MailMap {
        override fun mapIdentity(
            commitEmail: String,
            commitName: String
        ) : FullMappedIdentity {
            return FullMappedIdentity(commitName, commitEmail)
        }
    }
}