package io.github.wistefan.mapping.desc.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.EntityType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
public class MySimplePojo {
	private static final String ENTITY_TYPE = "my-pojo";
	private String myName;
	private List<Integer> numbers;

	@EntityType
	@JsonIgnore
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
