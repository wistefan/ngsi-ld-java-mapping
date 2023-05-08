package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.Getter;
import lombok.Setter;
import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.net.URI;

@MappingEnabled(entityType = "my-pojo")
public class MyInvalidRelationshipPojo {
    
    private static final String ENTITY_TYPE = "my-pojo";

    private URI id;

    // required constructor
    public MyInvalidRelationshipPojo(String id) {
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

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.RELATIONSHIP, targetName = "sub-entity")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.RELATIONSHIP, targetName = "sub-entity", targetClass = MySubEntityWithoutRelationshipObject.class)}))
    private MySubEntityWithoutRelationshipObject myInvalidRelationshipPojo;
}
