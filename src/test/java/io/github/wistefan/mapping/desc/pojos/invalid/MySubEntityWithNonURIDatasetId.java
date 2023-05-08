package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import io.github.wistefan.mapping.annotations.DatasetId;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;
import io.github.wistefan.mapping.annotations.RelationshipObject;

import java.net.URI;

@EqualsAndHashCode
@MappingEnabled(entityType = "sub-entity")
public class MySubEntityWithNonURIDatasetId {

    @Getter(onMethod = @__({@EntityId, @RelationshipObject}))
    private URI id;

    @Getter(onMethod = @__({@DatasetId}))
    private String invalidDatasetId;

    @Getter(onMethod = @__({@EntityType}))
    private String type = "sub-entity";

    public MySubEntityWithNonURIDatasetId(String id) {
        this.id = URI.create(id);
    }
}
