package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCodeChangesReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString

class CSVCodeChangesReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCodeChangesReport, String, Unit> {
    override fun render(value: RepositoryCodeChangesReport, params: Unit): String {
        return StringBuilder().apply {
            value.result.forEach { groupedResult ->
                append(groupedResult.groupName)
                append(",")
                append("\n")
                groupedResult.result.commitDiffs.forEach { diff ->
                    append(diff.key)
                    append(",")
                    append("\n")
                    append("Range")
                    append(",")
                    append("Added")
                    append(",")
                    append("Removed")
                    append(",")
                    append("Code increment")
                    append(",")
                    append("Total")
                    append(",")
                    append("\n")
                    diff.value.forEach {
                        append(it.range.asString(formatter))
                        append(",")
                        append(it.totalAdded())
                        append(",")
                        append(it.totalRemoved())
                        append(",")
                        append(it.codeIncrement())
                        append(",")
                        append(it.totalChanged())
                        append(",")
                        append("\n")
                    }
                }

            }
        }.toString()
    }

}