/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.PrintProjectBranchesInteractor
import com.e13mort.palantir.render.ReportRender
import com.jakewharton.picnic.table
import kotlinx.coroutines.runBlocking

class ASCIIBranchesRender :
    ReportRender<PrintProjectBranchesInteractor.BranchesReport, String, Unit> {
    override fun render(
        value: PrintProjectBranchesInteractor.BranchesReport,
        params: Unit
    ): String {
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