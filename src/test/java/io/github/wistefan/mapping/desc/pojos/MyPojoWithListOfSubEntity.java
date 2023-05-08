package io.github.wistefan.mapping.desc.pojos;

import lombok.Getter;
import lombok.Setter;
import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.net.URI;
import java.util.List;

@MappingEnabled(entityType = "complex-pojo")
public class MyPojoWithListOfSubEntity {

	@Getter(onMethod = @__({@EntityId}))
	private URI id;

	@Getter(onMethod = @__({@EntityType}))
	private String type = "complex-pojo";

	public MyPojoWithListOfSubEntity(String id) {
		this.id = URI.create(id);
	}

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.RELATIONSHIP_LIST, targetName = "sub-entity")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.RELATIONSHIP_LIST, targetName = "sub-entity", targetClass = MySubPropertyEntity.class)}))
	private List<MySubPropertyEntity> mySubProperty;

}
