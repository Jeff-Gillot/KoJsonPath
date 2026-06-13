package io.github.jeff_gillot.kojsonpath

import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * Verifies that the examples shown in README.md compile and produce the documented results.
 * If an example here changes, the README must change with it.
 */
class ReadmeExamplesTest {
    private val json =
        Json.parseToJsonElement(
            """
            {
                "store": {
                    "name": "Central Bookshop",
                    "books": [
                        { "title": "Kotlin in Action",  "price": 39.99, "available": true },
                        { "title": "Effective Kotlin",  "price": 29.90, "available": false }
                    ],
                    "special/section": {
                        "featured": "Advanced Kotlin"
                    }
                },
                "user": {
                    "addresses": [
                        { "street": "Main St", "default": false },
                        { "street": "Back St", "default": true }
                    ]
                }
            }
            """.trimIndent(),
        )

    @Test
    fun typedAccess() {
        assertEquals("Central Bookshop", json.stringAt("/store/name"))
        assertEquals(39.99, json.doubleAt("/store/books/0/price"))
        assertEquals(false, json.booleanAt("/store/books/1/available"))
        assertEquals("Kotlin in Action", json.stringAt("/store/books/0/title"))
        assertEquals("Advanced Kotlin", json.stringAt("/store/special~1section/featured"))
    }

    @Test
    fun safeAccess() {
        assertNull(json.stringOrNullAt("/store/books/0/publisher"))
        assertNull(json.jsonObjectOrNullAt("/store/books/2"))
        assertNull(json.stringOrNullAt("/store/books"))
    }

    @Test
    fun defaultAddressStreet() {
        val defaultStreet =
            json
                .jsonArrayAt("/user/addresses")
                .first { it.booleanOrNullAt("/default") == true }
                .stringAt("/street")

        assertEquals("Back St", defaultStreet)
    }

    @Test
    fun listOperations() {
        val titles = json.jsonArrayAt("/store/books").map { it.stringAt("/title") }
        assertEquals(listOf("Kotlin in Action", "Effective Kotlin"), titles)

        val affordable = json.jsonArrayAt("/store/books").filter { it.doubleAt("/price") < 35.0 }
        assertEquals(1, affordable.size)
        assertEquals("Effective Kotlin", affordable.single().stringAt("/title"))
    }

    @Test
    fun mapOperations() {
        val store = json.jsonObjectAt("/store")

        assertEquals(listOf("name", "books", "special/section"), store.keys.toList())

        val collections =
            store
                .filterValues { it is JsonArray }
                .mapValues { (_, value) -> value.jsonArray.size }
        assertEquals(mapOf("books" to 2), collections)

        assertEquals("Advanced Kotlin", store.jsonObjectAt("/special~1section").stringAt("/featured"))
    }

    @Test
    fun reuseParsedPointer() {
        val streetPointer = JsonPointer("/street")
        val streets = json.jsonArrayAt("/user/addresses").map { it.stringAt(streetPointer) }
        assertEquals(listOf("Main St", "Back St"), streets)
    }

    @Test
    fun emptyPointerIsRoot() {
        assertSame(json, json.jsonElementAt(""))
    }
}
