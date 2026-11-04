package io.github.wistefan.mapping;

import java.util.Optional;

/**
 * Retrieves the raw JSON-LD context document for a given URL. Implement and provide as a bean to replace the
 * default {@link HttpJsonLdContextLoader}, e.g. to serve the context from the classpath or to add authentication.
 */
@FunctionalInterface
public interface JsonLdContextLoader {

	/**
	 * Retrieve the document at the given URL. Implementations report an unreachable or invalid document as an empty
	 * result instead of throwing, the mapping continues without the context in that case.
	 *
	 * @param url the url of the context document
	 * @return the document body, or empty if it cannot be retrieved
	 */
	Optional<String> load(String url);

}
