package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.UnmappedProperty;
import io.github.wistefan.mapping.annotations.*;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.List;

@MappingEnabled(entityType = "my-pojo")
@EqualsAndHashCode
public class MyPojoWithUnmappedProperties {
	private static final String ENTITY_TYPE = "my-pojo";

	private URI id;

	private String myName;
	private List<UnmappedProperty> unmappedProperties;

	// required constructor
	public MyPojoWithUnmappedProperties(String id) {
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

	@AttributeGetter(value = AttributeType.PROPERTY, targetName = "name")
	public String getMyName() {
		return myName;
	}

	@AttributeSetter(value = AttributeType.PROPERTY, targetName = "name")
	public void setMyName(String myName) {
		this.myName = myName;
	}

	@UnmappedPropertiesGetter
	public List<UnmappedProperty> getUnmappedProperties() {
		return unmappedProperties;
	}

	@UnmappedPropertiesSetter
	public void setUnmappedProperties(List<UnmappedProperty> unmappedProperties) {
		this.unmappedProperties = unmappedProperties;
	}
}
