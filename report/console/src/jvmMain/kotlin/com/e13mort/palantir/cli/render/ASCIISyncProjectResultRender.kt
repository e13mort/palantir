package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.ReportRender
import com.e13mort.palantir.interactors.ScanProjectInteractor
import com.jakewharton.picnic.table

class ASCIISyncProjectResultRender : ReportRender<ScanProjectInteractor.ScanProjectResult, String> {
    override fun render(value: ScanProjectInteractor.ScanProjectResult): String {
        return table {
            cellStyle {
                border = true
            }
            row {
                cell("Project synced") {
                    columnSpan = 2
                }
            }
            row ("Name", value.projectName())
            row ("Branches", value.syncedBranchesCount())
            row ("Merge Requests", value.syncedMergeRequests())
        }.toString()
    }
}