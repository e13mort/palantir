package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.PrintProjectMergeRequestsInteractor
import com.e13mort.palantir.interactors.ReportRender
import com.jakewharton.picnic.table
import kotlinx.coroutines.runBlocking
import java.text.DateFormat
import java.util.*

class ASCIIMergeRequestsRender : ReportRender<PrintProjectMergeRequestsInteractor.MergeRequestsReport, String> {

    internal object RenderingSettings {
        const val branchMaxLength = 40
        val headers = arrayOf("ID", "From", "To", "Created", "Closed", "State")
    }

    override fun render(value: PrintProjectMergeRequestsInteractor.MergeRequestsReport): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row(*RenderingSettings.headers)
            }
            runBlocking {
                value.walk { id: String, sourceBranch: String, targetBranch: String, created: Long, closed: Long?, state: String ->
                    val createdString = created.formatAsDate()
                    val closedString = closed?.formatAsDate() ?: "-"
                    row(
                        id,
                        stripBranchName(sourceBranch),
                        stripBranchName(targetBranch),
                        createdString,
                        closedString,
                        state
                    )
                }
            }
        }.toString()
    }

    private fun stripBranchName(name: String): String {
        if (name.length <= RenderingSettings.branchMaxLength) return name
        return name.substring(0, RenderingSettings.branchMaxLength) + Typography.ellipsis
    }
}

internal fun Long.formatAsDate(): String {
    return DateFormat.getInstance().format(Date(this))
}