package io.github.wistefan.mapping;

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

	private static final String COMPACT_VALUE = "compact";
	private static final String EXPANDED_VALUE = "expanded";

	@ParameterizedTest(name = "{0}")
	@MethodSource("compactCases")
	void compactStripsKnownPrefixes(String description, String key, String expected) {
		assertEquals(expected, NgsiLdKeyCompactor.compact(key), description);
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

		Map<String, Integer> result = NgsiLdKeyCompactor.compactKeys(input);

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

		assertEquals(Map.of(expectedKey, COMPACT_VALUE), NgsiLdKeyCompactor.compactKeys(input),
				"The already compact key should win.");
	}

	static Stream<Arguments> collisionCases() {
		return Stream.of(true, false).flatMap(compactFirst -> Stream.of(
				Arguments.of("name", CORE_CONTEXT_PREFIX + "name", "name", compactFirst),
				Arguments.of("validFor", DEFAULT_CONTEXT_PREFIX + "validFor", "validFor", compactFirst),
				Arguments.of("@referredType", DEFAULT_CONTEXT_PREFIX + "tmfEscaped-@referredType", "@referredType", compactFirst)));
	}

	@Test
	void compactKeysReturnsInputWhenAlreadyCompact() {
		Map<String, Integer> input = new LinkedHashMap<>(Map.of("name", 1, "role", 2));
		assertSame(input, NgsiLdKeyCompactor.compactKeys(input), "An already compact map should be returned as it is.");
	}

	@Test
	void compactKeysOfNullIsNull() {
		assertNull(NgsiLdKeyCompactor.compactKeys(null), "null should be returned for null.");
	}

}
