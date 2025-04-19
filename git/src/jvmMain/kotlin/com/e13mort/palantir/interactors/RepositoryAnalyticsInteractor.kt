/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader

class RepositoryAnalyticsInteractor<T>(
    private val reportCalculator: RepositoryReportCalculator<T>
) :
    Interactor<RepositoryAnalyticsInteractor.Arguments, RepositoryReport<T>> {

    data class Arguments(
        val ranges: Pair<String, List<Range>>,
        val singleGroupName: String?,
        val itemIndexInGroup: Int?
    )

    override fun run(arg: Arguments): Flow<RepositoryReport<T>> {
        return flow {
            val argPath: String = arg.ranges.first
            val ranges: List<Range> = arg.ranges.second

            val specification = createSpec(argPath)
            val result = reportCalculator.calculateReport(specification, ranges, arg.singleGroupName, arg.itemIndexInGroup)
            emit(RepositoryReport(result))
        }
    }

    interface RepositoryReportCalculator<T> {
        suspend fun calculateReport(
            specification: RepositoryAnalysisSpecification,
            ranges: List<Range>,
            singleGroupName: String? = null,
            itemIndexInGroup: Int? = null
        ): List<RepositoryReport.GroupedResults<T>>

    }

    private suspend fun createSpec(filePath: String): RepositoryAnalysisSpecification {
        val file = File(filePath)
        return if (file.isDirectory) {
            RepositoryAnalysisSpecification(
                mapOf(
                    "single" to listOf(
                        RepositoryAnalysisSpecification.ProjectSpecification(
                            filePath
                        )
                    )
                )
            )
        } else {
            readSpec(file)
        }
    }

    private suspend fun readSpec(file: File): RepositoryAnalysisSpecification {
        val fileContent = withContext(Dispatchers.IO) {
            FileReader(file).readText()
        }
        return RepositoryAnalysisSpecification.fromString(fileContent)
            ?: throw IllegalArgumentException("Failed to create spec from file ${file.absolutePath}")
    }
}