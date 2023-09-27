package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.*;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * Abstract superclass for all entities with an id
 */
public abstract class EntityWithId {

    /**
     * Type of the entity
     */
    @Getter(onMethod = @__({@EntityType}))
    final String type;

    /**
     * Id of the entity. This is the id part of "urn:ngsi-ld:TYPE:ID"
     */
    @Ignore
    @Getter(onMethod = @__({@EntityId, @RelationshipObject, @DatasetId}))
    @Setter
    URI id;

    protected EntityWithId(String type, String id) {
        this.type = type;
        this.id = URI.create(id);
    }

    /**
     * When sub-classing, this defines the super-class
     */
    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "atBaseType")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "atBaseType")}))
    @Nullable
    String atBaseType;

    /**
     * A URI to a JSON-Schema file that defines additional attributes and relationships
     */
    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "atSchemaLocation")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "atSchemaLocation")}))
    @Nullable
    URI atSchemaLocation;

    /**
     * When sub-classing, this defines the sub-class entity name.
     * We cannot use @type, since it clashes with the ngsi-ld type field(e.g. reserved name)
     */
    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "tmForumType")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "tmForumType")}))
    @Nullable
    String atType;

    public String getEntityState() {
        return "default";
    }

}
