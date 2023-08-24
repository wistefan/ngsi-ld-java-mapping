package io.github.wistefan.mapping.desc.pojos.subscription;

import lombok.*;

import java.net.URI;

@EqualsAndHashCode
@ToString
@Data
@Getter
public class MyNotificationParamsEndpointProperty {

    @Setter
    private URI uri;

    @Setter
    private String accept;
}
