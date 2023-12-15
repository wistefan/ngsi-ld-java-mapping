package io.github.wistefan.mapping.desc.pojos.subscription;

import lombok.*;

import java.util.Set;

@EqualsAndHashCode
@ToString
@Data
@Getter
public class MyNotificationParamsProperty {

    @Setter
    private Set<String> attributes;

    @Setter
    private String format;

    @Setter
    private MyNotificationParamsEndpointProperty endpoint = new MyNotificationParamsEndpointProperty();
}
