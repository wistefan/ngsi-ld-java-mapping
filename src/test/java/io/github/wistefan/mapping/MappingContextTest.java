package io.github.wistefan.mapping;

import tools.jackson.core.TokenStreamFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Singleton;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.ngsi.model.GeoQueryVO;
import org.fiware.ngsi.model.PropertyListVO;
import org.fiware.ngsi.model.PropertyVO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Guards the DI wiring of the mapping beans and the Jackson customisations applied to the context ObjectMapper.
 */
class MappingContextTest {

	private static final String NGSI_URL_PROPERTY = "micronaut.http.services.ngsi.url";
	private static final String DUMMY_NGSI_URL = "http://localhost:1026";
	private static final String CANONICALIZE_FEATURE_PROPERTY = "jackson.json-factory-features.canonicalize-property-names";

	private static final String ESCAPED_KEY = "tmfEscaped-@context";
	private static final String UNESCAPED_KEY = "@context";
	private static final String CONTEXT_VALUE = "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld";
	private static final String ESCAPED_ENTITY = "{\"id\":\"urn:ngsi-ld:test:escaped\",\"type\":\"test\",\""
			+ ESCAPED_KEY + "\":\"" + CONTEXT_VALUE + "\"}";

	private static final String GEO_QUERY = "{\"georel\":\"near;maxDistance==2000\",\"geometry\":\"Point\",\"coordinates\":[8.68,49.4]}";
	private static final String ENTITY_WITH_PROPERTY_LIST = "{\"id\":\"urn:ngsi-ld:product-offering:the-offering\","
			+ "\"type\":\"product-offering\","
			+ "\"description\":{\"type\":\"Property\",\"value\":\"\"},"
			+ "\"listProp\":[{\"type\":\"Property\",\"value\":\"A\"},{\"type\":\"Property\",\"value\":\"B\"}]}";

	private static ApplicationContext applicationContext;
	private static ObjectMapper objectMapper;

	@BeforeAll
	static void startContext() {
		applicationContext = ApplicationContext.run(Map.of(NGSI_URL_PROPERTY, DUMMY_NGSI_URL));
		objectMapper = applicationContext.getBean(ObjectMapper.class);
	}

	@AfterAll
	static void stopContext() {
		applicationContext.close();
	}

	@DisplayName("Mapping beans are resolvable from the context.")
	@ParameterizedTest
	@ValueSource(classes = {EntityVOMapper.class, JavaObjectMapper.class, EntityVOSerializer.class, CacheSerdeableObjectMapper.class})
	void beansAreResolvable(Class<?> beanType) {
		assertNotNull(applicationContext.getBean(beanType), "The bean should be resolvable.");
	}

	@DisplayName("The context ObjectMapper uses the escape cleaning factory.")
	@Test
	void usesEscapeCleaningFactory() {
		assertInstanceOf(EscapeCleaningJsonFactory.class, objectMapper.tokenStreamFactory(), "The escape cleaning factory should be used.");
	}

	@DisplayName("Configured json-factory-features are applied to the escape cleaning factory.")
	@Test
	void appliesJsonFactoryFeatures() {
		try (ApplicationContext featureContext = ApplicationContext.run(
				Map.of(NGSI_URL_PROPERTY, DUMMY_NGSI_URL, CANONICALIZE_FEATURE_PROPERTY, false))) {
			TokenStreamFactory factory = featureContext.getBean(ObjectMapper.class).tokenStreamFactory();
			assertInstanceOf(EscapeCleaningJsonFactory.class, factory, "The escape cleaning factory should be used.");
			assertFalse(factory.isEnabled(TokenStreamFactory.Feature.CANONICALIZE_PROPERTY_NAMES), "The configured feature should be disabled.");
		}
	}

	@DisplayName("Escaped reserved keys are unescaped during parsing.")
	@ParameterizedTest(name = "{0}")
	@MethodSource("escapedKeyCases")
	void unescapesReservedKeys(String description, EscapedValueReader reader) throws Exception {
		assertEquals(CONTEXT_VALUE, reader.read(objectMapper), "The escaped key should be unescaped for " + description);
	}

	private static Stream<Arguments> escapedKeyCases() {
		return Stream.of(
				Arguments.of("tree", (EscapedValueReader) mapper -> mapper.readTree(ESCAPED_ENTITY).get(UNESCAPED_KEY).asText()),
				Arguments.of("map", (EscapedValueReader) mapper -> mapper.readValue(ESCAPED_ENTITY, new TypeReference<Map<String, Object>>() {
				}).get(UNESCAPED_KEY)),
				Arguments.of("bean property", (EscapedValueReader) mapper -> mapper.readValue(ESCAPED_ENTITY, EntityVO.class).getAtContext()));
	}

	@DisplayName("GeoQueries are deserialized through the GeoQueryDeserializer.")
	@Test
	void deserializesGeoQuery() throws Exception {
		assertEquals(new GeoQueryVO(), objectMapper.readValue(GEO_QUERY, GeoQueryVO.class), "An empty GeoQuery should be returned.");
	}

	@DisplayName("Additional properties are resolved through the AdditionalPropertyMixin.")
	@Test
	void appliesAdditionalPropertyMixin() throws Exception {
		EntityVO entityVO = objectMapper.readValue(ENTITY_WITH_PROPERTY_LIST, EntityVO.class);
		assertInstanceOf(PropertyVO.class, entityVO.getAdditionalProperties().get("description"), "A single property should be deserialized.");
		assertInstanceOf(PropertyListVO.class, entityVO.getAdditionalProperties().get("listProp"), "A property list should be deserialized.");
	}

	@FunctionalInterface
	private interface EscapedValueReader {
		Object read(ObjectMapper mapper) throws Exception;
	}

	/**
	 * The library ships no EntitiesRepository implementation; consumers provide one.
	 */
	@Singleton
	static class TestEntitiesRepository implements EntitiesRepository {

		@Override
		public Mono<List<EntityVO>> getEntities(List<URI> entityIds) {
			return Mono.just(List.of());
		}
	}
}
