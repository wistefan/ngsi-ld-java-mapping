package io.github.wistefan.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import io.github.wistefan.mapping.mixins.AdditionalPropertyDiscriminatorMixin;
import io.github.wistefan.mapping.mixins.AdditionalPropertyListDiscriminatorMixin;
import io.github.wistefan.mapping.mixins.AdditionalPropertyObjectDiscriminatorMixin;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Singleton;
import org.fiware.ngsi.model.AdditionalPropertyListVO;
import org.fiware.ngsi.model.AdditionalPropertyObjectVO;
import org.fiware.ngsi.model.AdditionalPropertyVO;
import org.fiware.ngsi.model.EntityVO;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Singleton
@Bean
public class CacheSerdeableObjectMapper extends ObjectMapper {
    public CacheSerdeableObjectMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(EntityVO.class)
                .allowIfBaseType(AdditionalPropertyVO.class)
                .allowIfSubType(List.class)
                .allowIfSubType(Instant.class)
                .allowIfSubType(URI.class)
                .allowIfSubType("io.github.wistefan.mapping.desc.pojos")
                .allowIfSubType("org.fiware.tmforum.common.domain")
                .build();
        setPolymorphicTypeValidator(ptv)
                .activateDefaultTyping(ptv, DefaultTyping.EVERYTHING);
        addMixIn(AdditionalPropertyVO.class, AdditionalPropertyDiscriminatorMixin.class);
        addMixIn(AdditionalPropertyObjectVO.class, AdditionalPropertyObjectDiscriminatorMixin.class);
        addMixIn(AdditionalPropertyListVO.class, AdditionalPropertyListDiscriminatorMixin.class);
        findAndRegisterModules();
    }
}
