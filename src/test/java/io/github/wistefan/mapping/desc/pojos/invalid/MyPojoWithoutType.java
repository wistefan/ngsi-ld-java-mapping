package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.AllArgsConstructor;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.net.URI;

@AllArgsConstructor
@MappingEnabled(entityType = "my-pojo")
public class MyPojoWithoutType {

    private URI id;

    @EntityId
    public URI getId() {
        return id;
    }
}
