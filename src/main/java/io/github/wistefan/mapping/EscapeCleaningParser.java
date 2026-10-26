package io.github.wistefan.mapping;


import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.SerializableString;
import tools.jackson.core.sym.PropertyNameMatcher;
import tools.jackson.core.util.JsonParserDelegate;

/**
 * Implementation of the JsonParser, that cleans json-key's that are escaped to comply with NGSI-LD.
 * An entry "tmfEscaped-@id": "my-id" will become "@id":"my-id" after the cleaning.
 *
 * Keys whose unescaped form would collide with an explicit setter on the
 * generated VOs (see {@link ReservedWordHandler#canUnescapeDuringParsing(String)})
 * are kept escaped: Jackson would otherwise route them to the structural field
 * (e.g. {@code PropertyVO.value}) and overwrite it, dropping the entry from
 * {@code additionalProperties}. Those keys are stripped of their prefix later,
 * in {@code EntityVOMapper}, when the user-facing keys are surfaced.
 */
public class EscapeCleaningParser extends JsonParserDelegate {

	public EscapeCleaningParser(JsonParser d) {
		super(d);
	}

	@Override
	public String currentName() {
		return cleanName(super.currentName());
	}

	// Jackson 3's delegate forwards the name-advancing and name-matching methods straight to the
	// wrapped parser; bean deserializers use them, so they have to go through cleanName as well.
	@Override
	public String nextName() {
		return nextToken() == JsonToken.PROPERTY_NAME ? currentName() : null;
	}

	@Override
	public boolean nextName(SerializableString str) {
		return nextToken() == JsonToken.PROPERTY_NAME && str.getValue().equals(currentName());
	}

	@Override
	public int nextNameMatch(PropertyNameMatcher matcher) {
		nextToken();
		return currentNameMatch(matcher);
	}

	@Override
	public int currentNameMatch(PropertyNameMatcher matcher) {
		JsonToken token = currentToken();
		if (token == JsonToken.PROPERTY_NAME) {
			return matcher.matchName(currentName());
		}
		if (token == JsonToken.END_OBJECT) {
			return PropertyNameMatcher.MATCH_END_OBJECT;
		}
		return PropertyNameMatcher.MATCH_ODD_TOKEN;
	}

	@Override
	public String getString() {
		return currentToken() == JsonToken.PROPERTY_NAME ? currentName() : super.getString();
	}

	@Override
	public String getValueAsString() {
		return currentToken() == JsonToken.PROPERTY_NAME ? currentName() : super.getValueAsString();
	}

	@Override
	public String getValueAsString(String defaultValue) {
		return currentToken() == JsonToken.PROPERTY_NAME ? currentName() : super.getValueAsString(defaultValue);
	}

	private static String cleanName(String name) {
		if (name == null) {
			return null;
		}
		if (ReservedWordHandler.canUnescapeDuringParsing(name)) {
			return ReservedWordHandler.removeEscape(name);
		}
		return name;
	}

}
