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
public class MyPojo {
	private static final String ENTITY_TYPE = "my-pojo";

	private URI id;

	private String myName;
	private List<Integer> numbers;

	// required constructor
	public MyPojo(String id) {
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

	@AttributeGetter(value = AttributeType.PROPERTY_LIST, targetName = "numbers")
	public List<Integer> getNumbers() {
		return numbers;
	}

	@AttributeSetter(value = AttributeType.PROPERTY_LIST, targetName = "numbers", targetClass = Integer.class)
	public void setNumbers(List<Integer> numbers) {
		this.numbers = numbers;
	}
}
