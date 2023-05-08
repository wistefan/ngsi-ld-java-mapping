package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.Getter;
import lombok.Setter;
import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;
import io.github.wistefan.mapping.desc.pojos.MySubPropertyEntity;

import java.net.URI;
import java.util.List;

@MappingEnabled(entityType = "my-pojo")
public class MyInvalidListRelationshipPojo {
    private static final String ENTITY_TYPE = "my-pojo";

    private URI id;

    private String myName;
    private List<Integer> numbers;

    // required constructor
    public MyInvalidListRelationshipPojo(String id) {
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

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.RELATIONSHIP_LIST, targetName = "sub-entity")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.RELATIONSHIP_LIST, targetName = "sub-entity", targetClass = MySubPropertyEntity.class)}))
    private MySubPropertyEntity mySubPropertyEntity;
}
