package com.e13mort.palantir.cloc

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
data class LanguageReport(
    val nFiles: Int,
    val blank: Int,
    val comment: Int,
    val code: Int
)

object ClocSerializer : JsonTransformingSerializer<Map<String, LanguageReport>>(
    MapSerializer(String.serializer(), LanguageReport.serializer())
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {
            return JsonObject(
                element.toMap()
                    .filterKeys {
                        it != "header"
                    }
            )
        }
        throw IllegalStateException("Wrong object")
    }
}