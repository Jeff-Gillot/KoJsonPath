# KoJsonPath

`KoJsonPath` is a lightweight, zero-dependency Kotlin library for navigating
`JsonElement` structures from
[`kotlinx.serialization`](https://github.com/Kotlin/kotlinx.serialization) using
[RFC 6901 JSON Pointers](https://datatracker.ietf.org/doc/html/rfc6901).

It does not introduce a query engine or a DSL. It adds a handful of extension
functions on the standard `JsonElement` types, so navigation reads like part of
`kotlinx.serialization` itself — and the values you pull out are still plain
`JsonObject`s, `JsonArray`s, and `JsonPrimitive`s that you can keep working with
using the standard library.

---

## Why it feels native

The accessors are extension functions on `JsonElement`, and the types you get
back are the real `kotlinx.serialization` types — nothing is wrapped:

- `JsonObject` **is a** `Map<String, JsonElement>`
- `JsonArray` **is a** `List<JsonElement>`

So a pointer lookup drops you straight back into the standard library. You can
navigate to an array with a pointer, run a normal `.first { }` / `.map { }` /
`.filter { }` over it, and continue navigating from each element with another
pointer — no glue code, no conversions.

```kotlin
val street = json
    .jsonArrayAt("/user/addresses")                       // -> JsonArray (a List)
    .first { it.booleanOrNullAt("/default") == true }     // -> JsonElement
    .stringAt("/street")                                  // -> String
```

---

## Installation

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jeff-gillot:kojsonpath:0.1.0")
}
```

`kotlinx-serialization-json` is exposed transitively, so you do not need to add
it yourself.

---

## The sample document

All examples below navigate this document:

```kotlin
val json = Json.parseToJsonElement(
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
```

---

## 1. Typed access

Each accessor navigates a pointer and returns a typed value. The throwing form
fails with a `JsonPointerError` if the path is missing or the type does not
match.

```kotlin
val name: String       = json.stringAt("/store/name")               // "Central Bookshop"
val price: Double      = json.doubleAt("/store/books/0/price")      // 39.99
val available: Boolean = json.booleanAt("/store/books/1/available") // false
```

Available typed accessors: `stringAt`, `intAt`, `longAt`, `doubleAt`,
`booleanAt`, `jsonObjectAt`, `jsonArrayAt`, and the untyped `jsonElementAt`.

A pointer segment that is all digits is treated as an array index:

```kotlin
val firstTitle = json.stringAt("/store/books/0/title")              // "Kotlin in Action"
```

Per RFC 6901, `~1` decodes to `/` and `~0` decodes to `~`, so keys containing a
slash are addressable:

```kotlin
val featured = json.stringAt("/store/special~1section/featured")    // "Advanced Kotlin"
```

---

## 2. Safe access with `OrNull`

Every typed accessor has an `OrNull` counterpart that returns `null` instead of
throwing when the path is missing or the type does not match. This is ideal for
optional fields and for use inside predicates.

```kotlin
val publisher: String? = json.stringOrNullAt("/store/books/0/publisher") // null (missing)
val thirdBook = json.jsonObjectOrNullAt("/store/books/2")                // null (out of bounds)
val notAString = json.stringOrNullAt("/store/books")                     // null (it is an array)
```

Available `OrNull` accessors: `stringOrNullAt`, `intOrNullAt`, `longOrNullAt`,
`doubleOrNullAt`, `booleanOrNullAt`, `jsonObjectOrNullAt`, `jsonArrayOrNullAt`,
and `jsonElementOrNullAt`.

---

## 3. Mixing pointers with arrays (`List` operations)

`jsonArrayAt` returns a `JsonArray`, which is a `List<JsonElement>`. Every
standard collection operation works, and each element is itself a `JsonElement`
you can navigate further:

```kotlin
// Find the default address and read its street.
val defaultStreet = json
    .jsonArrayAt("/user/addresses")
    .first { it.booleanOrNullAt("/default") == true }
    .stringAt("/street")                                            // "Back St"

// Project every book title.
val titles: List<String> = json
    .jsonArrayAt("/store/books")
    .map { it.stringAt("/title") }                                  // ["Kotlin in Action", "Effective Kotlin"]

// Filter on a nested value.
val affordable = json
    .jsonArrayAt("/store/books")
    .filter { it.doubleAt("/price") < 35.0 }                        // [ { "Effective Kotlin" ... } ]

// Sum with a standard fold.
val total = json
    .jsonArrayAt("/store/books")
    .sumOf { it.doubleAt("/price") }                                // 69.89
```

---

## 4. Mixing pointers with objects (`Map` operations)

`jsonObjectAt` returns a `JsonObject`, which is a `Map<String, JsonElement>`. Its
keys, entries, and values are all available directly:

```kotlin
val store = json.jsonObjectAt("/store")

val keys = store.keys                                               // ["name", "books", "special/section"]

// Iterate entries like any map.
store.forEach { (key, element) ->
    println("$key -> $element")
}

// Map-native filtering, then keep navigating the result.
val collections = store
    .filterValues { it is JsonArray }                              // { "books": [...] }
    .mapValues { (_, value) -> value.jsonArray.size }              // { "books": 2 }
```

Because the result is a `Map`, you can also reach a key directly when you do not
need pointer semantics — and switch back to a pointer for the deep part:

```kotlin
val featured = store
    .jsonObjectAt("/special~1section")
    .stringAt("/featured")                                         // "Advanced Kotlin"
```

---

## 5. Reusing a parsed pointer

Every accessor has an overload taking a pre-parsed `JsonPointer`, so a pointer
used in a hot loop is parsed once:

```kotlin
val streetPointer = JsonPointer("/street")

val streets = json
    .jsonArrayAt("/user/addresses")
    .map { it.stringAt(streetPointer) }                            // ["Main St", "Back St"]
```

The empty pointer `""` refers to the whole document:

```kotlin
val root = json.jsonElementAt("")                                  // the document itself
```

---

## Error messages

When a throwing accessor cannot resolve a pointer, it throws a `JsonPointerError`
whose message points at the exact segment that failed.

Index out of bounds — `json.stringAt("/store/books/5/title")`:

```text
Failed to resolve JSON pointer:
/store/books/5/title
             | index 5 is out of bounds - array has 2 elements
```

Missing key — `json.stringAt("/store/books/0/publisher")`:

```text
Failed to resolve JSON pointer:
/store/books/0/publisher
               | key 'publisher' not found in parent
```

Descending into a primitive — `json.stringAt("/store/name/length")`:

```text
Failed to resolve JSON pointer:
/store/name/length
       | 'name' is a primitive, not an object or array
```

The caret is aligned using the raw (still-escaped) segment text, so it stays
correct even for keys that contain `~0` / `~1` escapes.

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
