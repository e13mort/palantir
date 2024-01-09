package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.ApproveStatisticsInteractor
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.model.User
import com.jakewharton.picnic.table

class ASCIIApproveStatisticsRenderer :
    ReportRender<ApproveStatisticsInteractor.Report, String, Unit> {
    override fun render(value: ApproveStatisticsInteractor.Report, params: Unit): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                val users: List<User> = value.users()
                row {
                    cell("Period")
                    users.forEach { user ->
                        cell(user.name().split(" ")[0])
                    }
                }
                row {
                    cell("Total")
                    users.forEach {
                        cell(value.totalCount(it))
                    }
                }
                value.periods().forEach { period ->
                    row {
                        cell(period)
                        users.forEach { user ->
                            cell(value.approvesBy(user, period))
                        }
                    }
                }
            }
        }.toString()
    }
}