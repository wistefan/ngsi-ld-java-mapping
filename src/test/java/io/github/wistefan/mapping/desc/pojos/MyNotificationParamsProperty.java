package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import lombok.*;

import java.util.Set;

@EqualsAndHashCode
@ToString
@Data
public class MyNotificationParamsProperty {

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY_SET, targetName = "attributes")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY_SET, targetName = "attributes")}))
    private Set<String> attributes;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "format")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "format")}))
    private String format;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "endpoint")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "endpoint",
            targetClass = MyNotificationParamsEndpointProperty.class)}))
    private MyNotificationParamsEndpointProperty endpoint = new MyNotificationParamsEndpointProperty();
}
