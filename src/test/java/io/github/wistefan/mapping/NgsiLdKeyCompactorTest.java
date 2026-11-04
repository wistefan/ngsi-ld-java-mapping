package io.github.wistefan.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.wistefan.mapping.NgsiLdKeyCompactor.CORE_CONTEXT_PREFIX;
import static io.github.wistefan.mapping.NgsiLdKeyCompactor.DEFAULT_CONTEXT_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

class NgsiLdKeyCompactorTest {

	private static final String CUSTOM_VOCAB = "https://my.org/vocab/";
	private static final String CUSTOM_IRI = "https://my.org/attributes/validFor";

	private static final String COMPACT_VALUE = "compact";
	private static final String EXPANDED_VALUE = "expanded";
	private static final String FIRST_VALUE = "first";
	private static final String SECOND_VALUE = "second";

	private NgsiLdKeyCompactor compactor;

	@BeforeEach
	void setUp() {
		compactor = new NgsiLdKeyCompactor();
	}

	/**
	 * Compactor resolving against a context that defines {@code validFor} and a vocabulary for everything else.
	 */
	private static NgsiLdKeyCompactor withCustomContext() {
		return new NgsiLdKeyCompactor(() -> new JsonLdContext(Map.of(CUSTOM_IRI, "validFor"), List.of(CUSTOM_VOCAB)));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("compactCases")
	void compactStripsKnownPrefixes(String description, String key, String expected) {
		assertEquals(expected, compactor.compact(key), description);
	}

	static Stream<Arguments> compactCases() {
		return Stream.of(
				Arguments.of("core prefix", CORE_CONTEXT_PREFIX + "name", "name"),
				Arguments.of("default-context prefix", DEFAULT_CONTEXT_PREFIX + "validFor", "validFor"),
				Arguments.of("already compact", "name", "name"),
				Arguments.of("escaped expanded key", DEFAULT_CONTEXT_PREFIX + "tmfEscaped-@referredType", "@referredType"),
				Arguments.of("escaped expanded VO-collision key", DEFAULT_CONTEXT_PREFIX + "tmfEscaped-value", "tmfEscaped-value"),
				Arguments.of("already compact escaped key", "tmfEscaped-@referredType", "tmfEscaped-@referredType"),
				Arguments.of("unrelated URI", "https://example.org/name", "https://example.org/name"),
				Arguments.of("null", null, null));
	}

	@Test
	void compactKeysCompactsAndKeepsOrder() {
		Map<String, Integer> input = new LinkedHashMap<>();
		input.put(DEFAULT_CONTEXT_PREFIX + "validFor", 1);
		input.put("role", 2);
		input.put(CORE_CONTEXT_PREFIX + "name", 3);

		Map<String, Integer> result = compactor.compactKeys(input);

		assertEquals(List.of("validFor", "role", "name"), List.copyOf(result.keySet()), "Keys should be compacted in order.");
		assertEquals(List.of(1, 2, 3), List.copyOf(result.values()), "Values should stay with their keys.");
	}

	@ParameterizedTest(name = "{0}, compact first: {3}")
	@MethodSource("collisionCases")
	void compactKeyWinsOnCollision(String compactKey, String expandedKey, String expectedKey, boolean compactFirst) {
		Map<String, String> input = new LinkedHashMap<>();
		if (compactFirst) {
			input.put(compactKey, COMPACT_VALUE);
			input.put(expandedKey, EXPANDED_VALUE);
		} else {
			input.put(expandedKey, EXPANDED_VALUE);
			input.put(compactKey, COMPACT_VALUE);
		}

		assertEquals(Map.of(expectedKey, COMPACT_VALUE), compactor.compactKeys(input),
				"The already compact key should win.");
	}

	static Stream<Arguments> collisionCases() {
		return Stream.of(true, false).flatMap(compactFirst -> Stream.of(
				Arguments.of("name", CORE_CONTEXT_PREFIX + "name", "name", compactFirst),
				Arguments.of("validFor", DEFAULT_CONTEXT_PREFIX + "validFor", "validFor", compactFirst),
				Arguments.of("@referredType", DEFAULT_CONTEXT_PREFIX + "tmfEscaped-@referredType", "@referredType", compactFirst)));
	}

	@ParameterizedTest(name = "{0} first")
	@MethodSource("expandedCollisionCases")
	void firstExpandedKeyWinsOnExpandedCollision(String firstKey, String secondKey) {
		Map<String, String> input = new LinkedHashMap<>();
		input.put(firstKey, FIRST_VALUE);
		input.put(secondKey, SECOND_VALUE);

		assertEquals(Map.of("name", FIRST_VALUE), compactor.compactKeys(input),
				"The first expanded key in iteration order should win.");
	}

	static Stream<Arguments> expandedCollisionCases() {
		return Stream.of(
				Arguments.of(CORE_CONTEXT_PREFIX + "name", DEFAULT_CONTEXT_PREFIX + "name"),
				Arguments.of(DEFAULT_CONTEXT_PREFIX + "name", CORE_CONTEXT_PREFIX + "name"));
	}

	@Test
	void compactKeysReturnsInputWhenAlreadyCompact() {
		Map<String, Integer> input = new LinkedHashMap<>(Map.of("name", 1, "role", 2));
		assertSame(input, compactor.compactKeys(input), "An already compact map should be returned as it is.");
	}

	@Test
	void compactKeysOfNullIsNull() {
		assertNull(compactor.compactKeys(null), "null should be returned for null.");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("contextCases")
	void compactResolvesAgainstTheConfiguredContext(String description, String key, String expected) {
		assertEquals(expected, withCustomContext().compact(key), description);
	}

	static Stream<Arguments> contextCases() {
		return Stream.of(
				Arguments.of("term defined by the context", CUSTOM_IRI, "validFor"),
				Arguments.of("name expanded under the context vocabulary", CUSTOM_VOCAB + "role", "role"),
				Arguments.of("escaped name expanded under the context vocabulary", CUSTOM_VOCAB + "tmfEscaped-@referredType", "@referredType"),
				Arguments.of("escaped VO-collision name stays escaped", CUSTOM_VOCAB + "tmfEscaped-value", "tmfEscaped-value"),
				Arguments.of("built-in prefix still wins", DEFAULT_CONTEXT_PREFIX + "name", "name"),
				Arguments.of("IRI the context does not cover", "https://other.org/name", "https://other.org/name"),
				Arguments.of("already compact", "validFor", "validFor"));
	}

	@Test
	void compactKeysResolvesAgainstTheConfiguredContext() {
		Map<String, Integer> input = new LinkedHashMap<>();
		input.put(CUSTOM_IRI, 1);
		input.put(CUSTOM_VOCAB + "role", 2);
		input.put(DEFAULT_CONTEXT_PREFIX + "name", 3);
		input.put("href", 4);

		Map<String, Integer> result = withCustomContext().compactKeys(input);

		assertEquals(List.of("validFor", "role", "name", "href"), List.copyOf(result.keySet()),
				"All expanded keys should be compacted in order.");
		assertEquals(List.of(1, 2, 3, 4), List.copyOf(result.values()), "Values should stay with their keys.");
	}

	@Test
	void compactKeyWinsOverAContextExpandedOne() {
		Map<String, String> input = new LinkedHashMap<>();
		input.put(CUSTOM_IRI, EXPANDED_VALUE);
		input.put("validFor", COMPACT_VALUE);

		assertEquals(Map.of("validFor", COMPACT_VALUE), withCustomContext().compactKeys(input),
				"The already compact key should win.");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("expandedCases")
	void reportsWhetherAKeyIsExpanded(String description, String key, boolean expected) {
		assertEquals(expected, withCustomContext().isExpanded(key), description);
	}

	static Stream<Arguments> expandedCases() {
		return Stream.of(
				Arguments.of("built-in prefix", CORE_CONTEXT_PREFIX + "name", true),
				Arguments.of("context term", CUSTOM_IRI, true),
				Arguments.of("context vocabulary", CUSTOM_VOCAB + "role", true),
				Arguments.of("unknown IRI", "https://other.org/name", false),
				Arguments.of("compact name", "name", false),
				Arguments.of("null", null, false));
	}

	@Test
	void doesNotConsultTheContextForCompactKeys() {
		NgsiLdKeyCompactor failingContext = new NgsiLdKeyCompactor(() -> {
			throw new AssertionError("The context should not be resolved for a compact key.");
		});

		assertEquals("validFor", failingContext.compact("validFor"), "A compact key needs no context.");
		assertEquals("name", failingContext.compact(DEFAULT_CONTEXT_PREFIX + "name"), "A built-in prefix needs no context.");
	}

}
