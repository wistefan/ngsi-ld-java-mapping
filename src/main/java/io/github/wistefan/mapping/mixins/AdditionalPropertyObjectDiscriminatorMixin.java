package io.github.wistefan.mapping.mixins;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Class type info for cache serialization and deserialization
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class AdditionalPropertyObjectDiscriminatorMixin {}
