package com.e13mort.palantir.cloc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.InputStream

class ClocAdapter(
    private val clocPath: String
) {

    fun calculate(directory: String, excludedPaths: List<String>): Map<String, LanguageReport> {
        val runtime = Runtime.getRuntime()
        val process: Process = runtime.exec(buildClocCommand(excludedPaths), null, File(directory))
        return decodeResult(process.inputStream)
    }

    internal fun buildClocCommand(excludedPaths: List<String>): String {
        return buildString {
            append(clocPath)
            append(" . --json")
            if (excludedPaths.isNotEmpty()) {
                append(" --fullpath")
                val excludeArg = excludedPaths.joinTo(StringBuilder(), "|", "(", ")").toString()
                append(" --not-match-d=$excludeArg")
                append(" --not-match-f=$excludeArg")
            }
        }
    }

    internal fun decodeResult(inputStream: InputStream): Map<String, LanguageReport> {
        return Json {
            ignoreUnknownKeys = true
        }.decodeFromStream(ClocSerializer, inputStream)
    }

    companion object {
        fun create(): ClocAdapter {
            return ClocAdapter("cloc")
        }
    }
}

