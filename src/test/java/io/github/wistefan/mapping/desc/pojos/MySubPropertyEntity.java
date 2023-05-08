package io.github.wistefan.mapping.desc.pojos;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.DatasetId;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;
import io.github.wistefan.mapping.annotations.RelationshipObject;

import java.net.URI;

@EqualsAndHashCode
@ToString
@MappingEnabled(entityType = "sub-entity")
public class MySubPropertyEntity {

	@Getter(onMethod = @__({@EntityId, @RelationshipObject, @DatasetId}))
	private URI id;

	@Getter(onMethod = @__({@EntityType}))
	private String type = "sub-entity";

	public MySubPropertyEntity(String id) {
		this.id = URI.create(id);
	}

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "name")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "name")}))
	private String name = "myName";
}
