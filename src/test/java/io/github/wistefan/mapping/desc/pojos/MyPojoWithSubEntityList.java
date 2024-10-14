package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.List;

@EqualsAndHashCode
@MappingEnabled(entityType = "complex-pojo")
public class MyPojoWithSubEntityList {

    @Getter(onMethod = @__({@EntityId}))
    private URI id;

    @Getter(onMethod = @__({@EntityType}))
    private String type = "complex-pojo";

    public MyPojoWithSubEntityList(String id) {
        this.id = URI.create(id);
    }

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.RELATIONSHIP_LIST, targetName = "sub-entity-list")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.RELATIONSHIP_LIST, targetName = "sub-entity-list", targetClass = MySubPropertyEntity.class)}))
    private List<MySubPropertyEntity> mySubPropertyList;
}
