package io.github.wistefan.mapping;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A resolved JSON-LD context, reduced to what is needed to turn an expanded attribute IRI back into its term.
 * Instances are immutable and safe to share between threads.
 */
public final class JsonLdContext {

	/**
	 * Context without any term definition. Used when no context is configured or the configured one cannot be retrieved.
	 */
	public static final JsonLdContext EMPTY = new JsonLdContext(Map.of(), List.of());

	/**
	 * Shape of an expanded IRI, as opposed to a term ({@code name}) or a compact IRI ({@code tmf:name}). Only schemes
	 * that actually carry attribute IRIs are accepted, a bare scheme test would also match every compact IRI.
	 */
	private static final Pattern ABSOLUTE_IRI = Pattern.compile("^(urn:|[A-Za-z][A-Za-z0-9+.-]*://)");

	/**
	 * Separator between the scheme and the rest of an IRI.
	 */
	private static final char SCHEME_SEPARATOR = ':';

	private final Map<String, String> termsByIri;
	private final List<String> vocabularies;

	/**
	 * @param termsByIri   term to use for an expanded IRI, keyed by that IRI
	 * @param vocabularies {@code @vocab} IRIs of the context, any term expanded below one of them is its remainder
	 */
	public JsonLdContext(Map<String, String> termsByIri, List<String> vocabularies) {
		this.termsByIri = Map.copyOf(termsByIri);
		// longest first, so the most specific vocabulary wins when one is a prefix of another
		this.vocabularies = vocabularies.stream()
				.distinct()
				.sorted(Comparator.comparingInt(String::length).reversed())
				.toList();
	}

	/**
	 * The term the given expanded IRI was mapped from, if this context defines one. An explicit term definition takes
	 * precedence over a {@code @vocab}, since the latter only applies to terms the context does not define.
	 *
	 * @param iri the expanded attribute IRI as returned by the broker
	 * @return the term, or empty if this context does not cover the IRI
	 */
	public Optional<String> compact(String iri) {
		if (iri == null) {
			return Optional.empty();
		}
		String term = termsByIri.get(iri);
		if (term != null) {
			return Optional.of(term);
		}
		return vocabularies.stream()
				.filter(vocabulary -> iri.startsWith(vocabulary) && iri.length() > vocabulary.length())
				.findFirst()
				.map(vocabulary -> iri.substring(vocabulary.length()));
	}

	/**
	 * Whether the given key is an expanded IRI, i.e. a name that may need compaction at all.
	 *
	 * @param key the attribute name as it arrives from the broker
	 * @return true if the name is an absolute IRI
	 */
	public static boolean isAbsoluteIri(String key) {
		// the scheme separator is absent from virtually every attribute name, checking it first keeps the regex off the hot path
		return key != null && key.indexOf(SCHEME_SEPARATOR) > 0 && ABSOLUTE_IRI.matcher(key).find();
	}

	/**
	 * Whether this context defines anything that could compact an IRI.
	 */
	public boolean isEmpty() {
		return termsByIri.isEmpty() && vocabularies.isEmpty();
	}

	/**
	 * The known terms by their expanded IRI, for diagnostics.
	 */
	public Map<String, String> getTermsByIri() {
		return new LinkedHashMap<>(termsByIri);
	}

	/**
	 * The {@code @vocab} IRIs of this context, longest first.
	 */
	public List<String> getVocabularies() {
		return vocabularies;
	}

}
