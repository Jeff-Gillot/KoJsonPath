package io.github.jeff_gillot.kojsonpath

import kotlinx.serialization.json.*
import kotlin.test.*

class SafeNavigationTest {
    private val testJson =
        Json.parseToJsonElement(
            """
            {
                "values": {
                    "tags": ["a", "b", "c"],
                    "name": "Jean-Francois"
                }
            }
            """.trimIndent(),
        )

    @Test
    fun returnsElementWhenPathResolves() {
        val element = testJson.jsonElementOrNullAt("/values/name")
        assertEquals("Jean-Francois", (element as JsonPrimitive).content)
    }

    @Test
    fun returnsNullWhenKeyMissing() {
        assertNull(testJson.jsonElementOrNullAt("/values/city"))
    }

    @Test
    fun returnsNullWhenIndexOutOfBounds() {
        assertNull(testJson.jsonElementOrNullAt("/values/tags/9"))
    }

    @Test
    fun returnsNullWhenArrayIndexedByNonIndex() {
        assertNull(testJson.jsonElementOrNullAt("/values/tags/foo"))
    }

    @Test
    fun returnsNullWhenDescendingIntoPrimitive() {
        assertNull(testJson.jsonElementOrNullAt("/values/name/first"))
    }

    @Test
    fun acceptsJsonPointerOverload() {
        val element = testJson.jsonElementOrNullAt(JsonPointer("/values/name"))
        assertEquals("Jean-Francois", (element as JsonPrimitive).content)
    }
}
