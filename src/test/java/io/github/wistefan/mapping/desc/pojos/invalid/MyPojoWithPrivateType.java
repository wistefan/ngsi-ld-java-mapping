package io.github.wistefan.mapping.desc.pojos.invalid;

import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.net.URI;

@MappingEnabled(entityType = "my-pojo")
public class MyPojoWithPrivateType {
    private static final String ENTITY_TYPE = "my-pojo";

    private URI id;

    @EntityId
    public URI getId() {
        return URI.create("id");
    }

    @EntityType
    private String getType() {
        return ENTITY_TYPE;
    }

}
