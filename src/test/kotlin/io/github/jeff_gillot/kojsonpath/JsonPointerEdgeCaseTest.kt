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

    @Test
    fun indexBeyondIntRangeIsOutOfBounds() {
        val json = Json.parseToJsonElement("""{ "a": [1] }""")
        val exception = assertFailsWith<JsonPointerError> { json.jsonElementAt("/a/99999999999") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /a/99999999999
               | index 99999999999 is out of bounds - array has 1 element
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun indexBeyondIntRangeReturnsNullInOrNullAccessor() {
        val json = Json.parseToJsonElement("""{ "a": [1] }""")
        assertNull(json.jsonElementOrNullAt("/a/99999999999"))
    }

    @Test
    fun outOfBoundsOnEmptyArrayUsesPluralElements() {
        val json = Json.parseToJsonElement("""{ "a": [] }""")
        val exception = assertFailsWith<JsonPointerError> { json.jsonElementAt("/a/0") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /a/0
               | index 0 is out of bounds - array has 0 elements
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun asIndexIsNullWhenIndexExceedsIntRange() {
        val segment = JsonPointer("/99999999999").segments.single()
        assertTrue(segment.isIndex)
        assertNull(segment.asIndex)
    }

    @Test
    fun asIndexIsNullForNonIndexSegment() {
        assertNull(JsonPointer("/name").segments.single().asIndex)
    }

    @Test
    fun asIndexIsSetForValidIndex() {
        assertEquals(3, JsonPointer("/3").segments.single().asIndex)
    }

    @Test
    fun leadingZeroIsNotAnArrayIndex() {
        // RFC 6901: array indices must not have leading zeros.
        val json = Json.parseToJsonElement("""{ "a": [1, 2] }""")
        val exception = assertFailsWith<JsonPointerError> { json.jsonElementAt("/a/01") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /a/01
               | expected an array index, got '01'
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun negativeNumberIsNotAnArrayIndex() {
        // Negative indices are not valid array indices.
        val json = Json.parseToJsonElement("""{ "a": [1, 2] }""")
        val exception = assertFailsWith<JsonPointerError> { json.jsonElementAt("/a/-1") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /a/-1
               | expected an array index, got '-1'
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun dashIsNotAReadableArrayIndex() {
        // RFC 6901: '-' refers to the nonexistent element after the last one.
        val json = Json.parseToJsonElement("""{ "a": [1, 2] }""")
        val exception = assertFailsWith<JsonPointerError> { json.jsonElementAt("/a/-") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /a/-
               | expected an array index, got '-'
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }
}
