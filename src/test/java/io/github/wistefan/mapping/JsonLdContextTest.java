package io.github.wistefan.mapping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonLdContextTest {

	private static final String TMF_VOCAB = "https://schema.tmforum.org/";
	private static final String NESTED_VOCAB = "https://schema.tmforum.org/product/";
	private static final String CUSTOM_IRI = "https://my.org/attributes/validFor";

	@ParameterizedTest(name = "{0}")
	@MethodSource("absoluteIriCases")
	void detectsAbsoluteIris(String description, String key, boolean expected) {
		assertEquals(expected, JsonLdContext.isAbsoluteIri(key), description);
	}

	static Stream<Arguments> absoluteIriCases() {
		return Stream.of(
				Arguments.of("https IRI", "https://my.org/attributes/name", true),
				Arguments.of("http IRI", "http://my.org/attributes/name", true),
				Arguments.of("urn", "urn:ngsi-ld:entity:1", true),
				Arguments.of("term", "name", false),
				Arguments.of("compact IRI", "tmf:name", false),
				Arguments.of("escaped term", "tmfEscaped-value", false),
				Arguments.of("null", null, false));
	}

	@Test
	void compactsByTermDefinition() {
		JsonLdContext context = new JsonLdContext(Map.of(CUSTOM_IRI, "validFor"), List.of());

		assertEquals(Optional.of("validFor"), context.compact(CUSTOM_IRI), "The defined term should be returned.");
		assertEquals(Optional.empty(), context.compact("https://my.org/attributes/unknown"),
				"An IRI the context does not define should not be compacted.");
	}

	@Test
	void compactsByVocabulary() {
		JsonLdContext context = new JsonLdContext(Map.of(), List.of(TMF_VOCAB));

		assertEquals(Optional.of("name"), context.compact(TMF_VOCAB + "name"), "The remainder of the vocabulary should be returned.");
		assertEquals(Optional.empty(), context.compact(TMF_VOCAB), "The bare vocabulary is not an attribute name.");
		assertEquals(Optional.empty(), context.compact("https://other.org/name"), "Another vocabulary should not be compacted.");
	}

	@Test
	void termDefinitionWinsOverVocabulary() {
		JsonLdContext context = new JsonLdContext(Map.of(TMF_VOCAB + "validFor", "period"), List.of(TMF_VOCAB));

		assertEquals(Optional.of("period"), context.compact(TMF_VOCAB + "validFor"),
				"The explicit term definition should win over the vocabulary.");
	}

	@Test
	void longestVocabularyWins() {
		JsonLdContext context = new JsonLdContext(Map.of(), List.of(TMF_VOCAB, NESTED_VOCAB));

		assertEquals(Optional.of("name"), context.compact(NESTED_VOCAB + "name"),
				"The most specific vocabulary should be stripped.");
		assertEquals(List.of(NESTED_VOCAB, TMF_VOCAB), context.getVocabularies(), "Vocabularies should be ordered longest first.");
	}

	@Test
	void emptyContextCompactsNothing() {
		assertTrue(JsonLdContext.EMPTY.isEmpty(), "The empty context should report itself as empty.");
		assertEquals(Optional.empty(), JsonLdContext.EMPTY.compact(CUSTOM_IRI), "The empty context should not compact.");
		assertEquals(Optional.empty(), JsonLdContext.EMPTY.compact(null), "null should not be compacted.");
		assertFalse(new JsonLdContext(Map.of(CUSTOM_IRI, "validFor"), List.of()).isEmpty(),
				"A context with a term should not report itself as empty.");
	}

}
