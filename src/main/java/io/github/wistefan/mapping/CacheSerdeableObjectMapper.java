package io.github.wistefan.mapping;

import io.github.wistefan.mapping.mixins.AdditionalPropertyDiscriminatorMixin;
import io.github.wistefan.mapping.mixins.AdditionalPropertyListDiscriminatorMixin;
import io.github.wistefan.mapping.mixins.AdditionalPropertyObjectDiscriminatorMixin;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Singleton;
import org.fiware.ngsi.model.AdditionalPropertyListVO;
import org.fiware.ngsi.model.AdditionalPropertyObjectVO;
import org.fiware.ngsi.model.AdditionalPropertyVO;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Singleton
@Bean
public class CacheSerdeableObjectMapper extends JsonMapper {
    public CacheSerdeableObjectMapper() {
        super(JsonMapper.builder()
                .addMixIn(AdditionalPropertyVO.class, AdditionalPropertyDiscriminatorMixin.class)
                .addMixIn(AdditionalPropertyObjectVO.class, AdditionalPropertyObjectDiscriminatorMixin.class)
                .addMixIn(AdditionalPropertyListVO.class, AdditionalPropertyListDiscriminatorMixin.class)
                // keep the Jackson 2 defaults, so cached entries look like the ones written by 1.x
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndAddModules());
    }
}
