package io.github.wistefan.mapping.desc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wistefan.mapping.*;
import io.github.wistefan.mapping.desc.pojos.*;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithSubEntityWellKnown;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithWrongConstructor;
import io.github.wistefan.mapping.desc.pojos.invalid.MySetterThrowingPojo;
import io.github.wistefan.mapping.desc.pojos.invalid.MyThrowingConstructor;
import io.github.wistefan.mapping.desc.pojos.subscription.MyNotificationParamsEndpointProperty;
import io.github.wistefan.mapping.desc.pojos.subscription.MyNotificationParamsProperty;
import io.github.wistefan.mapping.desc.pojos.subscription.MySubscriptionPojo;
import org.fiware.ngsi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntityVOMapperTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private EntityVOMapper entityVOMapper;
	private EntitiesRepository entitiesRepository = mock(EntitiesRepository.class);

	private MappingProperties mappingProperties;

	@BeforeEach
	public void setup() {
		mappingProperties = new MappingProperties();
		entityVOMapper = new EntityVOMapper(mappingProperties, OBJECT_MAPPER, entitiesRepository);
		OBJECT_MAPPER
				.addMixIn(AdditionalPropertyVO.class, AdditionalPropertyMixin.class);
	}

	@DisplayName("Map entity containing a relationship.")
	@Test
	void testSubEntityMapping() throws JsonProcessingException {
		MySubPropertyEntity expectedSubEntity = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:the-sub-entity");
		MyPojoWithSubEntity expectedPojo = new MyPojoWithSubEntity("urn:ngsi-ld:complex-pojo:the-test-pojo");
		expectedPojo.setMySubProperty(expectedSubEntity);


		String subEntityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"sub-entity\",\"name\":{\"type\":\"Property\",\"value\":\"myName\"}}";
		EntityVO subEntity = OBJECT_MAPPER.readValue(subEntityString, EntityVO.class);

		when(entitiesRepository.getEntities(anyList())).thenReturn(Mono.just(List.of(subEntity)));

		String parentEntityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"sub-entity\":{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\",\"datasetId\":\"urn:ngsi-ld:sub-entity:the-sub-entity\"}}";
		EntityVO parentEntity = OBJECT_MAPPER.readValue(parentEntityString, EntityVO.class);

		MyPojoWithSubEntity myPojoWithSubEntity = entityVOMapper.fromEntityVO(parentEntity, MyPojoWithSubEntity.class).block();
		assertEquals(expectedPojo, myPojoWithSubEntity, "The full pojo should be retrieved.");
	}

	@DisplayName("Map an entity with not explicitly mapped properties.")
	@Test
	void testWithUnmappedProperties() throws Exception {
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", "test"));

		MyPojoWithUnmappedProperties expectedPojo = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		expectedPojo.setMyName("my-name");
		expectedPojo.setUnmappedProperties(unmappedProperties);

		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":\"test\",\"type\":\"Property\"},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		EntityVO theEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = entityVOMapper.fromEntityVO(theEntity, MyPojoWithUnmappedProperties.class).block();
		assertEquals(expectedPojo, myPojoWithUnmappedProperties, "The full pojo should be returned.");
	}

	@DisplayName("Map an entity with a not explicitly mapped property list.")
	@Test
	void testWithUnmappedPropertiesList() throws Exception {
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", List.of(1, 2, 3)));

		MyPojoWithUnmappedProperties expectedPojo = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		expectedPojo.setMyName("my-name");
		expectedPojo.setUnmappedProperties(unmappedProperties);

		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":[1,2,3],\"type\":\"Property\"},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		EntityVO theEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = entityVOMapper.fromEntityVO(theEntity, MyPojoWithUnmappedProperties.class).block();
		assertEquals(expectedPojo, myPojoWithUnmappedProperties, "The full pojo should be returned.");
	}

	@DisplayName("Map an entity with multiple not explicitly mapped properties.")
	@Test
	void testWithMultipleUnmappedProperties() throws Exception {
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("other", "property"));
		unmappedProperties.add(new UnmappedProperty("test", List.of(1, 2, 3)));

		MyPojoWithUnmappedProperties expectedPojo = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		expectedPojo.setMyName("my-name");
		expectedPojo.setUnmappedProperties(unmappedProperties);

		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"other\":{\"value\":\"property\",\"type\":\"Property\"},\"test\":{\"value\":[1,2,3],\"type\":\"Property\"},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		EntityVO theEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = entityVOMapper.fromEntityVO(theEntity, MyPojoWithUnmappedProperties.class).block();
		assertEquals(OBJECT_MAPPER.writeValueAsString(expectedPojo), OBJECT_MAPPER.writeValueAsString(myPojoWithUnmappedProperties), "The full pojo should be returned.");
	}

	@DisplayName("Map entity with an unmapped property, containing a relationship.")
	@Test
	void testWithUnmappedRelationship() throws Exception {
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", "test"));
		unmappedProperties.add(new UnmappedProperty("complex", Map.of("something", "other", "id", "urn:ngsi-ld:entity:id")));

		MyPojoWithUnmappedProperties expectedPojo = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		expectedPojo.setMyName("my-name");
		expectedPojo.setUnmappedProperties(unmappedProperties);

		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":\"test\",\"type\":\"Property\"},\"complex\":{\"object\":\"urn:ngsi-ld:entity:id\",\"type\":\"Relationship\",\"something\":{\"value\":\"other\",\"type\":\"Property\"}},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		EntityVO theEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = entityVOMapper.fromEntityVO(theEntity, MyPojoWithUnmappedProperties.class).block();
		assertEquals(expectedPojo, myPojoWithUnmappedProperties, "The full pojo should be returned.");
	}

	@DisplayName("Map entity with a complex unmapped property.")
	@Test
	void testWithComplexUnmappedProperties() throws Exception {
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", "test"));
		unmappedProperties.add(new UnmappedProperty("complex", Map.of("something", "something", "number", 1)));

		MyPojoWithUnmappedProperties expectedPojo = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		expectedPojo.setMyName("my-name");
		expectedPojo.setUnmappedProperties(unmappedProperties);

		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":\"test\",\"type\":\"Property\"},\"complex\":{\"value\":{\"number\":{\"value\":1,\"type\":\"Property\"},\"something\":{\"value\":\"something\",\"type\":\"Property\"}},\"type\":\"Property\",\"number\":{\"value\":1,\"type\":\"Property\"},\"something\":{\"value\":\"something\",\"type\":\"Property\"}},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		EntityVO theEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = entityVOMapper.fromEntityVO(theEntity, MyPojoWithUnmappedProperties.class).block();
		assertEquals(expectedPojo, myPojoWithUnmappedProperties, "The full pojo should be returned.");
	}

	@DisplayName("Map entity with a deep unmapped property.")
	@Test
	void testWithDeepUnmappedProperties() throws Exception {
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", "test"));
		unmappedProperties.add(new UnmappedProperty("complex", Map.of("number", 1, "deep", Map.of("something", "deep"))));

		MyPojoWithUnmappedProperties expectedPojo = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		expectedPojo.setMyName("my-name");
		expectedPojo.setUnmappedProperties(unmappedProperties);

		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":\"test\",\"type\":\"Property\"},\"complex\":{\"value\":{\"number\":{\"value\":1,\"type\":\"Property\"},\"deep\":{\"value\":{\"something\":{\"value\":\"deep\",\"type\":\"Property\"}},\"type\":\"Property\",\"something\":{\"value\":\"deep\",\"type\":\"Property\"}}},\"type\":\"Property\",\"number\":{\"value\":1,\"type\":\"Property\"},\"deep\":{\"value\":{\"something\":{\"value\":\"deep\",\"type\":\"Property\"}},\"type\":\"Property\",\"something\":{\"value\":\"deep\",\"type\":\"Property\"}}},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		EntityVO theEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = entityVOMapper.fromEntityVO(theEntity, MyPojoWithUnmappedProperties.class).block();
		assertEquals(expectedPojo, myPojoWithUnmappedProperties, "The full pojo should be returned.");
	}

	@DisplayName("Map Pojo with a field that is an object.")
	@Test
	void testSubPropertyMapping() throws JsonProcessingException {
		MyPojoWithSubProperty expectedPojo = new MyPojoWithSubProperty("urn:ngsi-ld:complex-pojo:the-test-pojo");
		MySubProperty mySubProperty = new MySubProperty();
		mySubProperty.setPropertyName("My property");
		expectedPojo.setMySubProperty(mySubProperty);

		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"mySubProperty\":{\"value\":{\"propertyName\":\"My property\"},\"type\":\"Property\",\"propertyName\":{\"value\":\"My property\",\"type\":\"Property\"}}}";
		EntityVO theEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);

		MyPojoWithSubProperty myPojoWithSubProperty = entityVOMapper.fromEntityVO(theEntity, MyPojoWithSubProperty.class).block();
		assertEquals(expectedPojo, myPojoWithSubProperty, "The full pojo should be returned.");
	}

	@DisplayName("Map Pojo with a field that is a list of objects.")
	@Test
	void testListOfSubPropertyMapping() throws JsonProcessingException {
		MyPojoWithListOfSubProperty expectedPojo = new MyPojoWithListOfSubProperty(
				"urn:ngsi-ld:complex-pojo:the-test-pojo");
		MySubProperty mySubProperty1 = new MySubProperty();
		mySubProperty1.setPropertyName("My property 1");
		MySubProperty mySubProperty2 = new MySubProperty();
		mySubProperty2.setPropertyName("My property 2");
		expectedPojo.setMySubProperties(List.of(mySubProperty1, mySubProperty2));

		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"mySubProperty\":[{\"value\":{\"propertyName\":\"My property 1\"},\"type\":\"Property\"},{\"value\":{\"propertyName\":\"My property 2\"},\"type\":\"Property\"}]}";
		EntityVO theEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);

		MyPojoWithListOfSubProperty myPojoWithListOfSubProperty = entityVOMapper.fromEntityVO(theEntity, MyPojoWithListOfSubProperty.class).block();
		assertEquals(expectedPojo, myPojoWithListOfSubProperty, "The full pojo should be returned.");
	}





	@DisplayName("Map entity containing a relationship that could not be resolved with strict-mapping disabled.")
	@Test
	void testSubEntityMappingNoStrict() throws JsonProcessingException {
		mappingProperties.setStrictRelationships(false);
		MySubPropertyEntity expectedSubEntity = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:the-sub-entity");
		MyPojoWithSubEntity expectedPojo = new MyPojoWithSubEntity("urn:ngsi-ld:complex-pojo:the-test-pojo");
		expectedPojo.setMySubProperty(expectedSubEntity);

		when(entitiesRepository.getEntities(anyList())).thenReturn(Mono.just(List.of()));

		String parentEntityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"sub-entity\":{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\",\"datasetId\":\"urn:ngsi-ld:sub-entity:the-sub-entity\"}}";
		EntityVO parentEntity = OBJECT_MAPPER.readValue(parentEntityString, EntityVO.class);

		MyPojoWithSubEntity myPojoWithSubEntity = entityVOMapper.fromEntityVO(parentEntity, MyPojoWithSubEntity.class).block();
		assertEquals(expectedPojo, myPojoWithSubEntity, "The full pojo should be retrieved.");
	}

	@DisplayName("Fail entity containing a relationship that could not be resolved with strict-mapping enabled.")
	@Test
	void testSubEntityMappingStrict() throws JsonProcessingException {
		mappingProperties.setStrictRelationships(true);
		when(entitiesRepository.getEntities(anyList())).thenReturn(Mono.just(List.of()));

		String parentEntityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"sub-entity\":{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\",\"datasetId\":\"urn:ngsi-ld:sub-entity:the-sub-entity\"}}";
		EntityVO parentEntity = OBJECT_MAPPER.readValue(parentEntityString, EntityVO.class);

		assertThrows(MappingException.class, () -> entityVOMapper.fromEntityVO(parentEntity, MyPojoWithSubEntity.class).block(), "For strict-mapping, an exception should be thrown.");
	}


	@DisplayName("Map entity containing a relationship with embedded values.")
	@Test
	void testSubEntityEmbedMapping() throws JsonProcessingException {
		MySubPropertyEntityEmbed expectedSubEntity = new MySubPropertyEntityEmbed("urn:ngsi-ld:sub-entity:the-sub-entity");
		MyPojoWithSubEntityEmbed expectedPojo = new MyPojoWithSubEntityEmbed("urn:ngsi-ld:complex-pojo:the-test-pojo");
		expectedPojo.setMySubProperty(expectedSubEntity);

		String subEntityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"sub-entity\",\"name\":{\"type\":\"Property\",\"value\":\"myName\"}}";
		EntityVO subEntity = OBJECT_MAPPER.readValue(subEntityString, EntityVO.class);

		when(entitiesRepository.getEntities(anyList())).thenReturn(Mono.just(List.of(subEntity)));

		String parentEntityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"sub-entity\":{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\",\"datasetId\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"role\":{\"type\":\"Property\",\"value\":\"Sub-Entity\"}}}";
		EntityVO parentEntity = OBJECT_MAPPER.readValue(parentEntityString, EntityVO.class);

		MyPojoWithSubEntityEmbed myPojoWithSubEntityEmbed = entityVOMapper.fromEntityVO(parentEntity, MyPojoWithSubEntityEmbed.class).block();
		assertEquals(expectedPojo, myPojoWithSubEntityEmbed, "The full pojo should be retrieved.");
	}

	@DisplayName("Map entity with all supported attribute types.")
	@Test
	void testListEntityMapping() throws JsonProcessingException {
		PropertyListPojo propertyListPojo = new PropertyListPojo("urn:ngsi-ld:list-pojo:the-pojo");

		MySubPropertyEntity subEntity1 = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:the-sub-entity-1");
		MySubPropertyEntity subEntity2 = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:the-sub-entity-2");

		MySubProperty property1 = new MySubProperty();
		property1.setPropertyName("p-1");
		MySubProperty property2 = new MySubProperty();
		property2.setPropertyName("p-2");

		propertyListPojo.setProperty(property1);
		propertyListPojo.setRelationShip(subEntity1);
		propertyListPojo.setPropertyList(List.of(property1, property2));
		propertyListPojo.setRelationshipList(List.of(subEntity1, subEntity2));

		String subEntity1String = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:sub-entity:the-sub-entity-1\",\"type\":\"sub-entity\",\"name\":{\"type\":\"Property\",\"value\":\"myName\"}}";
		String subEntity2String = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:sub-entity:the-sub-entity-2\",\"type\":\"sub-entity\",\"name\":{\"type\":\"Property\",\"value\":\"myName\"}}";

		EntityVO parsedSubEntity1 = OBJECT_MAPPER.readValue(subEntity1String, EntityVO.class);
		EntityVO parsedSubEntity2 = OBJECT_MAPPER.readValue(subEntity2String, EntityVO.class);

		when(entitiesRepository.getEntities(anyList())).thenReturn(Mono.just(List.of(parsedSubEntity1, parsedSubEntity2)));

		String parentEntityString = "{\n" +
				"\t\"@context\": \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\n" +
				"\t\"id\": \"urn:ngsi-ld:list-pojo:the-pojo\",\n" +
				"\t\"type\": \"list-pojo\",\n" +
				"\t\"mySubProperty\": {\n" +
				"\t  \"value\": {\n" +
				"\t\t\"propertyName\": \"p-1\"\n" +
				"\t  },\n" +
				"\t  \"type\": \"Property\"\n" +
				"\t},\n" +
				"\t\"myRelationship\": {\n" +
				"\t\t\"object\": \"urn:ngsi-ld:sub-entity:the-sub-entity-1\",\n" +
				"\t\t\"type\": \"Relationship\",\n" +
				"\t\t\"datasetId\": \"urn:ngsi-ld:sub-entity:the-sub-entity-1\"\n" +
				"\t},\n" +
				"\t\"mySubPropertyList\": [\n" +
				"\t \t{\n" +
				"\t\t \"value\": {\n" +
				"\t\t\t\"propertyName\": \"p-1\"\n" +
				"\t\t  },\n" +
				"\t  \t\"type\": \"Property\"\n" +
				"\t\t}, \n" +
				"\t  \t{\n" +
				"\t\t  \"value\": {\n" +
				"\t\t\t\"propertyName\": \"p-2\"\n" +
				"\t\t  },\n" +
				"\t\t  \"type\": \"Property\"\n" +
				"\t\t}\n" +
				"\t],\n" +
				"\t\"myRelationshipList\": [\n" +
				"\t \t{\n" +
				"\t\t  \"object\": \"urn:ngsi-ld:sub-entity:the-sub-entity-1\",\n" +
				"\t\t  \"type\": \"Relationship\",\n" +
				"\t\t  \"datasetId\": \"urn:ngsi-ld:sub-entity:the-sub-entity-1\"\n" +
				"\t\t}, \n" +
				"\t  \t{\n" +
				"\t\t  \"object\": \"urn:ngsi-ld:sub-entity:the-sub-entity-2\",\n" +
				"\t\t  \"type\": \"Relationship\",\n" +
				"\t\t  \"datasetId\": \"urn:ngsi-ld:sub-entity:the-sub-entity-2\"\n" +
				"\t\t}\n" +
				"\t]\n" +
				"}";
		EntityVO parentEntity = OBJECT_MAPPER.readValue(parentEntityString, EntityVO.class);

		PropertyListPojo mappedPojo = entityVOMapper.fromEntityVO(parentEntity, PropertyListPojo.class).block();
		assertEquals(propertyListPojo, mappedPojo, "The full pojo should be retrieved.");
	}

	@DisplayName("Only mapping to classes with mapping enabled is supported.")
	@Test
	void failWithoutMappingEnabled() {
		assertThrows(MappingException.class, () -> entityVOMapper.fromEntityVO(new EntityVO(), Object.class).block(), "Only mapping to classes with mapping enabled is supported.");
	}

	@DisplayName("Only mapping to matching classes is supported.")
	@Test
	void failWithoutMatchingClass() {
		EntityVO myEntity = new EntityVO().type("my-type");
		assertThrows(MappingException.class, () -> entityVOMapper.fromEntityVO(myEntity, MyPojo.class).block(), "Only mapping to matching classes is supported.");
	}

	@DisplayName("The target classes should provide a string constructor.")
	@Test
	void failWithoutWrongConstructor() {
		when(entitiesRepository.getEntities(anyList())).thenReturn(Mono.just(List.of()));

		EntityVO myEntity = new EntityVO().type("my-pojo").id(URI.create("urn:ngsi-ld:pojo:pojo"));
		assertThrows(MappingException.class, () -> entityVOMapper.fromEntityVO(myEntity, MyPojoWithWrongConstructor.class).block(), "The target classes should provide a string constructor.");
	}

	@DisplayName("Unmapped properties should be ignored.")
	@Test
	void ignoreUnmappedProperties() {
		MySubPropertyEntity mySubPropertyEntity = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:entity");
		mySubPropertyEntity.setMyName("non-ignore");
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngsi-ld:sub-entity:entity")).type("sub-entity");
		entityVO.setAdditionalProperties("non-prop", new PropertyVO().value("ignore"));
		entityVO.setAdditionalProperties("name", new PropertyVO().value("non-ignore"));
		assertEquals(mySubPropertyEntity, entityVOMapper.fromEntityVO(entityVO, MySubPropertyEntity.class).block(), "The non-prop should be ignored.");
	}

	@DisplayName("If the constructor is broken, nothing should be mapped.")
	@Test
	void failOnBrokenConstructor() {
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngsi-ld:throwing-pojo:id")).type("throwing-pojo");
		assertThrows(MappingException.class, () -> entityVOMapper.fromEntityVO(entityVO, MyThrowingConstructor.class).block(), "If the constructor is broken, nothing should be mapped.");
	}

	@DisplayName("The relationship target should have been created from its properties.")
	@Test
	void mapFromProperties() {
		EntityVO parentEntity = new EntityVO().id(URI.create("urn:ngsi-ld:complex-pojo:entity")).type("complex-pojo");
		EntityVO subEntity = new EntityVO().id(URI.create("urn:ngsi-ld:sub-entity:entity")).type("sub-entity");
		RelationshipVO subRel = new RelationshipVO()._object(subEntity.getId());
		subRel.setAdditionalProperties("name", new PropertyVO().value("my-other-name"));
		parentEntity.setAdditionalProperties("mySubProperty", subRel);

		MySubPropertyEntity expectedSub = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:entity");
		expectedSub.setMyName("my-other-name");
		MyPojoWithSubEntityFrom expectedPojo = new MyPojoWithSubEntityFrom("urn:ngsi-ld:complex-pojo:entity");
		expectedPojo.setMySubProperty(expectedSub);

		assertEquals(expectedPojo, entityVOMapper.fromEntityVO(parentEntity, MyPojoWithSubEntityFrom.class).block(), "The relationship target should have been created from its properties.");
	}


	@DisplayName("The relationship targets should have been created from its properties.")
	@Test
	void mapListFromProperties() {
		EntityVO parentEntity = new EntityVO().id(URI.create("urn:ngsi-ld:complex-pojo:entity")).type("complex-pojo");
		EntityVO subEntity1 = new EntityVO().id(URI.create("urn:ngsi-ld:sub-entity:entity-1")).type("sub-entity");
		EntityVO subEntity2 = new EntityVO().id(URI.create("urn:ngsi-ld:sub-entity:entity-2")).type("sub-entity");
		RelationshipVO subRel1 = new RelationshipVO()._object(subEntity1.getId());
		RelationshipVO subRel2 = new RelationshipVO()._object(subEntity2.getId());

		subRel1.setAdditionalProperties("name", new PropertyVO().value("sub-entity-1"));
		subRel2.setAdditionalProperties("name", new PropertyVO().value("sub-entity-2"));
		RelationshipListVO relationshipVOS = new RelationshipListVO();
		relationshipVOS.add(subRel1);
		relationshipVOS.add(subRel2);
		parentEntity.setAdditionalProperties("mySubProperty", relationshipVOS);

		MySubPropertyEntity expectedSub1 = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:entity-1");
		expectedSub1.setMyName("sub-entity-1");
		MySubPropertyEntity expectedSub2 = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:entity-2");
		expectedSub2.setMyName("sub-entity-2");
		MyPojoWithSubEntityListFrom expectedPojo = new MyPojoWithSubEntityListFrom("urn:ngsi-ld:complex-pojo:entity");
		expectedPojo.setMySubProperty(List.of(expectedSub1, expectedSub2));

		assertEquals(expectedPojo, entityVOMapper.fromEntityVO(parentEntity, MyPojoWithSubEntityListFrom.class).block(), "The relationship targets should have been created from its properties.");
	}

	@DisplayName("If the setter is broken, nothing should be constructed.")
	@Test
	void failWithThrowingSetter() {
		EntityVO entity = new EntityVO().id(URI.create("urn:ngsi-ld:my-pojo:entity")).type("my-pojo");
		assertThrows(MappingException.class, () -> entityVOMapper.fromEntityVO(entity, MySetterThrowingPojo.class).block(), "If the setter is broken, nothing should be constructed.");
	}

	@DisplayName("Well known properties should properly be mapped.")
	@Test
	void mapWithWellKnown() {
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngsi-ld:complex-pojo:entity")).type("complex-pojo");
		EntityVO subEntity = new EntityVO().id(URI.create("urn:ngsi-ld:sub-entity:entity")).type("sub-entity");
		when(entitiesRepository.getEntities(anyList())).thenReturn(Mono.just(List.of(subEntity)));

		RelationshipVO subRel = new RelationshipVO()
				._object(subEntity.getId())
				.observedAt(Instant.MAX)
				.createdAt(Instant.MAX)
				.modifiedAt(Instant.MAX)
				.datasetId(subEntity.getId())
				.instanceId(URI.create("id"));
		entityVO.setAdditionalProperties("mySubProperty", subRel);

		MySubPropertyEntityWithWellKnown mySubPropertyEntityWithWellKnown = new MySubPropertyEntityWithWellKnown("urn:ngsi-ld:sub-entity:entity");
		mySubPropertyEntityWithWellKnown.setDatasetId("urn:ngsi-ld:sub-entity:entity");
		mySubPropertyEntityWithWellKnown.setInstanceId("id");
		mySubPropertyEntityWithWellKnown.setCreatedAt(Instant.MAX);
		mySubPropertyEntityWithWellKnown.setModifiedAt(Instant.MAX);
		mySubPropertyEntityWithWellKnown.setObservedAt(Instant.MAX);

		MyPojoWithSubEntityWellKnown myPojoWithSubEntityWellKnown = new MyPojoWithSubEntityWellKnown("urn:ngsi-ld:complex-pojo:entity");
		myPojoWithSubEntityWellKnown.setMySubProperty(mySubPropertyEntityWithWellKnown);

		assertEquals(myPojoWithSubEntityWellKnown, entityVOMapper.fromEntityVO(entityVO, MyPojoWithSubEntityWellKnown.class).block(), "Well known properties should properly be mapped.");
	}

	@Test
	public void testConvertEntityToMap() {
		MySimplePojo pojo = new MySimplePojo();
		pojo.setMyName("Some");
		pojo.setNumbers(List.of());

		assertEquals(
				Map.ofEntries(
						Map.entry("myName", pojo.getMyName()),
						Map.entry("numbers", pojo.getNumbers())
				),
				entityVOMapper.convertEntityToMap(pojo));
	}

	@Test
	void testReadingNotificationFromJson() throws JsonProcessingException {
		String json = """
				{
				  "id": "urn:ngsi-ld:Notification:4233e3ca-50c3-11ee-8433-0a580a826912",
				  "type": "Notification",
				  "subscriptionId": "urn:ngsi-ld:subscription:567f4788-50bf-11ee-94e9-0a580a826911",
				  "notifiedAt": "2023-09-11T16:50:05.456Z",
				  "data": [
				    {
				      "id": "urn:ngsi-ld:product:4d0964a4-2341-4676-a551-de5115ccf98d",
				      "type": "product",
				      "deletedAt": "2023-09-11T16:50:05.456Z"
				    }
				  ]
				}""";
		NotificationVO notificationVO = entityVOMapper.readNotificationFromJSON(json);

		assertNotNull(notificationVO);
		assertEquals("Notification", notificationVO.getType());
	}

	@DisplayName("Query mapping")
	@Test
	void testQueryMapping() {
		MySubscriptionPojo myPojo = createSubscription();

		assertEquals(myPojo.getQ(), entityVOMapper.toSubscriptionVO(myPojo).getQ(),
				"The pojo should have the same query");
	}

	@DisplayName("Notification endpoint mapping")
	@Test
	void testNotificationEndpointMapping() {
		MySubscriptionPojo myPojo = createSubscription();

		assertEquals(myPojo.getNotification().getEndpoint().getUri(), entityVOMapper.toSubscriptionVO(myPojo).getNotification().getEndpoint().getUri(),
				"The pojo should have the same notification endpoint");
	}

	@DisplayName("Map entity with duplicate relationship")
	@Test
	void testDuplicateRelationship() throws Exception {
		MySubPropertyEntity expectedSubEntity = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:the-sub-entity");
		MyPojoWithSubEntityList expectedPojo = new MyPojoWithSubEntityList("urn:ngsi-ld:complex-pojo:the-test-pojo");
		expectedPojo.setMySubPropertyList(List.of(expectedSubEntity, expectedSubEntity));

		String subEntityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"sub-entity\",\"name\":{\"type\":\"Property\",\"value\":\"myName\"}}";
		EntityVO subEntity = OBJECT_MAPPER.readValue(subEntityString, EntityVO.class);

		when(entitiesRepository.getEntities(anyList())).thenReturn(Mono.just(List.of(subEntity, subEntity)));

		String parentEntityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"sub-entity-list\":[{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\",\"datasetId\":\"urn:ngsi-ld:sub-entity:the-sub-entity\"},{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\",\"datasetId\":\"urn:ngsi-ld:sub-entity:the-sub-entity\"}]}";
		EntityVO parentEntity = OBJECT_MAPPER.readValue(parentEntityString, EntityVO.class);

		MyPojoWithSubEntityList myPojoWithSubEntity = entityVOMapper.fromEntityVO(parentEntity, MyPojoWithSubEntityList.class).block();
		assertEquals(expectedPojo, myPojoWithSubEntity, "The full pojo should be retrieved.");
	}

	@DisplayName("*****Test mapping geo entities*****")
	@Test
	void testMappingGeoEntities() throws JsonProcessingException {
		MyPojoWithLocation expectedPojoWithLocation = new MyPojoWithLocation("urn:ngsi-ld:complex-pojo:the-test-pojo");
		MyLocation location = new MyLocation();
		double[] coordinates = new double[]{0.0, 0.0};
		location.setCoordinates(coordinates);
		expectedPojoWithLocation.setMyLocation(location);
		String entityString = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"location-pojo\",\"myLocation\": { \"type\": \"GeoProperty\",    \"value\": {      \"type\": \"Point\",      \"coordinates\": [0,0]    }  }}";
		EntityVO parsedEntity = OBJECT_MAPPER.readValue(entityString, EntityVO.class);
		//GeoPropertyVO
		MyPojoWithLocation myPojoWithLocation = entityVOMapper.fromEntityVO(parsedEntity, MyPojoWithLocation.class).block();
		assertEquals(expectedPojoWithLocation.getId(), myPojoWithLocation.getId(), "GeoEntities can be mapped to their Java Objects");
	}

	private MySubscriptionPojo createSubscription() {
		MySubscriptionPojo myPojo = new MySubscriptionPojo("urn:ngsi-ld:my-pojo:the-test-pojo");
		myPojo.setQ("eventType=custom");
		myPojo.setNotification(createNotification());

		return myPojo;
	}

	private MyNotificationParamsEndpointProperty createEndpoint() {
		MyNotificationParamsEndpointProperty endpointProperty = new MyNotificationParamsEndpointProperty();
		endpointProperty.setUri(URI.create("test.com"));
		endpointProperty.setAccept("application/ld+json");
		return endpointProperty;
	}

	private MyNotificationParamsProperty createNotification() {
		MyNotificationParamsProperty notificationParamsProperty = new MyNotificationParamsProperty();
		notificationParamsProperty.setEndpoint(createEndpoint());
		notificationParamsProperty.setFormat("keyValues");
		return notificationParamsProperty;
	}
}