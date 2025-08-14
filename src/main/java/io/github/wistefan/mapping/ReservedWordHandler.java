package io.github.wistefan.mapping;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Handler for reserved words in NGSI-LD
 */
@Slf4j
public class ReservedWordHandler {

	/**
	 * Words that have a special meaning in NGSI-LD and are not allowed to be used in non-compliant ways.
	 */
	private static final List<String> RESERVED_WORDS = List.of("id", "@id", "value", "@value", "type", "@type", "context", "@context");

	/**
	 * Prefix to be used for escaping the reserved words.
	 */
	private static final String ESCAPE_PREFIX = "tmfEscaped-";

	/**
	 * Escape reserved words in keys.
	 *
	 * @param key the key to escape
	 * @return the escaped key
	 */
	public static String escapeReservedWords(String key) {
		log.info("Check the word {}", key);
		if (RESERVED_WORDS.contains(key)) {
			return String.format("%s%s", ESCAPE_PREFIX, key);
		}
		return key;
	}

	/**
	 * Check if the key is an reserved word
	 *
	 * @param key the key to check
	 * @return true if the key is a reserved word
	 */
	public static boolean isReservedProperty(String key) {
		log.info("Check the word {}", key);
		return key.startsWith(ESCAPE_PREFIX);
	}

	/**
	 * Removes the escape prefix from the key
	 *
	 * @param key the key to clean
	 * @return the cleaned key
	 */
	public static String removeEscape(String key) {
		log.info("Check the word {}", key);
		if (isReservedProperty(key)) {
			return key.replaceFirst(ESCAPE_PREFIX, "");
		}
		return key;
	}
}
