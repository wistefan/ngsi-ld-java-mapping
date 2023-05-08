package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;
import io.github.wistefan.mapping.desc.pojos.MySubPropertyEntityWithWellKnown;

import java.net.URI;

@EqualsAndHashCode
@ToString
@MappingEnabled(entityType = "complex-pojo")
public class MyPojoWithSubEntityWellKnown {

    @Getter(onMethod = @__({@EntityId}))
    private URI id;

    @Getter(onMethod = @__({@EntityType}))
    private String type = "complex-pojo";

    public MyPojoWithSubEntityWellKnown(String id) {
        this.id = URI.create(id);
    }

    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.RELATIONSHIP, targetName = "mySubProperty", fromProperties = true, targetClass = MySubPropertyEntityWithWellKnown.class)}))
    private MySubPropertyEntityWithWellKnown mySubProperty;
}
