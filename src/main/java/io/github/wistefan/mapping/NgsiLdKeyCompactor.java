package io.github.wistefan.mapping;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Turns expanded attribute names back into their terms.
 * Orion-LD 1.9.0 returns the sub-attributes of RELATIONSHIP_LIST instances under their expanded names. Which IRIs
 * those are depends on the context the broker expands against: the NGSI-LD core and default-context prefixes are
 * known here, anything else is resolved against the context configured at {@link MappingProperties#getContextUrl()}.
 */
@Singleton
public final class NgsiLdKeyCompactor {

	static final String DEFAULT_CONTEXT_PREFIX = "https://uri.etsi.org/ngsi-ld/default-context/";
	static final String CORE_CONTEXT_PREFIX = "https://uri.etsi.org/ngsi-ld/";

	/**
	 * Longest prefix first, since the default-context prefix starts with the core prefix.
	 */
	private static final List<String> PREFIXES = List.of(DEFAULT_CONTEXT_PREFIX, CORE_CONTEXT_PREFIX);

	private final Supplier<JsonLdContext> contextSupplier;

	/**
	 * Compactor that only knows the built-in NGSI-LD prefixes, for use without the configured context.
	 */
	public NgsiLdKeyCompactor() {
		this(() -> JsonLdContext.EMPTY);
	}

	@Inject
	public NgsiLdKeyCompactor(JsonLdContextResolver contextResolver) {
		this(contextResolver::resolve);
	}

	/**
	 * Compactor resolving against an already known context, for use without the {@link JsonLdContextResolver}.
	 */
	public NgsiLdKeyCompactor(JsonLdContext context) {
		this(() -> context);
	}

	NgsiLdKeyCompactor(Supplier<JsonLdContext> contextSupplier) {
		this.contextSupplier = contextSupplier;
	}

	/**
	 * Strips a known NGSI-LD prefix or resolves the IRI against the configured context, and applies the parser's
	 * unescape rule; other keys (and null) are returned unchanged.
	 *
	 * @param key the attribute name as it arrives from the broker
	 * @return the compact attribute name
	 */
	public String compact(String key) {
		if (key == null) {
			return null;
		}
		for (String prefix : PREFIXES) {
			if (key.startsWith(prefix)) {
				return asTerm(key.substring(prefix.length()));
			}
		}
		if (!JsonLdContext.isAbsoluteIri(key)) {
			// an ordinary attribute name, nothing to compact and no reason to retrieve the context
			return key;
		}
		return contextSupplier.get().compact(key)
				.map(NgsiLdKeyCompactor::asTerm)
				.orElse(key);
	}

	/**
	 * Returns a map with compacted keys in insertion order; an already compact key wins over an expanded one,
	 * and among expanded keys that compact to the same name the first in iteration order wins.
	 * Returns the input itself when no key needs compaction, and null for null.
	 *
	 * @param map  the attributes as they arrive from the broker
	 * @param <V>  type of the attribute values
	 * @return the attributes under their compact names
	 */
	public <V> Map<String, V> compactKeys(Map<String, V> map) {
		if (map == null || map.keySet().stream().noneMatch(this::isExpanded)) {
			return map;
		}
		Map<String, V> compacted = new LinkedHashMap<>();
		map.forEach((key, value) -> {
			String compactedKey = compact(key);
			if (Objects.equals(compactedKey, key)) {
				compacted.put(key, value);
			} else {
				compacted.putIfAbsent(compactedKey, value);
			}
		});
		return compacted;
	}

	/**
	 * Whether the given key is an expanded form of another one.
	 *
	 * @param key the attribute name as it arrives from the broker
	 * @return true if compacting the key changes it
	 */
	public boolean isExpanded(String key) {
		return key != null && !Objects.equals(compact(key), key);
	}

	/**
	 * The EscapeCleaningParser cannot see the escape behind the prefix, so apply its rule here.
	 */
	private static String asTerm(String compacted) {
		return ReservedWordHandler.canUnescapeDuringParsing(compacted) ? ReservedWordHandler.removeEscape(compacted) : compacted;
	}

}
