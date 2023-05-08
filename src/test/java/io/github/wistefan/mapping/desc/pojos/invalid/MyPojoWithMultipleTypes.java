package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.AllArgsConstructor;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.net.URI;

@AllArgsConstructor
@MappingEnabled(entityType = "my-pojo")
public class MyPojoWithMultipleTypes {


    private URI id;

    @EntityId
    public URI getId() {
        return id;
    }

    @EntityType
    public String getType1() {
        return "type-1";
    }

    @EntityType
    public String getType2() {
        return "type-2";
    }
}
