package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.ApproveStatisticsInteractor
import com.e13mort.gitlab_report.interactors.ReportRender
import com.e13mort.gitlab_report.model.User
import com.jakewharton.picnic.table

class ASCIIApproveStatisticsRenderer : ReportRender<ApproveStatisticsInteractor.Report, String> {
    override fun render(value: ApproveStatisticsInteractor.Report): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                val users: List<User> = value.users()
                row {
                    cell("Period")
                    for (user in users) {
                        cell(user.name().split(" ")[0])
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