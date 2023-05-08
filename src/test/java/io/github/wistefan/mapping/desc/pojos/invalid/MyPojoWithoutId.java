package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.Data;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

@Data
@MappingEnabled(entityType = "my-pojo")
public class MyPojoWithoutId {

    private static final String ENTITY_TYPE = "my-pojo";

    private String myName;

    @EntityType
    public String getType() {
        return ENTITY_TYPE;
    }
}
