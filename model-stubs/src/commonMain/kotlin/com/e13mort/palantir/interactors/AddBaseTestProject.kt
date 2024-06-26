/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.MergeRequestEvent

fun RemoteProjectRepositoryBuilder.addBaseTestProject() {
    project {
        mr {
            state = MergeRequest.State.OPEN
            assignees {
                addStubUser()
            }
            events {
                event {
                    type = MergeRequestEvent.Type.DISCUSSION
                    stubUser(1)
                    content = "discussion"
                    time = 0
                }
                event {
                    type = MergeRequestEvent.Type.APPROVE
                    stubUser(1)
                    content = "approve"
                    time = 1
                }
            }
            sourceBranch = "dev"
            targetBranch = "master"
        }
        branches {
            add("master")
            add("dev")
        }
    }

}