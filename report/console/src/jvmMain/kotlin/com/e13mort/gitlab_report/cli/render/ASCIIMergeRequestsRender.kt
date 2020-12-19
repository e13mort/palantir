package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.PrintProjectMergeRequestsInteractor
import com.e13mort.gitlab_report.interactors.ReportRender
import com.jakewharton.picnic.table
import kotlinx.coroutines.runBlocking
import java.text.DateFormat
import java.util.*

class ASCIIMergeRequestsRender : ReportRender<PrintProjectMergeRequestsInteractor.MergeRequestsReport, String> {

    internal object RenderingSettings {
        const val branchMaxLength = 50
        val headers = arrayOf("ID", "From", "To", "Created", "State")
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
                value.walk { id: String, sourceBranch: String, targetBranch: String, created: Long, state: String ->
                    val formattedDate = DateFormat.getInstance().format(Date(created))
                    row(
                        id,
                        stripBranchName(sourceBranch),
                        stripBranchName(targetBranch),
                        formattedDate,
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