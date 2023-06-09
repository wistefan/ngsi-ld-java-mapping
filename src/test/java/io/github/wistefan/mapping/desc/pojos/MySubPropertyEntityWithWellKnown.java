package io.github.wistefan.mapping.desc.pojos;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.DatasetId;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;
import io.github.wistefan.mapping.annotations.RelationshipObject;

import java.net.URI;
import java.time.Instant;

@EqualsAndHashCode
@ToString
@MappingEnabled(entityType = "sub-entity")
public class MySubPropertyEntityWithWellKnown {

    @Getter(onMethod = @__({@EntityId, @RelationshipObject}))
    private URI id;

    @Getter(onMethod = @__({@EntityType}))
    private String type = "sub-entity";

    public MySubPropertyEntityWithWellKnown(String id) {
        this.id = URI.create(id);
    }

    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "observedAt", targetClass = Instant.class)}))
    private Instant observedAt;

    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "modifiedAt", targetClass = Instant.class)}))
    private Instant modifiedAt;

    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "createdAt", targetClass = Instant.class)}))
    private Instant createdAt;

    @Setter(onMethod = @__({@DatasetId, @AttributeSetter(value = AttributeType.PROPERTY, targetName = "datasetId")}))
    private String datasetId;

    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "instanceId")}))
    private String instanceId;


}
