/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

data class StubMergeRequests(
    val project: Project,
    val requests: List<MergeRequest>
) : MergeRequests {
    override suspend fun count(): Long = requests.size.toLong()

    override suspend fun values(): Flow<MergeRequest> = requests.asFlow()
}

