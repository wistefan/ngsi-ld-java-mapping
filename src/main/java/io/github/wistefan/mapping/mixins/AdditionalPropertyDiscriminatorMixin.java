package io.github.wistefan.mapping.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.fiware.ngsi.model.PropertyTypeVO;

/**
 * Class type info for cache serialization and deserialization
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class AdditionalPropertyDiscriminatorMixin {
    @JsonIgnore abstract PropertyTypeVO getType();
}
