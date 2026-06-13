package io.github.jeff_gillot.kojsonpath

import kotlinx.serialization.json.*

public fun JsonElement.stringAt(jsonPointer: String): String = stringAt(JsonPointer(jsonPointer))

public fun JsonElement.stringAt(jsonPointer: JsonPointer): String =
    when (val jsonElement = jsonElementAt(jsonPointer)) {
        is JsonPrimitive -> jsonElement.content
        else -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not a string, it is ${jsonElement.niceType}" }
    }

public fun JsonElement.intAt(jsonPointer: String): Int = intAt(JsonPointer(jsonPointer))

public fun JsonElement.intAt(jsonPointer: JsonPointer): Int =
    when (val jsonElement = jsonElementAt(jsonPointer)) {
        is JsonPrimitive if (jsonElement.content.toIntOrNull() != null) -> jsonElement.content.toInt()
        is JsonPrimitive -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not a valid integer" }
        else -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not an integer, it is ${jsonElement.niceType}" }
    }

public fun JsonElement.longAt(jsonPointer: String): Long = longAt(JsonPointer(jsonPointer))

public fun JsonElement.longAt(jsonPointer: JsonPointer): Long =
    when (val jsonElement = jsonElementAt(jsonPointer)) {
        is JsonPrimitive if (jsonElement.content.toLongOrNull() != null) -> jsonElement.content.toLong()
        is JsonPrimitive -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not a valid long" }
        else -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not a long, it is ${jsonElement.niceType}" }
    }

public fun JsonElement.doubleAt(jsonPointer: String): Double = doubleAt(JsonPointer(jsonPointer))

public fun JsonElement.doubleAt(jsonPointer: JsonPointer): Double =
    when (val jsonElement = jsonElementAt(jsonPointer)) {
        is JsonPrimitive if (jsonElement.content.toDoubleOrNull() != null) -> jsonElement.content.toDouble()
        is JsonPrimitive -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not a valid number" }
        else -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not a number, it is ${jsonElement.niceType}" }
    }

public fun JsonElement.booleanAt(jsonPointer: String): Boolean = booleanAt(JsonPointer(jsonPointer))

public fun JsonElement.booleanAt(jsonPointer: JsonPointer): Boolean =
    when (val jsonElement = jsonElementAt(jsonPointer)) {
        is JsonPrimitive if (jsonElement.content.toBooleanStrictOrNull() != null) -> jsonElement.content.toBooleanStrict()
        is JsonPrimitive -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not a valid boolean" }
        else -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not a boolean, it is ${jsonElement.niceType}" }
    }

public fun JsonElement.jsonObjectAt(jsonPointer: String): JsonObject = jsonObjectAt(JsonPointer(jsonPointer))

public fun JsonElement.jsonObjectAt(jsonPointer: JsonPointer): JsonObject =
    when (val jsonElement = jsonElementAt(jsonPointer)) {
        is JsonObject -> jsonElement
        else -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not an object, it is ${jsonElement.niceType}" }
    }

public fun JsonElement.jsonArrayAt(jsonPointer: String): JsonArray = jsonArrayAt(JsonPointer(jsonPointer))

public fun JsonElement.jsonArrayAt(jsonPointer: JsonPointer): JsonArray =
    when (val jsonElement = jsonElementAt(jsonPointer)) {
        is JsonArray -> jsonElement
        else -> throw JsonPointerError(jsonPointer, jsonPointer.segments.lastIndex) { "'$it' is not an array, it is ${jsonElement.niceType}" }
    }

public fun JsonElement.jsonElementAt(jsonPointer: String): JsonElement = jsonElementAt(JsonPointer(jsonPointer))

public fun JsonElement.jsonElementAt(jsonPointer: JsonPointer): JsonElement =
    jsonPointer.segments.fold(this) { jsonElement, segment ->
        when (jsonElement) {
            is JsonObject -> jsonElement[segment.decoded] ?: throw JsonPointerError(jsonPointer, segment.pointerIndex) { "key '$it' not found in parent" }
            is JsonArray if (segment.isIndex && segment.asIndex in jsonElement.indices) -> jsonElement[segment.asIndex]
            is JsonArray if (segment.isIndex) -> throw JsonPointerError(jsonPointer, segment.pointerIndex) { "index $it is out of bounds - array has ${jsonElement.size} element${if (jsonElement.size > 1) "s" else "" }" }
            is JsonArray -> throw JsonPointerError(jsonPointer, segment.pointerIndex) { "expected an array index, got '$it'" }
            is JsonPrimitive -> throw JsonPointerError(jsonPointer, segment.pointerIndex - 1) { "'$it' is ${jsonElement.niceType}, not an object or array" }
            JsonNull -> throw JsonPointerError(jsonPointer, segment.pointerIndex) { "'$it' is null, not an object or array" }
        }
    }

