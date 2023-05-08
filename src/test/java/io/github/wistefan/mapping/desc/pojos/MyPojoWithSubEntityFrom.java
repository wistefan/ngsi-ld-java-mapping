package io.github.wistefan.mapping.desc.pojos;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.EntityId;
import io.github.wistefan.mapping.annotations.EntityType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.net.URI;

@EqualsAndHashCode
@ToString
@MappingEnabled(entityType = "complex-pojo")
public class MyPojoWithSubEntityFrom {

    @Getter(onMethod = @__({@EntityId}))
    private URI id;

    @Getter(onMethod = @__({@EntityType}))
    private String type = "complex-pojo";

    public MyPojoWithSubEntityFrom(String id) {
        this.id = URI.create(id);
    }

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.RELATIONSHIP, targetName = "mySubProperty")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.RELATIONSHIP, targetName = "mySubProperty", fromProperties = true, targetClass = MySubPropertyEntity.class)}))
    private MySubPropertyEntity mySubProperty;
}
