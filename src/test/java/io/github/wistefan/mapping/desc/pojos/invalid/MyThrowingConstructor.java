package io.github.wistefan.mapping.desc.pojos.invalid;

import io.github.wistefan.mapping.annotations.MappingEnabled;

@MappingEnabled(entityType = "throwing-pojo")
public class MyThrowingConstructor {

    public MyThrowingConstructor(String id) {
        throw new RuntimeException("Something is really wrong.");
    }
}
