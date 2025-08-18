package io.github.wistefan.mapping;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

import java.io.IOException;

/**
 * Implementation of the JsonParser, that cleans json-key's that are escaped to comply with NGSI-LD.
 * An entry "tmfEscaped-@id": "my-id" will become "@id":"my-id" after the cleaning.
 */
public class EscapeCleaningParser extends JsonParserDelegate {

	public EscapeCleaningParser(JsonParser d) {
		super(d);
	}

	@Override
	public String getCurrentName() throws IOException {
		return ReservedWordHandler.removeEscape(super.currentName());
	}

	@Override
	public String currentName() throws IOException {
		return ReservedWordHandler.removeEscape(super.currentName());
	}


}
