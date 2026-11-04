package io.github.wistefan.mapping;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builds the {@link JsonLdContext} for the context configured at {@code mapping.context-url}.
 * The document is retrieved at most once, on the first key that actually needs it, and the result is kept for the
 * lifetime of the bean. A retrieval that failed is retried at the earliest after
 * {@code mapping.context-retrieval-retry-delay}, so an unreachable context does not cause a request per
 * mapped entity.
 */
@Slf4j
@Singleton
public class JsonLdContextResolver {

	private static final String CONTEXT_KEY = "@context";
	private static final String ID_KEY = "@id";
	private static final String VOCAB_KEY = "@vocab";
	private static final String KEYWORD_PREFIX = "@";
	private static final char COMPACT_IRI_SEPARATOR = ':';

	/**
	 * Maximum number of nested remote contexts to follow, a guard against contexts referencing each other.
	 */
	private static final int MAX_CONTEXT_DEPTH = 10;

	/**
	 * Plain mapper for the context document. The NGSI-LD mapper is not usable here, its factory unescapes reserved
	 * words in field names and would rewrite term names.
	 */
	private final ObjectMapper documentMapper = JsonMapper.builder().build();

	private final MappingProperties mappingProperties;
	private final JsonLdContextLoader contextLoader;
	private final Clock clock;

	private volatile JsonLdContext resolvedContext;
	private Instant earliestRetry = Instant.MIN;

	@Inject
	public JsonLdContextResolver(MappingProperties mappingProperties, JsonLdContextLoader contextLoader) {
		this(mappingProperties, contextLoader, Clock.systemUTC());
	}

	JsonLdContextResolver(MappingProperties mappingProperties, JsonLdContextLoader contextLoader, Clock clock) {
		this.mappingProperties = mappingProperties;
		this.contextLoader = contextLoader;
		this.clock = clock;
	}

	/**
	 * The resolved context, or {@link JsonLdContext#EMPTY} if compaction against the context is switched off or the
	 * document is (currently) not retrievable.
	 *
	 * @return the context to compact expanded attribute names with
	 */
	public JsonLdContext resolve() {
		if (!mappingProperties.isCompactAgainstContext()) {
			return JsonLdContext.EMPTY;
		}
		JsonLdContext current = resolvedContext;
		return current != null ? current : retrieve();
	}

	private synchronized JsonLdContext retrieve() {
		if (resolvedContext != null) {
			return resolvedContext;
		}
		String contextUrl = mappingProperties.getContextUrl();
		if (contextUrl == null || contextUrl.isBlank()) {
			resolvedContext = JsonLdContext.EMPTY;
			return resolvedContext;
		}
		if (clock.instant().isBefore(earliestRetry)) {
			// a previous retrieval failed and the retry delay has not passed yet
			return JsonLdContext.EMPTY;
		}
		Optional<JsonLdContext> context = buildFrom(contextUrl);
		if (context.isEmpty()) {
			earliestRetry = clock.instant().plus(mappingProperties.getContextRetrievalRetryDelay());
			log.warn("The context {} could not be retrieved, expanded attribute names are not compacted until {}.",
					contextUrl, earliestRetry);
			return JsonLdContext.EMPTY;
		}
		resolvedContext = context.get();
		log.debug("Resolved the context {} to {} terms and the vocabularies {}.",
				contextUrl, resolvedContext.getTermsByIri().size(), resolvedContext.getVocabularies());
		return resolvedContext;
	}

	/**
	 * The context built from the document at the given url, or empty if the document could not be retrieved.
	 */
	private Optional<JsonLdContext> buildFrom(String contextUrl) {
		Collector collector = new Collector();
		if (!collectFromUrl(contextUrl, collector, 0)) {
			return Optional.empty();
		}
		return Optional.of(collector.toContext());
	}

