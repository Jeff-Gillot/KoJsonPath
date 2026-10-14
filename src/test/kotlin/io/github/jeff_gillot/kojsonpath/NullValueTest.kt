package io.github.jeff_gillot.kojsonpath

import kotlinx.serialization.json.*
import kotlin.test.*

class NullValueTest {
    private val testJson = Json.parseToJsonElement("""{ "value": null }""")

    @Test
    fun stringOrNullAtReturnsNullForJsonNull() {
        assertNull(testJson.stringOrNullAt("/value"))
    }

    @Test
    fun stringAtThrowsOnJsonNull() {
        val exception = assertFailsWith<JsonPointerError> { testJson.stringAt("/value") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /value
             | 'value' is not a string, it is null
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun intAtThrowsOnJsonNull() {
        val exception = assertFailsWith<JsonPointerError> { testJson.intAt("/value") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /value
             | 'value' is not an integer, it is null
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun longAtThrowsOnJsonNull() {
        val exception = assertFailsWith<JsonPointerError> { testJson.longAt("/value") }
        assertContains(exception.message!!, "'value' is not a long, it is null")
    }

    @Test
    fun doubleAtThrowsOnJsonNull() {
        val exception = assertFailsWith<JsonPointerError> { testJson.doubleAt("/value") }
        assertContains(exception.message!!, "'value' is not a number, it is null")
    }

    @Test
    fun booleanAtThrowsOnJsonNull() {
        val exception = assertFailsWith<JsonPointerError> { testJson.booleanAt("/value") }
        assertContains(exception.message!!, "'value' is not a boolean, it is null")
    }

    @Test
    fun descendingThroughNullReportsTheNullSegment() {
        val exception = assertFailsWith<JsonPointerError> { testJson.stringAt("/value/child") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /value/child
             | 'value' is null, not an object or array
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }
}
