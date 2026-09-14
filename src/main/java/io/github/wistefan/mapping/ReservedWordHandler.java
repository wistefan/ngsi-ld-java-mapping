package io.github.wistefan.mapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * Subset of {@link #RESERVED_WORDS} whose JSON name (after unescape) would
	 * collide with an explicit setter on the generated NGSI-LD VOs
	 * ({@code PropertyVO.value}, {@code PropertyVO.type}, {@code EntityVO.id}, …).
	 *
	 * When such a name appears as an additional attribute on the wire (i.e. with
	 * the {@link #ESCAPE_PREFIX}), the {@link EscapeCleaningParser} must leave the
	 * prefix in place so Jackson routes the entry through {@code @JsonAnySetter}
	 * into {@code additionalProperties} instead of overwriting the structural field.
	 * The unescape then happens later — in {@code EntityVOMapper} — when emitting
	 * the user-facing keys.
	 */
	private static final List<String> VO_FIELD_COLLISIONS = List.of("value", "type", "id");

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
		return key.startsWith(ESCAPE_PREFIX);
	}

	/**
	 * Removes the escape prefix from the key
	 *
	 * @param key the key to clean
	 * @return the cleaned key
	 */
	public static String removeEscape(String key) {
		if (isReservedProperty(key)) {
			var w = key.replaceFirst(ESCAPE_PREFIX, "");
			log.info("Done {}", w);
			return w;
		}
		return key;
	}

	/**
	 * Whether the given (potentially escaped) key should be unescaped during
	 * JSON parsing — i.e. it is safe to surface the original name at the Jackson
	 * field-resolution stage without colliding with an explicit setter on a VO.
	 *
	 * Returns {@code false} for keys whose unescaped form is in
	 * {@link #VO_FIELD_COLLISIONS}: those must keep the {@link #ESCAPE_PREFIX}
	 * until {@code EntityVOMapper} surfaces them, otherwise Jackson would route
	 * the entry to the structural field instead of {@code @JsonAnySetter}.
	 *
	 * @param key the key as it arrives from the broker (possibly escaped)
	 * @return true if the parser may safely strip the escape prefix
	 */
	public static boolean canUnescapeDuringParsing(String key) {
		if (!isReservedProperty(key)) {
			return false;
		}
		String unescaped = key.replaceFirst(ESCAPE_PREFIX, "");
		return !VO_FIELD_COLLISIONS.contains(unescaped);
	}

}
