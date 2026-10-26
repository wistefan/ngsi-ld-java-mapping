package io.github.wistefan.mapping.desc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.github.wistefan.mapping.*;
import io.github.wistefan.mapping.desc.pojos.MyPojoWithUnmappedProperties;
import org.fiware.ngsi.model.EntityVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * NGSI-LD does not allow member names starting with "@" (ETSI GS CIM 009 clause 4.6.2). Keys like
 * {@code @referredType} inside unmapped properties have to be escaped, while data stored before (with the raw
 * "@" key) has to stay readable.
 */
class AtPrefixedUnmappedKeysTest {

	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder(new EscapeCleaningJsonFactory())
			.addModule(new NgsiLdMappingModule())
			.build();
	// inspects the json as sent to the broker - the EscapeCleaningJsonFactory would already unescape while parsing
	private static final ObjectMapper RAW_MAPPER = JsonMapper.builder().build();
	private static final String ENTITY_ID = "urn:ngsi-ld:my-pojo:the-entity";

	private JavaObjectMapper javaObjectMapper;
	private EntityVOMapper entityVOMapper;

	@BeforeEach
	public void setup() {
		javaObjectMapper = new JavaObjectMapper(new MappingProperties(), JsonMapper.builder().build());
		entityVOMapper = new EntityVOMapper(new MappingProperties(), OBJECT_MAPPER, mock(EntitiesRepository.class));
	}

	private static Map<String, Object> party(String id, String role) {
		Map<String, Object> party = new LinkedHashMap<>();
		party.put("id", id);
		party.put("href", id);
		party.put("name", "the-org");
		party.put("role", role);
		party.put("@referredType", "Organization");
		return party;
	}

	private static MyPojoWithUnmappedProperties pojoWith(String name, Object value) {
		MyPojoWithUnmappedProperties pojo = new MyPojoWithUnmappedProperties(ENTITY_ID);
		pojo.setMyName("my-name");
		pojo.setUnmappedProperties(new ArrayList<>(List.of(new UnmappedProperty(name, value))));
		return pojo;
	}

	private static List<String> atPrefixedNames(JsonNode node) {
		List<String> found = new ArrayList<>();
		if (node.isObject()) {
			node.forEachEntry((name, value) -> {
				if (name.startsWith("@") && !name.equals("@context")) {
					found.add(name);
				}
				found.addAll(atPrefixedNames(value));
			});
		} else if (node.isArray()) {
			node.forEach(item -> found.addAll(atPrefixedNames(item)));
		}
		return found;
	}

	private MyPojoWithUnmappedProperties roundTrip(MyPojoWithUnmappedProperties pojo) throws Exception {
		String json = OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(pojo));
		EntityVO entityVO = OBJECT_MAPPER.readValue(json, EntityVO.class);
		return entityVOMapper.fromEntityVO(entityVO, MyPojoWithUnmappedProperties.class).block();
	}

	@DisplayName("Keys starting with @ in unmapped properties are escaped.")
	@Test
	void atKeysAreNotSentToTheBroker() throws Exception {
		MyPojoWithUnmappedProperties pojo = pojoWith("relatedParty",
				List.of(party("urn:ngsi-ld:organization:one", "seller"), party("urn:ngsi-ld:organization:two", "customer")));

		JsonNode written = RAW_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(pojo)));

		assertEquals(List.of(), atPrefixedNames(written), "No member name may start with @.");
		assertTrue(written.toString().contains("\"tmfEscaped-@referredType\""), "The escaped key should be written.");
	}

	@DisplayName("An unmapped property named with a leading @ is escaped and read back.")
	@Test
	void atPropertyNameSurvivesTheRoundTrip() throws Exception {
		MyPojoWithUnmappedProperties pojo = pojoWith("@baseType", "Extension");

		JsonNode written = RAW_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(javaObjectMapper.toEntityVO(pojo)));
		assertEquals(List.of(), atPrefixedNames(written), "No member name may start with @.");

		MyPojoWithUnmappedProperties read = roundTrip(pojo);
		assertEquals("@baseType", read.getUnmappedProperties().get(0).getName());
		assertEquals("Extension", read.getUnmappedProperties().get(0).getValue());
	}

	@DisplayName("Unmapped properties with @ keys survive the round trip.")
	@Test
	void atKeysSurviveTheRoundTrip() throws Exception {
		List<Map<String, Object>> parties = List.of(party("urn:ngsi-ld:organization:one", "seller"),
				party("urn:ngsi-ld:organization:two", "customer"));

		MyPojoWithUnmappedProperties read = roundTrip(pojoWith("relatedParty", parties));

		assertEquals(parties, read.getUnmappedProperties().get(0).getValue(),
				"The parties should be read back with @referredType.");
	}

	@DisplayName("A single object with an @ key survives the round trip.")
	@Test
	void singleObjectWithAtKeySurvivesTheRoundTrip() throws Exception {
		Map<String, Object> party = party("urn:ngsi-ld:organization:one", "seller");

		MyPojoWithUnmappedProperties read = roundTrip(pojoWith("owner", party));

		assertEquals(party, read.getUnmappedProperties().get(0).getValue(),
				"The object should be read back with @referredType.");
	}

	@DisplayName("Keys starting with 'at' are not touched.")
	@Test
	void keysStartingWithAtAreNotTouched() throws Exception {
		Map<String, Object> value = new LinkedHashMap<>();
		value.put("attachment", "a");
		value.put("attribute", "b");
		value.put("atReferredType", "c");

		MyPojoWithUnmappedProperties read = roundTrip(pojoWith("extension", value));

		assertEquals(value, read.getUnmappedProperties().get(0).getValue(),
				"attachment, attribute and an explicit atReferredType should be read back unchanged.");
	}

	@DisplayName("Data stored with the raw @referredType (before the escaping) is still read.")
	@Test
	void legacyRawAtKeysAreStillRead() throws Exception {
		String legacyEntity = "{\"@context\":\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\","
				+ "\"id\":\"" + ENTITY_ID + "\",\"type\":\"my-pojo\","
				+ "\"name\":{\"type\":\"Property\",\"value\":\"my-name\"},"
				+ "\"relatedParty\":{\"type\":\"Property\",\"value\":[{\"tmfEscaped-id\":\"urn:ngsi-ld:organization:one\","
				+ "\"role\":\"seller\",\"@referredType\":\"Organization\"}]}}";
		EntityVO entityVO = OBJECT_MAPPER.readValue(legacyEntity, EntityVO.class);

		MyPojoWithUnmappedProperties read = entityVOMapper.fromEntityVO(entityVO, MyPojoWithUnmappedProperties.class).block();

		Map<String, Object> expectedParty = new LinkedHashMap<>();
		expectedParty.put("id", "urn:ngsi-ld:organization:one");
		expectedParty.put("role", "seller");
		expectedParty.put("@referredType", "Organization");
		assertEquals(List.of(expectedParty), read.getUnmappedProperties().get(0).getValue(),
				"Old data should be read exactly as before.");
	}
}
