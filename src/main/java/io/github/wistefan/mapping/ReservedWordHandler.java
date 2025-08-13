package io.github.wistefan.mapping;

import java.util.List;

/**
 * Handler for reserved words in NGSI-LD
 */
public class ReservedWordHandler {

	/**
	 * Words that have a special meaning in NGSI-LD and are not allowed to be used in non-compliant ways.
	 */
	private static final List<String> RESERVED_WORDS = List.of("id", "@id", "value", "@value", "type", "@type");

	/**
	 * Prefix to be used for escaping the reserved words.
	 */
	private static final String ESCAPE_PREFIX = "tmfEscaped-";

	/**
	 * Escape reserved words in keys.
	 *
	 * @param unmappedProperty the property to escape
	 * @return the escaped property
	 */
	public static UnmappedProperty escapeReservedWords(UnmappedProperty unmappedProperty) {
		if (RESERVED_WORDS.contains(unmappedProperty.getName())) {
			unmappedProperty.setName(String.format("%s%s", ESCAPE_PREFIX, unmappedProperty.getName()));
		}
		return unmappedProperty;
	}

	/**
	 * Check if the unmapped property is an reserved word property
	 *
	 * @param unmappedProperty the property to check
	 * @return true if the key is a reserved word
	 */
	public static boolean isReservedProperty(UnmappedProperty unmappedProperty) {
		return unmappedProperty.getName().startsWith(ESCAPE_PREFIX);
	}

	/**
	 * Removes the escape prefix from the property
	 *
	 * @param unmappedProperty the property to clean
	 * @return the cleaned property
	 */
	public static UnmappedProperty removeEscape(UnmappedProperty unmappedProperty) {
		if (isReservedProperty(unmappedProperty)) {
			unmappedProperty.setName(unmappedProperty.getName().replaceFirst(ESCAPE_PREFIX, ""));
		}
		return unmappedProperty;
	}
}
