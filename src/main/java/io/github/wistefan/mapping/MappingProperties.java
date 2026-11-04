package io.github.wistefan.mapping;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.time.Duration;

/**
 * General properties to be used for the mapping.
 */
@ConfigurationProperties("mapping")
@Data
public class MappingProperties {

	/**
	 * Default timeout for retrieving a context document.
	 */
	private static final Duration DEFAULT_CONTEXT_RETRIEVAL_TIMEOUT = Duration.ofSeconds(10);

	/**
	 * Default time to wait before a failed context retrieval is attempted again.
	 */
	private static final Duration DEFAULT_CONTEXT_RETRIEVAL_RETRY_DELAY = Duration.ofSeconds(60);

	/**
	 * ContextUrl for the service to use.
	 */
	private String contextUrl = "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld";

	/**
	 * When set to true, mapping will fail if a realtionship cannot be resolved.
	 */
	private boolean strictRelationships = true;

	/**
	 * When set to true, attribute names that a broker returned expanded under the context at {@link #contextUrl} are
	 * compacted back to their terms on read. The context is only retrieved once an attribute name actually shows up as
	 * an IRI that the built-in NGSI-LD prefixes do not cover, deployments using the core context never retrieve it.
	 */
	private boolean compactAgainstContext = true;

	/**
	 * Timeout for retrieving the context document at {@link #contextUrl}.
	 */
	private Duration contextRetrievalTimeout = DEFAULT_CONTEXT_RETRIEVAL_TIMEOUT;

	/**
	 * Time to wait before retrieving a context document again that could not be retrieved. Until it has passed, the
	 * mapping continues without the context instead of retrying on every entity.
	 */
	private Duration contextRetrievalRetryDelay = DEFAULT_CONTEXT_RETRIEVAL_RETRY_DELAY;
}
