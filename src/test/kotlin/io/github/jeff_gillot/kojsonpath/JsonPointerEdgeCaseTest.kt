package io.github.jeff_gillot.kojsonpath

import kotlinx.serialization.json.*
import kotlin.test.*

class JsonPointerEdgeCaseTest {
    @Test
    fun emptyPointerReturnsWholeDocument() {
        val json = Json.parseToJsonElement("""{ "a": 1 }""")
        assertSame(json, json.jsonElementAt(""))
    }

    @Test
    fun slashResolvesEmptyStringKey() {
        // RFC 6901: "/" references the member whose key is the empty string.
        val json = Json.parseToJsonElement("""{ "": 42 }""")
        assertEquals(42, json.intAt("/"))
    }

    @Test
    fun trailingSlashResolvesEmptyStringKey() {
        val json = Json.parseToJsonElement("""{ "a": { "": 5 } }""")
        assertEquals(5, json.intAt("/a/"))
    }

    @Test
    fun emptySegmentOnArrayReportsError() {
        val json = Json.parseToJsonElement("""{ "a": [1, 2] }""")
        val exception = assertFailsWith<JsonPointerError> { json.jsonElementAt("/a/") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /a/
               | expected an array index, got ''
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }
}
