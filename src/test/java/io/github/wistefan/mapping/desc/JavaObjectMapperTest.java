package io.github.wistefan.mapping.desc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wistefan.mapping.*;
import io.github.wistefan.mapping.desc.pojos.*;
import io.github.wistefan.mapping.desc.pojos.invalid.MyInvalidListRelationshipPojo;
import io.github.wistefan.mapping.desc.pojos.invalid.MyInvalidRelationshipPojo;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithInvalidSubEntity;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithMultipleIds;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithMultipleTypes;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithPrivateId;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithPrivateType;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithWrongIdType;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithWrongTypeType;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithoutId;
import io.github.wistefan.mapping.desc.pojos.invalid.MyPojoWithoutType;
import io.github.wistefan.mapping.desc.pojos.invalid.MySubEntityWithNonURIDatasetId;
import io.github.wistefan.mapping.desc.pojos.invalid.MySubEntityWithNonURIRelObject;
import io.github.wistefan.mapping.desc.pojos.invalid.MySubEntityWithoutRelationshipObject;
import io.github.wistefan.mapping.desc.pojos.invalid.MyThrowingPojo;
import org.fiware.ngsi.model.AdditionalPropertyVO;
import org.fiware.ngsi.model.EntityVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class JavaObjectMapperTest {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private JavaObjectMapper javaObjectMapper;

	@BeforeEach
	public void setup() {
		javaObjectMapper = new JavaObjectMapper(new MappingProperties());
		OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		OBJECT_MAPPER
				.addMixIn(AdditionalPropertyVO.class, AdditionalPropertyMixin.class);
	}

	@DisplayName("Simple pojo mapping.")
	@Test
	void testSimplePojoMapping() throws JsonProcessingException {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-test-pojo\",\"type\":\"my-pojo\",\"name\":{\"type\":\"Property\",\"value\":\"The test pojo.\"},\"numbers\":{\"type\":\"Property\",\"value\":[1,2,3]}}";

		MyPojo myPojo = new MyPojo("urn:ngsi-ld:my-pojo:the-test-pojo");
		myPojo.setNumbers(List.of(1, 2, 3));
		myPojo.setMyName("The test pojo.");

		EntityVO expectedEntity = OBJECT_MAPPER.readValue(expectedJson, EntityVO.class);
		expectedEntity.setLocation(null);
		expectedEntity.setOperationSpace(null);
		expectedEntity.setObservationSpace(null);

		assertEquals(expectedEntity, javaObjectMapper.toEntityVO(myPojo),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map Pojo with a field that is an object.")
	@Test
	void testSubPropertyMapping() throws JsonProcessingException {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"mySubProperty\":{\"value\":{\"propertyName\":\"My property\"},\"type\":\"Property\"}}";

		MyPojoWithSubProperty myComplexPojo = new MyPojoWithSubProperty("urn:ngsi-ld:complex-pojo:the-test-pojo");
		MySubProperty mySubProperty = new MySubProperty();
		mySubProperty.setPropertyName("My property");
		myComplexPojo.setMySubProperty(mySubProperty);

		assertEquals(expectedJson, OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myComplexPojo)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map Pojo with a field that is a list of objects.")
	@Test
	void testListOfSubPropertyMapping() throws JsonProcessingException {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"mySubProperty\":[{\"value\":{\"propertyName\":\"My property 1\"},\"type\":\"Property\"},{\"value\":{\"propertyName\":\"My property 2\"},\"type\":\"Property\"}]}";
		MyPojoWithListOfSubProperty myComplexPojo = new MyPojoWithListOfSubProperty(
				"urn:ngsi-ld:complex-pojo:the-test-pojo");
		MySubProperty mySubProperty1 = new MySubProperty();
		mySubProperty1.setPropertyName("My property 1");
		MySubProperty mySubProperty2 = new MySubProperty();
		mySubProperty2.setPropertyName("My property 2");
		myComplexPojo.setMySubProperties(List.of(mySubProperty1, mySubProperty2));

		assertEquals(expectedJson, OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myComplexPojo)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map Pojo with a field that is a relationship.")
	@Test
	void testSubEntityMapping() throws JsonProcessingException {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"sub-entity\":{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\"}}";

		MySubPropertyEntity mySubProperty = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:the-sub-entity");
		MyPojoWithSubEntity myComplexPojo = new MyPojoWithSubEntity("urn:ngsi-ld:complex-pojo:the-test-pojo");
		myComplexPojo.setMySubProperty(mySubProperty);
		assertEquals(expectedJson, OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myComplexPojo)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map Pojo with a field that is a relationship with additional attributes.")
	@Test
	void testSubEntityEmbedMapping() throws JsonProcessingException {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:complex-pojo:the-test-pojo\",\"type\":\"complex-pojo\",\"sub-entity\":{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\",\"role\":{\"value\":\"Sub-Entity\",\"type\":\"Property\"}}}";

		MySubPropertyEntityEmbed mySubProperty = new MySubPropertyEntityEmbed("urn:ngsi-ld:sub-entity:the-sub-entity");
		MyPojoWithSubEntityEmbed myComplexPojo = new MyPojoWithSubEntityEmbed("urn:ngsi-ld:complex-pojo:the-test-pojo");
		myComplexPojo.setMySubProperty(mySubProperty);
		assertEquals(expectedJson, OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myComplexPojo)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map Pojo with a field that is a relationship-list.")
	@Test
	void testEntityWithRelationShipList() throws JsonProcessingException {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:sub-entity-pojo:the-parent\",\"type\":\"complex-pojo\",\"sub-entity\":[{\"object\":\"urn:ngsi-ld:sub-entity:the-sub-entity\",\"type\":\"Relationship\"}]}";
		MySubPropertyEntity mySubEntity = new MySubPropertyEntity("urn:ngsi-ld:sub-entity:the-sub-entity");
		MyPojoWithListOfSubEntity myPojoWithListOfSubEntity = new MyPojoWithListOfSubEntity(
				"urn:ngsi-ld:sub-entity-pojo:the-parent");
		myPojoWithListOfSubEntity.setMySubProperty(List.of(mySubEntity));
		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithListOfSubEntity)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Pojo without an entity-id method should not be mapped.")
	@Test
	void testFailWithoutId() throws JsonProcessingException {
		MyPojoWithoutId myPojoWithoutId = new MyPojoWithoutId();
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithoutId),
				"A pojo without an entity id method should not be mapped.");
	}

	@DisplayName("Pojo with a non-uri entity-id method should not be mapped.")
	@Test
	void testFailWithWrongIdType() throws JsonProcessingException {
		MyPojoWithWrongIdType myPojoWithWrongIdType = new MyPojoWithWrongIdType(3);
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithWrongIdType),
				"A pojo without a non uri id should not be mapped.");
	}

	@DisplayName("Pojo without an entity-type method should not be mapped.")
	@Test
	void testFailWithoutType() throws JsonProcessingException {
		MyPojoWithoutType myPojoWithoutType = new MyPojoWithoutType(URI.create("urn:ngsi-ld:entity:the-entity"));
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithoutType),
				"A pojo without an entity type method should not be mapped.");
	}

	@DisplayName("Pojo with a non-string entity-type method should not be mapped.")
	@Test
	void testFailWithWrongTypeType() throws JsonProcessingException {
		MyPojoWithWrongTypeType myPojoWithWrongTypeType = new MyPojoWithWrongTypeType(
				URI.create("urn:ngsi-ld:entity:the-entity"));
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithWrongTypeType),
				"A pojo without a non string type should not be mapped.");
	}

	@DisplayName("Pojo with more than one entity-type methods should not be mapped.")
	@Test
	void testFailWithMulitpleTypes() throws JsonProcessingException {
		MyPojoWithMultipleTypes myPojoWithMultipleTypes = new MyPojoWithMultipleTypes(
				URI.create("urn:ngsi-ld:entity:the-entity"));
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithMultipleTypes),
				"A pojo without more than one type should not be mapped.");
	}

	@DisplayName("Pojo with more than one entity-id methods should not be mapped.")
	@Test
	void testFailWithMulitpleIds() throws JsonProcessingException {
		MyPojoWithMultipleIds myPojoWithMultipleIds = new MyPojoWithMultipleIds();
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithMultipleIds),
				"A pojo without more than one id should not be mapped.");
	}

	@DisplayName("Pojo with a private entity-type method should not be mapped.")
	@Test
	void testFailWithPrivateType() throws JsonProcessingException {
		MyPojoWithPrivateType myPojoWithPrivateType = new MyPojoWithPrivateType();
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithPrivateType),
				"A pojo with a private type should not be mapped.");
	}

	@DisplayName("Pojo with a private entity-id methods should not be mapped.")
	@Test
	void testFailWithPrivateId() throws JsonProcessingException {
		MyPojoWithPrivateId myPojoWithPrivateId = new MyPojoWithPrivateId();
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithPrivateId),
				"A pojo with a private id should not be mapped.");
	}

	@DisplayName("Pojo with a sub-entity without relationship object should not be mapped.")
	@Test
	void testFailWithNoRelObject() throws JsonProcessingException {
		MySubEntityWithoutRelationshipObject mySubEntityWithoutRelationshipObject = new MySubEntityWithoutRelationshipObject();
		MyInvalidRelationshipPojo myInvalidRelationshipPojo = new MyInvalidRelationshipPojo(
				"urn:ngsi-ld:entity:the-entity");
		myInvalidRelationshipPojo.setMyInvalidRelationshipPojo(mySubEntityWithoutRelationshipObject);
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myInvalidRelationshipPojo),
				"A pojo with a sub-entity without relationship object should not be mapped.");
	}

	@DisplayName("Pojo with non-list relationship-list should not be mapped.")
	@Test
	void testFailWithNonListRelList() throws JsonProcessingException {
		MySubPropertyEntity mySubPropertyEntity = new MySubPropertyEntity("urn:ngsi-ld:entity:the-entity");
		MyInvalidListRelationshipPojo myInvalidListRelationshipPojo = new MyInvalidListRelationshipPojo(
				"urn:ngsi-ld:entity:the-entity");
		myInvalidListRelationshipPojo.setMySubPropertyEntity(mySubPropertyEntity);
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myInvalidListRelationshipPojo),
				"A pojo with a non-list relationship list should not be mapped.");
	}

	@DisplayName("Pojo with null relationship-list should be mapped.")
	@Test
	void testWithNullListRelList() throws JsonProcessingException {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:entity:the-entity\",\"type\":\"complex-pojo\"}";
		MyPojoWithListOfSubEntity myPojoWithListOfSubEntity = new MyPojoWithListOfSubEntity(
				"urn:ngsi-ld:entity:the-entity");
		myPojoWithListOfSubEntity.setMySubProperty(null);
		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithListOfSubEntity)),
				"A pojo with a null relationship list should be mapped.");
	}

	@DisplayName("Pojo with null entry in relationship-list should be mapped.")
	@Test
	void testWithNullInRelList() throws JsonProcessingException {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:entity:the-entity\",\"type\":\"complex-pojo\",\"sub-entity\":[]}";
		MyPojoWithListOfSubEntity myPojoWithListOfSubEntity = new MyPojoWithListOfSubEntity(
				"urn:ngsi-ld:entity:the-entity");
		List<MySubPropertyEntity> listWithNull = new ArrayList<>();
		listWithNull.add(null);
		myPojoWithListOfSubEntity.setMySubProperty(listWithNull);
		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithListOfSubEntity)),
				"A pojo with a null entry in the relationship list should be mapped.");
	}

	@DisplayName("Pojo with sub-entity with invalid dataset id should not be mapped.")
	@Test
	void testWithInvalidSubEntityDatasetId() throws JsonProcessingException {
		MyPojoWithInvalidSubEntity myPojoWithSubEntity = new MyPojoWithInvalidSubEntity("urn:ngsi-ld:entity:entity");
		myPojoWithSubEntity.setMySubProperty(new MySubEntityWithNonURIDatasetId("urn:ngsi-ld:sub-entity:sub-entity"));
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithSubEntity),
				"A sub entity with a invalid dataset id should not be mapped.");
	}

	@DisplayName("Pojo with sub-entity with invalid relationship object should not be mapped.")
	@Test
	void testWithInvalidSubEntityRelObject() throws JsonProcessingException {
		MyPojoWithInvalidSubEntity myPojoWithSubEntity = new MyPojoWithInvalidSubEntity("urn:ngsi-ld:entity:entity");
		myPojoWithSubEntity.setMySubProperty(new MySubEntityWithNonURIRelObject("urn:ngsi-ld:sub-entity:sub-entity"));
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myPojoWithSubEntity),
				"A sub entity with an invalid relationship object should not be mapped.");
	}

	@DisplayName("Pojos throwing exceptions on methods should not be mapped.")
	@ParameterizedTest
	@MethodSource("provideThrowingPojos")
	void testFailWithThrowingPojos(MyThrowingPojo myThrowingPojo) {
		assertThrows(MappingException.class, () -> javaObjectMapper.toEntityVO(myThrowingPojo),
				"A entity with an annotated method throwing exceptions should not be mapped.");
	}

	@DisplayName("Mapping should only happen when its enabled.")
	@Test
	void testNoMappingEnabledShouldFail() {
		assertThrows(UnsupportedOperationException.class, () -> javaObjectMapper.toEntityVO(new Object()),
				"Mapping should only happen when its enabled.");
	}

	private static Stream<Arguments> provideThrowingPojos() {
		return Stream.of(
				Arguments.of(MyThrowingPojo.builder().idSupplier(() -> {
					throw new RuntimeException();
				}).build()),
				Arguments.of(MyThrowingPojo.builder().typeSupplier(() -> {
					throw new RuntimeException();
				}).build()),
				Arguments.of(MyThrowingPojo.builder().attributeSupplier(() -> {
					throw new RuntimeException();
				}).build()),
				Arguments.of(MyThrowingPojo.builder().attributeListSupplier(() -> {
					throw new RuntimeException();
				}).build()),
				Arguments.of(MyThrowingPojo.builder().relationshipSupplier(() -> {
					throw new RuntimeException();
				}).build()),
				Arguments.of(MyThrowingPojo.builder().relationshipListSupplier(() -> {
					throw new RuntimeException();
				}).build())
		);
	}

	@ParameterizedTest
	@MethodSource("getNGSIAttributePaths")
	void testGetNGSIAttributePath(Class<?> testClass, List<String> requestPath, NgsiLdAttribute expectedAttribute) {
		assertEquals(expectedAttribute, JavaObjectMapper.getNGSIAttributePath(requestPath, testClass),
				"The correct path mapping should have been returned.");
	}

	@DisplayName("Map entity with a simple unmapped property.")
	@Test
	void testWithUnmappedProperties() throws Exception {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":\"test\",\"type\":\"Property\"},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";

		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", "test"));

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		myPojoWithUnmappedProperties.setMyName("my-name");
		myPojoWithUnmappedProperties.setUnmappedProperties(unmappedProperties);

		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithUnmappedProperties)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map entity with an unmapped property, containing a relationship.")
	@Test
	void testWithUnmappedRelationship() throws Exception {

		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", "test"));
		unmappedProperties.add(new UnmappedProperty("complex", Map.of("something", "other", "id", "urn:ngsi-ld:entity:id")));

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		myPojoWithUnmappedProperties.setMyName("my-name");
		myPojoWithUnmappedProperties.setUnmappedProperties(unmappedProperties);

		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":\"test\",\"type\":\"Property\"},\"complex\":{\"object\":\"urn:ngsi-ld:entity:id\",\"type\":\"Relationship\",\"something\":{\"value\":\"other\",\"type\":\"Property\"}},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";

		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithUnmappedProperties)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map entity with a complex unmapped property.")
	@Test
	void testWithComplexUnmappedProperties() throws Exception {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":\"test\",\"type\":\"Property\"},\"complex\":{\"value\":{\"number\":{\"value\":1,\"type\":\"Property\"},\"something\":{\"value\":\"something\",\"type\":\"Property\"}},\"type\":\"Property\",\"number\":{\"value\":1,\"type\":\"Property\"},\"something\":{\"value\":\"something\",\"type\":\"Property\"}},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", "test"));
		unmappedProperties.add(new UnmappedProperty("complex", Map.of("number", 1, "something", "something")));

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		myPojoWithUnmappedProperties.setMyName("my-name");
		myPojoWithUnmappedProperties.setUnmappedProperties(unmappedProperties);

		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithUnmappedProperties)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map entity with a deep unmapped property.")
	@Test
	void testWithDeepUnmappedProperties() throws Exception {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":\"test\",\"type\":\"Property\"},\"complex\":{\"value\":{\"number\":{\"value\":1,\"type\":\"Property\"},\"deep\":{\"value\":{\"something\":{\"value\":\"deep\",\"type\":\"Property\"}},\"type\":\"Property\",\"something\":{\"value\":\"deep\",\"type\":\"Property\"}}},\"type\":\"Property\",\"number\":{\"value\":1,\"type\":\"Property\"},\"deep\":{\"value\":{\"something\":{\"value\":\"deep\",\"type\":\"Property\"}},\"type\":\"Property\",\"something\":{\"value\":\"deep\",\"type\":\"Property\"}}},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", "test"));
		unmappedProperties.add(new UnmappedProperty("complex", Map.of("number", 1, "deep", Map.of("something", "deep"))));

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		myPojoWithUnmappedProperties.setMyName("my-name");
		myPojoWithUnmappedProperties.setUnmappedProperties(unmappedProperties);

		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithUnmappedProperties)),
				"The pojo should have been translated into a valid entity");
	}

	@DisplayName("Map entity with an unmapped property list.")
	@Test
	void testWithUnmappedPropertiesList() throws Exception {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"test\":{\"value\":[1,2,3],\"type\":\"Property\"},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", List.of(1, 2, 3)));

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		myPojoWithUnmappedProperties.setMyName("my-name");
		myPojoWithUnmappedProperties.setUnmappedProperties(unmappedProperties);

		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithUnmappedProperties)),
				"The pojo should have been translated into a valid entity");
	}


	@DisplayName("Map entity with multiple unmapped properties.")
	@Test
	void testWithMultipleUnmappedProperties() throws Exception {
		String expectedJson = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\"id\":\"urn:ngsi-ld:my-pojo:the-entity\",\"type\":\"my-pojo\",\"other\":{\"value\":\"property\",\"type\":\"Property\"},\"test\":{\"value\":[1,2,3],\"type\":\"Property\"},\"name\":{\"value\":\"my-name\",\"type\":\"Property\"}}";
		List<UnmappedProperty> unmappedProperties = new ArrayList<>();
		unmappedProperties.add(new UnmappedProperty("test", List.of(1, 2, 3)));
		unmappedProperties.add(new UnmappedProperty("other", "property"));

		MyPojoWithUnmappedProperties myPojoWithUnmappedProperties = new MyPojoWithUnmappedProperties("urn:ngsi-ld:my-pojo:the-entity");
		myPojoWithUnmappedProperties.setMyName("my-name");
		myPojoWithUnmappedProperties.setUnmappedProperties(unmappedProperties);

		assertEquals(expectedJson,
				OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(myPojoWithUnmappedProperties)),
				"The pojo should have been translated into a valid entity");
	}

	private static Stream<Arguments> getNGSIAttributePaths() {
		return Stream.of(
				Arguments.of(MyPojo.class, List.of("myName"),
						new NgsiLdAttribute(List.of("name"), QueryAttributeType.STRING)),
				Arguments.of(MyPojo.class, List.of("id"),
						new NgsiLdAttribute(List.of("id"), QueryAttributeType.STRING)),
				Arguments.of(MyPojo.class, List.of("numbers"),
						new NgsiLdAttribute(List.of("numbers"), QueryAttributeType.STRING)),
				Arguments.of(MyPojoWithSubProperty.class, List.of("mySubProperty", "propertyName"),
						new NgsiLdAttribute(List.of("mySubProperty", "propertyName"), QueryAttributeType.STRING)),
				Arguments.of(MyPojoWithListOfSubEntity.class, List.of("mySubProperty", "myName"),
						new NgsiLdAttribute(List.of("sub-entity", "name"), QueryAttributeType.STRING)),
				Arguments.of(MyPojoWithSubEntityEmbed.class, List.of("mySubProperty", "role"),
						new NgsiLdAttribute(List.of("sub-entity", "role"), QueryAttributeType.STRING)),
				Arguments.of(MyMultiTypePojo.class, List.of("myBoolean"),
						new NgsiLdAttribute(List.of("boolean"), QueryAttributeType.BOOLEAN)),
				Arguments.of(MyMultiTypePojo.class, List.of("myNumber"),
						new NgsiLdAttribute(List.of("number"), QueryAttributeType.NUMBER)),
				Arguments.of(MyMultiTypePojo.class, List.of("myProperty", "number"),
						new NgsiLdAttribute(List.of("property", "number"), QueryAttributeType.NUMBER)),
				Arguments.of(MyMultiTypePojo.class, List.of("myProperty", "aBoolean"),
						new NgsiLdAttribute(List.of("property", "aBoolean"), QueryAttributeType.BOOLEAN)),
				Arguments.of(MyMultiTypePojo.class, List.of("myProperty", "string"),
						new NgsiLdAttribute(List.of("property", "string"), QueryAttributeType.STRING))
		);
	}

}