package io.github.wistefan.mapping.desc.pojos.invalid;

import lombok.Builder;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.util.function.Consumer;

@Builder
@MappingEnabled(entityType = "my-pojo")
public class MySetterThrowingPojo {

    public Consumer<String> attributeConsumer;

    @AttributeSetter(value = AttributeType.PROPERTY, targetName = "name")
    public void setName(String name) {
        throw new RuntimeException("Something is wrong with me.");
    }

}
