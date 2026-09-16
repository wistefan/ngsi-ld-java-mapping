package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.*;
import lombok.EqualsAndHashCode;

import java.net.URI;

/**
 * Regression fixture for a domain property explicitly mapped ({@code @AttributeSetter}/
 * {@code @AttributeGetter}) to an NGSI-LD reserved word ({@code value}). The broker (and
 * {@code JavaObjectMapper} on write) carries such attributes under the
 * {@link io.github.wistefan.mapping.ReservedWordHandler#escapeReservedWords escaped} name
 * (e.g. {@code tmfEscaped-value}) - {@code EntityVOMapper} must unescape it back before matching
 * against this class' {@code targetName = "value"}, or the property silently never gets set.
 */
@MappingEnabled(entityType = "my-pojo")
@EqualsAndHashCode
public class MyPojoWithReservedWordProperty {

	private static final String ENTITY_TYPE = "my-pojo";

	private URI id;

	private String myValue;

	// required constructor
	public MyPojoWithReservedWordProperty(String id) {
		this.id = URI.create(id);
	}

	@EntityId
	public URI getId() {
		return id;
	}

	@EntityType
	public String getType() {
		return ENTITY_TYPE;
	}

	@AttributeGetter(value = AttributeType.PROPERTY, targetName = "value")
	public String getMyValue() {
		return myValue;
	}

	@AttributeSetter(value = AttributeType.PROPERTY, targetName = "value")
	public void setMyValue(String myValue) {
		this.myValue = myValue;
	}
}
