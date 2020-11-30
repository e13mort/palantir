package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.PrintProjectBranchesInteractor
import com.e13mort.gitlab_report.interactors.ReportRender
import com.jakewharton.picnic.table
import kotlinx.coroutines.runBlocking

class ASCIIBranchesRender : ReportRender<PrintProjectBranchesInteractor.BranchesReport, String> {
    override fun render(value: PrintProjectBranchesInteractor.BranchesReport): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Name")
            }
            runBlocking {
                value.walk {
                    row(it)
                }
            }
        }.toString()
    }
}