package io.github.wistefan.mapping.desc.pojos.subscription;

import lombok.*;

@EqualsAndHashCode
@ToString
@Data
@Getter
public class MyEntityInfoProperty {

    @Setter
    private Object id;

    @Setter
    private String type;

    @Setter
    private String idPattern;
}
