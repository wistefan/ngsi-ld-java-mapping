package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import io.github.wistefan.mapping.annotations.DatasetId;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.net.URI;

@EqualsAndHashCode
@MappingEnabled(entityType = "sub-entity")
public class MySubEntityWithoutRelationshipObject {


    @Getter(onMethod = @__({@EntityId, @DatasetId}))
    private URI id;

    @Getter(onMethod = @__({@EntityType}))
    private String type = "sub-entity";


}
