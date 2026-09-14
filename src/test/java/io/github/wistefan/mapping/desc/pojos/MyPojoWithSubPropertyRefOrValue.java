package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.List;

/**
 * Mirrors the TMForum shape that broke after 1.6.14: a parent entity whose
 * {@code @AttributeSetter(PROPERTY)} field is a complex object holding a
 * nested ref-or-value with reserved-word keys.
 */
@MappingEnabled(entityType = "complex-pojo")
@EqualsAndHashCode
public class MyPojoWithSubPropertyRefOrValue {

	@Getter(onMethod = @__({@EntityId}))
	private URI id;

	@Getter(onMethod = @__({@EntityType}))
	private String type = "complex-pojo";

	public MyPojoWithSubPropertyRefOrValue(String id) {
		this.id = URI.create(id);
	}

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "myRefOrValue")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "myRefOrValue")}))
	private MySubPropertyRefOrValue myRefOrValue;

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY_LIST, targetName = "myRefOrValueList")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY_LIST, targetName = "myRefOrValueList", targetClass = MySubPropertyRefOrValue.class)}))
	private List<MySubPropertyRefOrValue> myRefOrValueList;
}
