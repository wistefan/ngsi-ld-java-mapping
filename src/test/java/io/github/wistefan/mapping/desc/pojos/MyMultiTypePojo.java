package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.net.URI;
import java.util.List;

@MappingEnabled(entityType = "my-pojo")
public class MyMultiTypePojo {
	private static final String ENTITY_TYPE = "my-pojo";

	private URI id;

	private String myString;
	private Integer myNumber;
	private Boolean myBoolean;
	private MultiTypeProperty myProperty;

	// required constructor
	public MyMultiTypePojo(String id) {
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

	@AttributeGetter(value = AttributeType.PROPERTY, targetName = "string")
	public String getMyString() {
		return myString;
	}

	@AttributeSetter(value = AttributeType.PROPERTY, targetName = "string")
	public void setMyString(String myString) {
		this.myString = myString;
	}

	@AttributeGetter(value = AttributeType.PROPERTY, targetName = "number")
	public Integer getMyNumber() {
		return myNumber;
	}

	@AttributeSetter(value = AttributeType.PROPERTY, targetName = "number")
	public void setMyNumber(Integer myNumber) {
		this.myNumber = myNumber;
	}

	@AttributeGetter(value = AttributeType.PROPERTY, targetName = "boolean")
	public Boolean getMyBoolean() {
		return myBoolean;
	}

	@AttributeSetter(value = AttributeType.PROPERTY, targetName = "boolean")
	public void setMyBoolean(Boolean myBoolean) {
		this.myBoolean = myBoolean;
	}

	@AttributeGetter(value = AttributeType.PROPERTY, targetName = "property")
	public MultiTypeProperty getMyProperty() {
		return myProperty;
	}

	@AttributeSetter(value = AttributeType.PROPERTY, targetName = "property")
	public void setMyProperty(MultiTypeProperty myProperty) {
		this.myProperty = myProperty;
	}
}
