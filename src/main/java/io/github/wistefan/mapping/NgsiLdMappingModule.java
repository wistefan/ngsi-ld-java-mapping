package io.github.wistefan.mapping;

import tools.jackson.databind.module.SimpleModule;
import jakarta.inject.Singleton;
import org.fiware.ngsi.model.AdditionalPropertyVO;
import org.fiware.ngsi.model.GeoQueryVO;

/**
 * Jackson customisations required to (de)serialize NGSI-LD VOs. Picked up by Micronaut's ObjectMapperFactory;
 * hand-built mappers have to register it themselves.
 */
@Singleton
public class NgsiLdMappingModule extends SimpleModule {

	private static final String MODULE_NAME = "NgsiLdMappingModule";

	public NgsiLdMappingModule() {
		super(MODULE_NAME);
		setMixInAnnotation(AdditionalPropertyVO.class, AdditionalPropertyMixin.class);
		addDeserializer(GeoQueryVO.class, new GeoQueryDeserializer());
	}
}
