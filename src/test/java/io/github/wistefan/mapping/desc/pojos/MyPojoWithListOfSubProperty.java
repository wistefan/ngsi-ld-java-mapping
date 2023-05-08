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
import java.util.List;

@EqualsAndHashCode
@ToString
@MappingEnabled(entityType = "complex-pojo")
public class MyPojoWithListOfSubProperty {

    @Getter(onMethod = @__({@EntityId}))
    private URI id;

    @Getter(onMethod = @__({@EntityType}))
    private String type = "complex-pojo";

    public MyPojoWithListOfSubProperty(String id) {
        this.id = URI.create(id);
    }

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY_LIST, targetName = "mySubProperty")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY_LIST, targetName = "mySubProperty", targetClass = MySubProperty.class)}))
    private List<MySubProperty> mySubProperties;
}
