package io.github.wistefan.mapping;

import io.github.wistefan.mapping.mixins.AdditionalPropertyDiscriminatorMixin;
import io.github.wistefan.mapping.mixins.AdditionalPropertyListDiscriminatorMixin;
import io.github.wistefan.mapping.mixins.AdditionalPropertyObjectDiscriminatorMixin;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Singleton;
import org.fiware.ngsi.model.AdditionalPropertyListVO;
import org.fiware.ngsi.model.AdditionalPropertyObjectVO;
import org.fiware.ngsi.model.AdditionalPropertyVO;
import tools.jackson.databind.json.JsonMapper;

@Singleton
@Bean
public class CacheSerdeableObjectMapper extends JsonMapper {
    public CacheSerdeableObjectMapper() {
        super(JsonMapper.builder()
                .addMixIn(AdditionalPropertyVO.class, AdditionalPropertyDiscriminatorMixin.class)
                .addMixIn(AdditionalPropertyObjectVO.class, AdditionalPropertyObjectDiscriminatorMixin.class)
                .addMixIn(AdditionalPropertyListVO.class, AdditionalPropertyListDiscriminatorMixin.class)
                .findAndAddModules());
    }
}
