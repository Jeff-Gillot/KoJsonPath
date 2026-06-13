package io.github.jeff_gillot.kojsonpath

import kotlinx.serialization.json.*
import kotlin.test.*

class JsonPointerTest {
    private val testJson =
        Json.parseToJsonElement(
            """
            {
                "values": {
                    "addresses": [
                        { "street": "Main St" }
                    ],
                    "tags": ["a", "b", "c"],
                    "name": "Jean-Francois",
                    "age": 42,
                    "population": 8000000000,
                    "height": 1.75,
                    "active": true,
                    "special/key": {
                    }
                }
            }
            """.trimIndent(),
        )

    @Test
    fun testSuccessfulNavigation() {
        val street = testJson.jsonElementAt("/values/addresses/0/street")
        assertEquals("Main St", (street as JsonPrimitive).content)
    }

    @Test
    fun testArrayNotAnObjectError() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.jsonElementAt("/values/addresses/street")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/addresses/street
                              | expected an array index, got 'street'
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testMissingKeyError() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.jsonElementAt("/values/city")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/city
                    | key 'city' not found in parent
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testPrimitiveNotAnObjectOrArrayError() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.jsonElementAt("/values/name/first")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/name/first
                    | 'name' is a primitive, not an object or array
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testIndexOutOfBoundsError() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.jsonElementAt("/values/addresses/5")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/addresses/5
                              | index 5 is out of bounds - array has 1 element
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testIndexOutOfBoundsErrorPluralizesElements() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.jsonElementAt("/values/tags/5")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/tags/5
                         | index 5 is out of bounds - array has 3 elements
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testInvalidPathPrefix() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                testJson.jsonElementAt("values/addresses")
            }
        assertEquals("Non-empty JSON Pointer must start with '/'", exception.message)
    }

    @Test
    fun testStringAt() {
        assertEquals("Jean-Francois", testJson.stringAt("/values/name"))
    }

    @Test
    fun testIntAt() {
        assertEquals(42, testJson.intAt("/values/age"))
    }

    @Test
    fun testLongAt() {
        assertEquals(8_000_000_000L, testJson.longAt("/values/population"))
    }

    @Test
    fun testDoubleAt() {
        assertEquals(1.75, testJson.doubleAt("/values/height"))
    }

    @Test
    fun testBooleanAt() {
        assertEquals(true, testJson.booleanAt("/values/active"))
    }

    @Test
    fun testJsonObjectAt() {
        val jsonObject = testJson.jsonObjectAt("/values/addresses/0")
        assertEquals("Main St", jsonObject.stringAt("/street"))
    }

    @Test
    fun testJsonArrayAt() {
        val jsonArray = testJson.jsonArrayAt("/values/addresses")
        assertEquals(1, jsonArray.size)
    }

    @Test
    fun testBooleanAtInvalidValueError() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.booleanAt("/values/name")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/name
                    | 'name' is not a valid boolean
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testLongAtInvalidValueError() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.longAt("/values/name")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/name
                    | 'name' is not a valid long
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testJsonArrayAtTypeMismatchError() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.jsonArrayAt("/values/name")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/name
                    | 'name' is not an array, it is a primitive
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testJsonObjectAtTypeMismatchError() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.jsonObjectAt("/values/addresses")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/addresses
                    | 'addresses' is not an object, it is an array
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testEscapedKeyIndexAlignment() {
        val exception =
            assertFailsWith<JsonPointerError> {
                testJson.jsonElementAt("/values/special~1key/nested")
            }

        val expectedMessage =
            """
            Failed to resolve JSON pointer:
            /values/special~1key/nested
                                 | key 'nested' not found in parent
            """.trimIndent()

        assertEquals(expectedMessage, exception.message)
    }
}
