# Implementation Plan: Compact expanded sub-attribute names of relationship instances on read

## Overview
Orion-LD 1.9.0 returns the sub-attributes of each instance in a RELATIONSHIP_LIST (2+ instances) under expanded names such as `https://uri.etsi.org/ngsi-ld/name` and `https://uri.etsi.org/ngsi-ld/default-context/validFor`. `EntityVOMapper` looks sub-attributes up by their short name, so a `fromProperties = true` ref comes back with only its id. The fix adds a small key-compaction helper that strips the NGSI-LD core and default-context prefixes. `EntityVOMapper` applies it before it matches sub-attribute keys to setters and before it merges override properties. Already-compact input does not change.

Out of scope (possible follow-up): full JSON-LD compaction against the `@context` in use. Prefix stripping covers the reported Orion-LD case.

## Steps

### Step 1: Add the `NgsiLdKeyCompactor` helper with unit tests
Create `src/main/java/io/github/wistefan/mapping/NgsiLdKeyCompactor.java`, a final utility class with a private constructor and tab indentation, like `ReservedWordHandler`:

- Named constants: `DEFAULT_CONTEXT_PREFIX = "https://uri.etsi.org/ngsi-ld/default-context/"` and `CORE_CONTEXT_PREFIX = "https://uri.etsi.org/ngsi-ld/"`. Keep them in an ordered list, longest prefix first, so the default-context prefix is checked before the core prefix it starts with.
- `public static String compact(String key)`: strips the first matching prefix. Returns the key unchanged when no prefix matches or when the key is `null`. It must not touch escaped keys (`tmfEscaped-...`), so an expanded escaped key such as `https://uri.etsi.org/ngsi-ld/default-context/tmfEscaped-@referredType` becomes `tmfEscaped-@referredType`.
- `public static <V> Map<String, V> compactKeys(Map<String, V> map)`: returns a new `LinkedHashMap` with compacted keys and keeps the insertion order. **On a collision, the key that was already compact wins** (an expanded key never overwrites a compact one). `null` gives `null`. When no key needs compaction, return the input map unchanged, so the compacted path costs nothing.
- One-line Javadoc per public member. The class gets a short comment that names the Orion-LD quirk.

Tests: `src/test/java/io/github/wistefan/mapping/NgsiLdKeyCompactorTest.java`, structured like `ReservedWordHandlerTest`:
- `@ParameterizedTest` + `@MethodSource` for `compact`: core prefix, default-context prefix, an already-compact key, an escaped expanded key, an unrelated URI (`https://example.org/name` stays as it is), and `null`.
- `compactKeys`: keys get compacted, the compact key wins on a collision in both insertion orders, and the order is kept.

Acceptance: `mvn clean verify` passes. Nothing else in `src/main` uses the helper yet.

### Step 2: Compact sub-attribute keys in `EntityVOMapper` and add the Orion-LD regression test
Wire the helper into `src/main/java/io/github/wistefan/mapping/EntityVOMapper.java`:

1. **`relationshipFromProperties`** (~L543): at the start, compute `Map<String, AdditionalPropertyVO> subAttributes = NgsiLdKeyCompactor.compactKeys(relationshipVO.getAdditionalProperties())` once. Use it in every lookup: the direct `field` lookup, the `ReservedWordHandler.escapeReservedWords(field)` lookup, the `legacyNames` loop, and both `getRelationshipMap(...)` calls in the `RELATIONSHIP`/`RELATIONSHIP_LIST` branches. Do not mutate `relationshipVO`.
2. **`getObjectFromRelationship`** (~L846): merge `NgsiLdKeyCompactor.compactKeys(additionalPropertyVOMap)` instead of the raw map. Expanded override keys then replace the fetched entity's compact attributes instead of landing next to them as unmapped properties.
3. **Nested structured values.** The sub-attribute values themselves can carry expanded keys, for example `validFor` or `associationSpec` with expanded keys inside their value or as their own sub-attributes:
   - In `fromProperty` and `fromRelationship` (~L289-L395), compact the key next to the existing `ReservedWordHandler.removeEscape(entry.getKey())` calls. Compact first, then remove the escape: `removeEscape(compact(key))`. Also compact the `additionalProperties` key set before the merge, so a compact sibling still wins.
   - Extend the private value-tree walker `unescapeReservedKeys` (~L819) to call `removeEscape(NgsiLdKeyCompactor.compact(key))` for every map key. Then `handleProperty`, `handlePropertyList` and the unmapped-property paths compact nested `value` maps as well. If a compacted key collides inside one value map, the compact key wins here too. Use `putIfAbsent` for expanded originals, or compact up front with `compactKeys`. Update that method's Javadoc in one line, and don't make it longer.
4. Leave the top-level entity attribute handling as it is. Top-level keys are compacted by both brokers.

Tests in `src/test/java/io/github/wistefan/mapping/desc/`:
- New test POJOs in `desc/pojos/`:
  - `MyExpandedRef`: entity type `sub-entity`, `String id` constructor, `@EntityId/@RelationshipObject/@DatasetId` id. It has `PROPERTY` setters for `name`, `href` (URI) and `@referredType` (stored escaped, like in `AtPrefixedUnmappedKeysTest`). `validFor` is a `PROPERTY` with `targetClass` = a new `MyTimePeriod` (`startDateTime`, `endDateTime` strings). `associationSpec` is a `PROPERTY` with `targetClass` = `MySubPropertyRefOrValue`, which already exists.
  - `MyPojoWithExpandedRefList`: a `RELATIONSHIP_LIST` attribute with `fromProperties = true` and `targetClass = MyExpandedRef.class`, modelled on `MyPojoWithSubEntityListFrom`.
  - Match the Lombok style of the neighbouring POJOs and their sparse docs.
- In `EntityVOMapperTest.java`, next to the existing `MyPojoWithSubEntityListFrom` cases (~L840-L885), add one `@ParameterizedTest` + `@MethodSource` test that feeds `fromEntityVO` a `RelationshipListVO` with **2 instances**:
  - case `compacted`: sub-attribute keys `name`, `href`, `tmfEscaped-@referredType`, `validFor`, `associationSpec`. This is the regression guard that "compacted input behaves exactly as before".
  - case `expanded`: the same values under `https://uri.etsi.org/ngsi-ld/name`, `https://uri.etsi.org/ngsi-ld/default-context/href`, `.../default-context/tmfEscaped-@referredType`, `.../default-context/validFor` (whose value map uses expanded keys too) and `.../default-context/associationSpec`.
  - case `mixed`: one instance compacted, one expanded. If cheap, add a key that appears in both forms and assert that the compact value wins.
  - Assert that both refs are fully populated (name, href, @referredType, nested validFor, associationSpec), with the `EntitiesRepository` mock never called.
- Add one case for the override merge in `getObjectFromRelationship` (`fromProperties = false`). The mocked repository returns the target entity, and an expanded override sub-attribute on the relationship replaces the fetched value.
- All existing tests in `EntityVOMapperTest`, `AtPrefixedUnmappedKeysTest` and `EntityVOSerializerTest` must stay green without changes.

Update `AGENTS.md`: add `NgsiLdKeyCompactor.java` to the Project Structure list (one line).

Acceptance: `mvn clean verify` passes. The expanded and mixed cases fail on `main` and pass with the change. The compacted case passes on both.
