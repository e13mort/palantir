/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.PrintMergeRequestInteractor
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.User
import com.jakewharton.picnic.table
import org.jsoup.Jsoup

//todo: extract as config properties
const val MAX_MR_CONTENT_LENGTH = 80
const val APPROVE_MARK = "\uD83D\uDC4D"
const val COMMENT_MARK = "\uD83D\uDCAC"

class ASCIIMergeRequestRender :
    ReportRender<PrintMergeRequestInteractor.MergeRequestsReport, String, Unit> {
    override fun render(
        value: PrintMergeRequestInteractor.MergeRequestsReport,
        params: Unit
    ): String {
        return table {
            cellStyle {
                border = true
            }
            row("Id", value.id)
            row("State", value.state)
            row("From", value.from)
            row("To", value.to)
            row("Created", value.createdMillis.formatAsDate())
            value.closedMillis?.let {
                row("Closed", it.formatAsDate())
            }
            value.assignees.let {
                if (it.isEmpty()) {
                    row {
                        cell("Empty assignees") {
                            columnSpan = 2
                        }
                    }
                } else {
                    row {
                        cell("Assignees") {
                            rowSpan = it.size
                        }
                        cell(format(it[0]))
                    }
                    it.forEachIndexed { index, user ->
                        if (index > 0) {
                            row(format(user))
                        }
                    }
                }

            }
            value.events.let {
                if (it.isEmpty()) {
                    row {
                        cell("There are no events yet") {
                            columnSpan = 2
                        }
                    }
                } else {
                    row {
                        cell("Events") {
                            rowSpan = it.size
                        }
                        cell(format(it[0]))
                    }
                    it.forEachIndexed { index, event ->
                        if (index > 0) {
                            row(format(event))
                        }
                    }
                }
            }
        }.toString()
    }

    private fun format(user: User) = "${user.name()}<${user.userName()}>"

    private fun format(event: MergeRequestEvent) = event.formatted().let {
        "${it.user().name()}<${it.user().userName()}> ${it.timeMillis().formatAsDate()}\n" +
                when (it.type()) {
                    MergeRequestEvent.Type.APPROVE -> APPROVE_MARK
                    MergeRequestEvent.Type.DISCUSSION -> "$COMMENT_MARK ${it.content()}"
                    else -> it.content()
                }
    }

    internal class FormattedMREvent(private val event: MergeRequestEvent) :
        MergeRequestEvent by event {
        override fun content(): String {
            val chunked: List<String> =
                Jsoup.parse(event.content()).text().chunked(MAX_MR_CONTENT_LENGTH)
            return chunked.joinToString(separator = "\n")
        }
    }
}

fun MergeRequestEvent.formatted(): MergeRequestEvent {
    return ASCIIMergeRequestRender.FormattedMREvent(this)
}