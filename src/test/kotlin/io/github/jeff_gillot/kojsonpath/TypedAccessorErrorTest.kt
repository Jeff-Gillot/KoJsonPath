package io.github.jeff_gillot.kojsonpath

import kotlinx.serialization.json.*
import kotlin.test.*

class TypedAccessorErrorTest {
    private val testJson =
        Json.parseToJsonElement(
            """
            {
                "values": {
                    "addresses": [
                        { "street": "Main St" }
                    ],
                    "name": "Jean-Francois",
                    "age": 42
                }
            }
            """.trimIndent(),
        )

    @Test
    fun stringAtReturnsStringifiedPrimitive() {
        // stringAt accepts any primitive and renders it as text.
        assertEquals("42", testJson.stringAt("/values/age"))
    }

    @Test
    fun stringAtThrowsOnTypeMismatch() {
        val exception = assertFailsWith<JsonPointerError> { testJson.stringAt("/values/addresses") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/addresses
                    | 'addresses' is not a string, it is an array
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun intAtThrowsOnInvalidValue() {
        val exception = assertFailsWith<JsonPointerError> { testJson.intAt("/values/name") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/name
                    | 'name' is not a valid integer
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun intAtThrowsOnTypeMismatch() {
        val exception = assertFailsWith<JsonPointerError> { testJson.intAt("/values/addresses") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/addresses
                    | 'addresses' is not an integer, it is an array
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun doubleAtThrowsOnInvalidValue() {
        val exception = assertFailsWith<JsonPointerError> { testJson.doubleAt("/values/name") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/name
                    | 'name' is not a valid number
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun doubleAtThrowsOnTypeMismatch() {
        val exception = assertFailsWith<JsonPointerError> { testJson.doubleAt("/values/addresses") }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/addresses
                    | 'addresses' is not a number, it is an array
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun acceptsJsonPointerOverload() {
        assertEquals(42, testJson.intAt(JsonPointer("/values/age")))
    }
}
