package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.*;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@MappingEnabled (entityType = "location-pojo")
public class MyPojoWithLocation {
    @Getter(onMethod = @__({@EntityId}))
    private URI id;

    @Getter(onMethod = @__({@EntityType}))
    private String type = "location-pojo";

    public MyPojoWithLocation(String id) {
        this.id = URI.create(id);
    }

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.GEO_PROPERTY, targetName = "myLocation")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.GEO_PROPERTY, targetName = "myLocation")}))
    private MyLocation myLocation;
}