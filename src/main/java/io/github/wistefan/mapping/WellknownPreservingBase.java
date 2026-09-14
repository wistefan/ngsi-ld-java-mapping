package io.github.wistefan.mapping;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class to be used for all generated VOs. Will preserve all properties that are not explicitly mapped.
 */
public class WellknownPreservingBase {

	private Map<String, String> wellknownProperties = new HashMap<>();

	@JsonAnyGetter
	public Map<String, String> getWellknownProperties() {
		return this.wellknownProperties;
	}

	@JsonAnySetter
	public void setWellknownProperties(String propertyKey, String value) {
		if (this.wellknownProperties == null) {
			this.wellknownProperties = new HashMap();
		}

		this.wellknownProperties.put(propertyKey, value);
	}
}
