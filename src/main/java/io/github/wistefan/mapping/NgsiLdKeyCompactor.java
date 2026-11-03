package io.github.wistefan.mapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Strips the NGSI-LD core and default-context prefixes from attribute names.
 * Orion-LD 1.9.0 returns the sub-attributes of RELATIONSHIP_LIST instances under their expanded names.
 */
public final class NgsiLdKeyCompactor {

	static final String DEFAULT_CONTEXT_PREFIX = "https://uri.etsi.org/ngsi-ld/default-context/";
	static final String CORE_CONTEXT_PREFIX = "https://uri.etsi.org/ngsi-ld/";

	/**
	 * Longest prefix first, since the default-context prefix starts with the core prefix.
	 */
	private static final List<String> PREFIXES = List.of(DEFAULT_CONTEXT_PREFIX, CORE_CONTEXT_PREFIX);

	private NgsiLdKeyCompactor() {
	}

	/**
	 * Strips a known NGSI-LD prefix and applies the parser's unescape rule; other keys (and null) are returned unchanged.
	 */
	public static String compact(String key) {
		if (key == null) {
			return null;
		}
		for (String prefix : PREFIXES) {
			if (key.startsWith(prefix)) {
				// the EscapeCleaningParser cannot see the escape behind the prefix, so apply its rule here
				String stripped = key.substring(prefix.length());
				return ReservedWordHandler.canUnescapeDuringParsing(stripped) ? ReservedWordHandler.removeEscape(stripped) : stripped;
			}
		}
		return key;
	}

	/**
	 * Returns a map with compacted keys in insertion order; an already compact key wins over an expanded one,
	 * and among expanded keys that compact to the same name the first in iteration order wins.
	 * Returns the input itself when no key needs compaction, and null for null.
	 */
	public static <V> Map<String, V> compactKeys(Map<String, V> map) {
		if (map == null || map.keySet().stream().noneMatch(NgsiLdKeyCompactor::isExpanded)) {
			return map;
		}
		Map<String, V> compacted = new LinkedHashMap<>();
		map.forEach((key, value) -> {
			if (isExpanded(key)) {
				compacted.putIfAbsent(compact(key), value);
			} else {
				compacted.put(key, value);
			}
		});
		return compacted;
	}

	private static boolean isExpanded(String key) {
		return key != null && PREFIXES.stream().anyMatch(key::startsWith);
	}

}
