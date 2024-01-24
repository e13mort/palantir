package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCodeChangesReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString
import com.jakewharton.picnic.table

class ASCIICodeChangesReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCodeChangesReport, String, Set<CodeChangesReportParams>> {
    override fun render(value: RepositoryCodeChangesReport, params: Set<CodeChangesReportParams>): String {
        return table {
            cellStyle {
                border = true
            }
            value.result.forEach { groupedResult ->
                row {
                    cell(groupedResult.groupName) {
                        columnSpan = 6
                    }
                }
                groupedResult.result.commitDiffs.forEach { diff ->
                    row {
                        cell(diff.key) {
                            columnSpan = 6
                        }
                    }
                    row {
                        cell("Range")
                        cell("Added")
                        cell("Removed")
                        cell("Code increment")
                        cell("Total")
                        cell("Authors count")
                    }
                    diff.value.forEach {
                        row {
                            cell(it.range.asString(formatter))
                            cell(it.totalAdded())
                            cell(it.totalRemoved())
                            cell(it.codeIncrement())
                            cell(it.totalChanged())
                            cell(it.uniqueAuthors().size)
                        }
                        if (params.contains(CodeChangesReportParams.ShowFullCommitsList)) {
                            row {
                                cell("Details") {
                                    columnSpan = 7
                                }
                            }
                            row {
                                cell("Commit")
                                cell("Added")
                                cell("Removed")
                                cell("Added(Ignored)")
                                cell("Removed(Ignored)")
                                cell("Effective code increment")
                                cell("Effective total changes")
                                cell("Author email")
                            }
                            it.diffs.forEach { commitDiff ->
                                row {
                                    cell(commitDiff.targetCommitSHA1)
                                    cell(commitDiff.linesAdded)
                                    cell(commitDiff.linesRemoved)
                                    cell(commitDiff.ignoredLinesAdd)
                                    cell(commitDiff.ignoredLinesRemove)
                                    cell(commitDiff.codeIncrement())
                                    cell(commitDiff.totalChanges())
                                    cell(commitDiff.authorEmailAddress)
                                }
                            }
                        }
                    }
                }

            }
        }.toString()
    }

}