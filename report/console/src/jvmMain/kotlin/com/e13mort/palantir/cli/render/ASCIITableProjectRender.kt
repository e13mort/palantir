/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.ProjectSummary
import com.e13mort.palantir.render.ReportRender
import com.jakewharton.picnic.table

class ASCIITableProjectRender : ReportRender<ProjectSummary, String, Unit> {
    override fun render(value: ProjectSummary, params: Unit): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Id", "Project Name", "Branches", "MRs")
                row(
                    value.projectId(),
                    value.projectName(),
                    value.branchCount(),
                    value.mergeRequestCount()
                )
            }
        }.toString()
    }

}