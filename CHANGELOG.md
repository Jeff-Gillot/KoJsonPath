# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.0] - 2026-10-14

### Changed

- **Breaking:** `JsonPointerSegment.asIndex` is now `Int?` — it is `null` when the
  segment is not an array index or exceeds the `Int` range, instead of throwing.

### Fixed

- `stringOrNullAt` returned the string `"null"` for a JSON `null`; it now returns `null`.
- Typed accessors (`stringAt`, `intAt`, `longAt`, `doubleAt`, `booleanAt`) now
  report `'<key>' is not a <type>, it is null` for a JSON `null`.
- Array indices beyond the `Int` range threw `NumberFormatException`; they are now
  reported as out of bounds, and the `OrNull` accessors return `null`.
- The out-of-bounds error for an empty array now says "0 elements".
- Type mismatches on the root pointer `""` now produce a descriptive error message.

## [0.1.0] - 2026-06-13

### Added

- Initial release: typed and `OrNull` accessors on `JsonElement` for navigating
  `kotlinx.serialization` JSON with RFC 6901 JSON Pointers, with error messages
  that point at the failing segment.

[Unreleased]: https://github.com/Jeff-Gillot/kojsonpath/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/Jeff-Gillot/kojsonpath/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Jeff-Gillot/kojsonpath/releases/tag/v0.1.0
