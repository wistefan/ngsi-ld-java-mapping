package io.github.wistefan.mapping;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * General properties to be used for the mapping.
 */
@ConfigurationProperties("mapping")
@Data
public class MappingProperties {

	/**
	 * ContextUrl for the service to use.
	 */
	private String contextUrl = "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld";
}
