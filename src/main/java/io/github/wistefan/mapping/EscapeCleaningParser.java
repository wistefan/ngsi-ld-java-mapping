package io.github.wistefan.mapping;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

import java.io.IOException;

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
	public String getCurrentName() throws IOException {
		return cleanName(super.currentName());
	}

	@Override
	public String currentName() throws IOException {
		return cleanName(super.currentName());
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
