package io.github.jeff_gillot.kojsonpath

import kotlinx.serialization.json.*
import kotlin.test.*

class TypedOrNullAccessorTest {
    private val testJson =
        Json.parseToJsonElement(
            """
            {
                "user": {
                    "name": "Jean-Francois",
                    "age": 42,
                    "balance": 8000000000,
                    "height": 1.75,
                    "active": true,
                    "addresses": [
                        { "street": "Main St", "default": false },
                        { "street": "Back St", "default": true }
                    ]
                }
            }
            """.trimIndent(),
        )

    @Test
    fun stringOrNullAt() {
        assertEquals("Jean-Francois", testJson.stringOrNullAt("/user/name"))
        assertNull(testJson.stringOrNullAt("/user/missing"))
        assertNull(testJson.stringOrNullAt("/user/addresses"))
    }

    @Test
    fun intOrNullAt() {
        assertEquals(42, testJson.intOrNullAt("/user/age"))
        assertNull(testJson.intOrNullAt("/user/missing"))
        assertNull(testJson.intOrNullAt("/user/name"))
    }

    @Test
    fun longOrNullAt() {
        assertEquals(8_000_000_000L, testJson.longOrNullAt("/user/balance"))
        assertNull(testJson.longOrNullAt("/user/name"))
    }

    @Test
    fun doubleOrNullAt() {
        assertEquals(1.75, testJson.doubleOrNullAt("/user/height"))
        assertNull(testJson.doubleOrNullAt("/user/name"))
    }

    @Test
    fun booleanOrNullAt() {
        assertEquals(true, testJson.booleanOrNullAt("/user/active"))
        assertNull(testJson.booleanOrNullAt("/user/name"))
        assertNull(testJson.booleanOrNullAt("/user/missing"))
    }

    @Test
    fun jsonObjectOrNullAt() {
        assertEquals("Main St", testJson.jsonObjectOrNullAt("/user/addresses/0")?.stringAt("/street"))
        assertNull(testJson.jsonObjectOrNullAt("/user/addresses"))
        assertNull(testJson.jsonObjectOrNullAt("/user/missing"))
    }

    @Test
    fun jsonArrayOrNullAt() {
        assertEquals(2, testJson.jsonArrayOrNullAt("/user/addresses")?.size)
        assertNull(testJson.jsonArrayOrNullAt("/user/name"))
    }

    @Test
    fun mixingPointersWithNativeCollectionOperations() {
        val street =
            testJson
                .jsonArrayAt("/user/addresses")
                .first { it.booleanOrNullAt("/default") == true }
                .stringAt("/street")

        assertEquals("Back St", street)
    }
}
