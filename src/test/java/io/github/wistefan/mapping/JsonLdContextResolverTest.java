package io.github.wistefan.mapping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonLdContextResolverTest {

	private static final String CONTEXT_URL = "https://my.org/context.jsonld";
	private static final String NESTED_URL = "https://my.org/nested.jsonld";
	private static final String TMF_VOCAB = "https://schema.tmforum.org/";
	private static final String CUSTOM_VOCAB = "https://my.org/vocab/";
	private static final Duration RETRY_DELAY = Duration.ofSeconds(60);

	@ParameterizedTest(name = "{0}")
	@MethodSource("documentCases")
	void buildsContextFromDocument(String description, String document, String iri, String expectedTerm) {
		JsonLdContext context = resolverFor(Map.of(CONTEXT_URL, document)).resolve();

		assertEquals(Optional.ofNullable(expectedTerm), context.compact(iri), description);
	}

	static Stream<Arguments> documentCases() {
		return Stream.of(
				Arguments.of("plain term definition",
						"{\"@context\":{\"validFor\":\"https://my.org/attributes/validFor\"}}",
						"https://my.org/attributes/validFor", "validFor"),
				Arguments.of("expanded term definition",
						"{\"@context\":{\"validFor\":{\"@id\":\"https://my.org/attributes/validFor\",\"@type\":\"@id\"}}}",
						"https://my.org/attributes/validFor", "validFor"),
				Arguments.of("term defined as compact IRI",
						"{\"@context\":{\"tmf\":\"" + TMF_VOCAB + "\",\"validFor\":\"tmf:validFor\"}}",
						TMF_VOCAB + "validFor", "validFor"),
				Arguments.of("vocabulary",
						"{\"@context\":{\"@vocab\":\"" + CUSTOM_VOCAB + "\"}}",
						CUSTOM_VOCAB + "name", "name"),
				Arguments.of("vocabulary defined as compact IRI",
						"{\"@context\":{\"tmf\":\"" + TMF_VOCAB + "\",\"@vocab\":\"tmf:\"}}",
						TMF_VOCAB + "name", "name"),
				Arguments.of("context without the document wrapper",
						"{\"validFor\":\"https://my.org/attributes/validFor\"}",
						"https://my.org/attributes/validFor", "validFor"),
				Arguments.of("keywords are no term definitions",
						"{\"@context\":{\"@version\":1.1,\"@protected\":true,\"name\":\"" + CUSTOM_VOCAB + "name\"}}",
						CUSTOM_VOCAB + "name", "name"),
				Arguments.of("term with a relative IRI is ignored",
						"{\"@context\":{\"name\":\"relative/name\"}}",
						"relative/name", null),
				Arguments.of("unknown IRI",
						"{\"@context\":{\"validFor\":\"https://my.org/attributes/validFor\"}}",
						"https://other.org/name", null));
	}

	@Test
	void laterDefinitionOfATermWins() {
		String document = "{\"@context\":[{\"name\":\"https://first.org/name\"},{\"name\":\"https://second.org/name\"}]}";

		JsonLdContext context = resolverFor(Map.of(CONTEXT_URL, document)).resolve();

		assertEquals(Optional.of("name"), context.compact("https://second.org/name"), "The last definition should be used.");
		assertEquals(Optional.empty(), context.compact("https://first.org/name"), "The overridden definition should be gone.");
	}

	@Test
	void followsNestedRemoteContexts() {
		Map<String, String> documents = Map.of(
				CONTEXT_URL, "{\"@context\":[\"" + NESTED_URL + "\",{\"role\":\"" + CUSTOM_VOCAB + "role\"}]}",
				NESTED_URL, "{\"@context\":{\"validFor\":\"" + TMF_VOCAB + "validFor\"}}");

		JsonLdContext context = resolverFor(documents).resolve();

		assertEquals(Optional.of("validFor"), context.compact(TMF_VOCAB + "validFor"), "The nested context should be followed.");
		assertEquals(Optional.of("role"), context.compact(CUSTOM_VOCAB + "role"), "The outer context should be kept.");
	}

	@Test
	void survivesContextsReferencingEachOther() {
		Map<String, String> documents = Map.of(
				CONTEXT_URL, "{\"@context\":[\"" + NESTED_URL + "\",{\"role\":\"" + CUSTOM_VOCAB + "role\"}]}",
				NESTED_URL, "{\"@context\":[\"" + CONTEXT_URL + "\",{\"validFor\":\"" + TMF_VOCAB + "validFor\"}]}");

		JsonLdContext context = resolverFor(documents).resolve();

		assertEquals(Optional.of("validFor"), context.compact(TMF_VOCAB + "validFor"), "The cycle should not prevent resolution.");
		assertEquals(Optional.of("role"), context.compact(CUSTOM_VOCAB + "role"), "The cycle should not prevent resolution.");
	}

	@Test
	void survivesCyclicPrefixDefinitions() {
		String document = "{\"@context\":{\"a\":\"b:\",\"b\":\"a:\",\"name\":\"a:name\"}}";

		assertTrue(resolverFor(Map.of(CONTEXT_URL, document)).resolve().isEmpty(),
				"A cyclic prefix definition should not resolve to a term.");
	}

	@Test
	void retrievesTheDocumentOnlyOnce() {
		RecordingLoader loader = new RecordingLoader(Map.of(CONTEXT_URL,
				"{\"@context\":{\"name\":\"" + CUSTOM_VOCAB + "name\"}}"));
		JsonLdContextResolver resolver = new JsonLdContextResolver(properties(), loader, new TestClock());

		JsonLdContext first = resolver.resolve();

		assertSame(first, resolver.resolve(), "The resolved context should be reused.");
		assertEquals(List.of(CONTEXT_URL), loader.requestedUrls, "The document should be retrieved once.");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("unusableDocumentCases")
	void retriesAnUnusableDocumentAfterTheDelay(String description, String document) {
		Map<String, String> documents = new HashMap<>();
		Optional.ofNullable(document).ifPresent(body -> documents.put(CONTEXT_URL, body));
		RecordingLoader loader = new RecordingLoader(documents);
		TestClock clock = new TestClock();
		JsonLdContextResolver resolver = new JsonLdContextResolver(properties(), loader, clock);

		assertSame(JsonLdContext.EMPTY, resolver.resolve(), description);
		assertSame(JsonLdContext.EMPTY, resolver.resolve(), "Within the retry delay the document should not be requested again.");
		assertEquals(1, loader.requestedUrls.size(), "The document should have been requested once.");

		clock.advance(RETRY_DELAY.plusSeconds(1));
		documents.put(CONTEXT_URL, "{\"@context\":{\"name\":\"" + CUSTOM_VOCAB + "name\"}}");

		assertEquals(Optional.of("name"), resolver.resolve().compact(CUSTOM_VOCAB + "name"),
				"After the retry delay the document should be requested again.");
		assertEquals(2, loader.requestedUrls.size(), "The document should have been requested twice.");
	}

	static Stream<Arguments> unusableDocumentCases() {
		return Stream.of(
				Arguments.of("An unreachable context should resolve to the empty context.", null),
				Arguments.of("An invalid context should resolve to the empty context.", "{not json"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("noRetrievalCases")
	void doesNotRetrieveTheDocument(String description, boolean compactAgainstContext, String contextUrl) {
		RecordingLoader loader = new RecordingLoader(Map.of());
		MappingProperties mappingProperties = properties();
		mappingProperties.setCompactAgainstContext(compactAgainstContext);
		mappingProperties.setContextUrl(contextUrl);

		assertSame(JsonLdContext.EMPTY, new JsonLdContextResolver(mappingProperties, loader, new TestClock()).resolve(), description);
		assertTrue(loader.requestedUrls.isEmpty(), "No document should have been requested.");
	}

	static Stream<Arguments> noRetrievalCases() {
		return Stream.of(
				Arguments.of("Compaction against the context can be switched off.", false, CONTEXT_URL),
				Arguments.of("Without a context url there is nothing to resolve.", true, null),
				Arguments.of("A blank context url is no context url.", true, "  "));
	}

	private static JsonLdContextResolver resolverFor(Map<String, String> documents) {
		return new JsonLdContextResolver(properties(), new RecordingLoader(documents), new TestClock());
	}

	private static MappingProperties properties() {
		MappingProperties mappingProperties = new MappingProperties();
		mappingProperties.setContextUrl(CONTEXT_URL);
		mappingProperties.setContextRetrievalRetryDelay(RETRY_DELAY);
		return mappingProperties;
	}

	/**
	 * Loader serving the documents from a map, recording what was requested.
	 */
	private static final class RecordingLoader implements JsonLdContextLoader {

		private final Map<String, String> documents;
		private final List<String> requestedUrls = new java.util.ArrayList<>();

		private RecordingLoader(Map<String, String> documents) {
			this.documents = documents;
		}

		@Override
		public Optional<String> load(String url) {
			requestedUrls.add(url);
			return Optional.ofNullable(documents.get(url));
		}
	}

	/**
	 * Clock that only moves when the test moves it.
	 */
	private static final class TestClock extends Clock {

		private Instant now = Instant.parse("2026-11-04T10:00:00Z");

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}

		private void advance(Duration duration) {
			now = now.plus(duration);
		}
	}

}
