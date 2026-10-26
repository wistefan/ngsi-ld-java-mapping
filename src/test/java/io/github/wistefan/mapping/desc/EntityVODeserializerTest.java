package io.github.wistefan.mapping.desc;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.github.wistefan.mapping.AdditionalPropertyMixin;
import org.fiware.ngsi.model.AdditionalPropertyVO;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.ngsi.model.PropertyListVO;
import org.fiware.ngsi.model.PropertyVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityVODeserializerTest {


    @Test
    public void testDeserialization() throws JacksonException {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addMixIn(AdditionalPropertyVO.class, AdditionalPropertyMixin.class)
                .build();

        String testEntity = "{\n" +
                "  \"id\" : \"urn:ngsi-ld:product-offering:716fa880-b910-458a-bc96-14a6ba98cb48\",\n" +
                "  \"type\" : \"product-offering\",\n" +
                "  \"description\" : {\n" +
                "    \"type\" : \"Property\",\n" +
                "    \"value\" : \"\"\n" +
                "  },\n" +
                "  \"bundledProductOffering\" : [ ],\n" +
                "  \"listProp\": [{\n" +
                "    \"type\" : \"Property\",\n" +
                "    \"value\" : \"A\"\n" +
                "   }, {\n" +
                "    \"type\" : \"Property\",\n" +
                "    \"value\" : \"B\"\n" +
                "   }" +
                "]}";
        EntityVO e = objectMapper.readValue(testEntity, EntityVO.class);
        assertTrue(e.getAdditionalProperties().containsKey("description"), "The properties should be deserialized.");
        assertTrue(e.getAdditionalProperties().containsKey("bundledProductOffering"), "The properties should be deserialized.");
        assertTrue(e.getAdditionalProperties().get("description") instanceof PropertyVO, "The description should have been desiralized to a single property.");
        assertTrue(e.getAdditionalProperties().get("bundledProductOffering") instanceof PropertyListVO, "The bundledProductOffering should have been desiralized to a property list.");
    }
}
