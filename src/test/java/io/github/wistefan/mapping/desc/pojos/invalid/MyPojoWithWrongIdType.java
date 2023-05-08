package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.AllArgsConstructor;
import lombok.Setter;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

@AllArgsConstructor
@MappingEnabled(entityType = "my-pojo")
public class MyPojoWithWrongIdType {

    private static final String ENTITY_TYPE = "my-pojo";

    @Setter
    private Integer id;

    @EntityId
    public Integer getId() {
        return id;
    }

    @EntityType
    public String getType() {
        return ENTITY_TYPE;
    }
}
