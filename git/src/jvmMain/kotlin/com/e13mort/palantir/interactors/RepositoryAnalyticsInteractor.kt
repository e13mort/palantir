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
    Interactor<Pair<String, List<Range>>, RepositoryReport<T>> {
    override fun run(arg: Pair<String, List<Range>>): Flow<RepositoryReport<T>> {
        return flow {
            val argPath = arg.first
            val ranges = arg.second

            val specification = createSpec(argPath)
            val result = reportCalculator.calculateReport(specification, ranges)
            emit(RepositoryReport(result))
        }
    }

    interface RepositoryReportCalculator<T> {
        suspend fun calculateReport(
            specification: RepositoryAnalysisSpecification,
            ranges: List<Range>
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