public fun JsonElement.stringOrNullAt(jsonPointer: String): String? = stringOrNullAt(JsonPointer(jsonPointer))

public fun JsonElement.stringOrNullAt(jsonPointer: JsonPointer): String? = (jsonElementOrNullAt(jsonPointer) as? JsonPrimitive)?.content

public fun JsonElement.intOrNullAt(jsonPointer: String): Int? = intOrNullAt(JsonPointer(jsonPointer))

public fun JsonElement.intOrNullAt(jsonPointer: JsonPointer): Int? = (jsonElementOrNullAt(jsonPointer) as? JsonPrimitive)?.content?.toIntOrNull()

public fun JsonElement.longOrNullAt(jsonPointer: String): Long? = longOrNullAt(JsonPointer(jsonPointer))

public fun JsonElement.longOrNullAt(jsonPointer: JsonPointer): Long? = (jsonElementOrNullAt(jsonPointer) as? JsonPrimitive)?.content?.toLongOrNull()

public fun JsonElement.doubleOrNullAt(jsonPointer: String): Double? = doubleOrNullAt(JsonPointer(jsonPointer))

public fun JsonElement.doubleOrNullAt(jsonPointer: JsonPointer): Double? = (jsonElementOrNullAt(jsonPointer) as? JsonPrimitive)?.content?.toDoubleOrNull()

public fun JsonElement.booleanOrNullAt(jsonPointer: String): Boolean? = booleanOrNullAt(JsonPointer(jsonPointer))

public fun JsonElement.booleanOrNullAt(jsonPointer: JsonPointer): Boolean? = (jsonElementOrNullAt(jsonPointer) as? JsonPrimitive)?.content?.toBooleanStrictOrNull()

public fun JsonElement.jsonObjectOrNullAt(jsonPointer: String): JsonObject? = jsonObjectOrNullAt(JsonPointer(jsonPointer))

public fun JsonElement.jsonObjectOrNullAt(jsonPointer: JsonPointer): JsonObject? = jsonElementOrNullAt(jsonPointer) as? JsonObject

public fun JsonElement.jsonArrayOrNullAt(jsonPointer: String): JsonArray? = jsonArrayOrNullAt(JsonPointer(jsonPointer))

public fun JsonElement.jsonArrayOrNullAt(jsonPointer: JsonPointer): JsonArray? = jsonElementOrNullAt(jsonPointer) as? JsonArray

public fun JsonElement.jsonElementOrNullAt(jsonPointer: String): JsonElement? = jsonElementOrNullAt(JsonPointer(jsonPointer))

public fun JsonElement.jsonElementOrNullAt(jsonPointer: JsonPointer): JsonElement? =
    jsonPointer.segments.fold(this as JsonElement?) { jsonElement, segment ->
        val decodedPart = segment.decoded
        val pathIndex = segment.pointerIndex

        when (jsonElement) {
            is JsonObject -> jsonElement[decodedPart]
            is JsonArray if (segment.isIndex && segment.asIndex in jsonElement.indices) -> jsonElement[segment.asIndex]
            else -> null
        }
    }

public data class JsonPointer(
    public val path: String,
) {
    init {
        if (path.isNotEmpty() && !path.startsWith("/")) {
            throw IllegalArgumentException("Non-empty JSON Pointer must start with '/'")
        }
    }

    public val segments: List<JsonPointerSegment> =
        if (path.isEmpty()) {
            emptyList()
        } else {
            path.split('/').drop(1).mapIndexed { index, part -> JsonPointerSegment(part, index) }
        }

    override fun toString(): String = path
}

public data class JsonPointerSegment(
    val raw: String,
    val pointerIndex: Int,
) {
    val isIndex: Boolean = raw.isNotEmpty() && (raw == "0" || (raw.first() != '0' && raw.all { it.isDigit() }))
    val asIndex: Int by lazy { raw.toInt() }
    val decoded: String = raw.replace("~1", "/").replace("~0", "~")
}

public class JsonPointerError internal constructor(
    public val jsonPointer: JsonPointer,
    public val index: Int,
    messageProvider: (String) -> String,
) : RuntimeException(
        buildString {
            appendLine("Failed to resolve JSON pointer:")
            appendLine(jsonPointer)
            jsonPointer.segments.take(index + 1).forEach { segment ->
                if (segment.pointerIndex == index) {
                    append(" | ${messageProvider(segment.decoded)}")
                } else {
                    append(" ")
                    repeat(segment.raw.length) {
                        append(" ")
                    }
                }
            }
        },
    )

private val JsonElement.niceType: String get() =
    when (this) {
        is JsonArray -> "an array"
        is JsonObject -> "an object"
        is JsonNull -> "null"
        is JsonPrimitive -> "a primitive"
    }