	/**
	 * Collects the definitions of a remote context. Returns false if the document could not be retrieved or parsed.
	 */
	private boolean collectFromUrl(String url, Collector collector, int depth) {
		if (depth > MAX_CONTEXT_DEPTH) {
			log.warn("Stopped following nested contexts at {}, the maximum depth of {} is reached.", url, MAX_CONTEXT_DEPTH);
			return false;
		}
		if (!collector.visitedUrls.add(url)) {
			// already collected, a context referencing another one twice is not an error
			return true;
		}
		Optional<String> document = contextLoader.load(url);
		if (document.isEmpty()) {
			return false;
		}
		try {
			collect(documentMapper.readValue(document.get(), Object.class), collector, depth);
			return true;
		} catch (RuntimeException e) {
			log.warn("The context {} is not a valid json document.", url, e);
			return false;
		}
	}

	private void collect(Object context, Collector collector, int depth) {
		switch (context) {
			case String url -> collectFromUrl(url, collector, depth + 1);
			case List<?> contexts -> contexts.forEach(entry -> collect(entry, collector, depth));
			case Map<?, ?> definitions -> collectFromMap(definitions, collector, depth);
			case null, default -> log.debug("Ignoring the unsupported context entry {}.", context);
		}
	}

	private void collectFromMap(Map<?, ?> definitions, Collector collector, int depth) {
		Object nestedContext = definitions.get(CONTEXT_KEY);
		if (nestedContext != null) {
			// a context document wraps the definitions in "@context"
			collect(nestedContext, collector, depth);
			return;
		}
		definitions.forEach((key, definition) -> {
			String term = String.valueOf(key);
			if (VOCAB_KEY.equals(term)) {
				iriOf(definition).ifPresent(collector.vocabularies::add);
			} else if (!term.startsWith(KEYWORD_PREFIX)) {
				// a later definition of the same term overrides the earlier one
				iriOf(definition).ifPresent(iri -> collector.termsToIris.put(term, iri));
			}
		});
	}

	/**
	 * The IRI of a term definition, either given directly or as its {@code @id}.
	 */
	private static Optional<String> iriOf(Object definition) {
		if (definition instanceof String iri) {
			return Optional.of(iri);
		}
		if (definition instanceof Map<?, ?> expandedDefinition && expandedDefinition.get(ID_KEY) instanceof String iri) {
			return Optional.of(iri);
		}
		return Optional.empty();
	}

	/**
	 * Expands a compact IRI such as {@code tmf:name} against the prefixes defined in the same context. Returns the
	 * input for an already absolute IRI, an unknown prefix or a cyclic prefix definition.
	 */
	private static String expandIri(String iri, Map<String, String> termsToIris, Set<String> visitedPrefixes) {
		if (iri == null || JsonLdContext.isAbsoluteIri(iri)) {
			return iri;
		}
		int separator = iri.indexOf(COMPACT_IRI_SEPARATOR);
		if (separator <= 0) {
			return iri;
		}
		String prefix = iri.substring(0, separator);
		String prefixIri = termsToIris.get(prefix);
		if (prefixIri == null || !visitedPrefixes.add(prefix)) {
			return iri;
		}
		return expandIri(prefixIri, termsToIris, visitedPrefixes) + iri.substring(separator + 1);
	}

	/**
	 * Mutable state of a single resolution run.
	 */
	private static final class Collector {

		private final Map<String, String> termsToIris = new LinkedHashMap<>();
		private final List<String> vocabularies = new ArrayList<>();
		private final Set<String> visitedUrls = new HashSet<>();

		/**
		 * The collected definitions as a context: compact IRIs expanded, keyed by IRI. Among several terms for the same
		 * IRI the first collected one wins, so the result does not depend on the iteration order of the document.
		 */
		private JsonLdContext toContext() {
			Map<String, String> termsByIri = new LinkedHashMap<>();
			termsToIris.forEach((term, iri) -> {
				String expanded = expandIri(iri, termsToIris, new HashSet<>());
				if (JsonLdContext.isAbsoluteIri(expanded)) {
					termsByIri.putIfAbsent(expanded, term);
				}
			});
			List<String> expandedVocabularies = vocabularies.stream()
					.map(vocabulary -> expandIri(vocabulary, termsToIris, new HashSet<>()))
					.filter(JsonLdContext::isAbsoluteIri)
					.toList();
			return new JsonLdContext(termsByIri, expandedVocabularies);
		}
	}

}
