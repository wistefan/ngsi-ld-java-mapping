package io.github.wistefan.mapping.desc;

import io.github.wistefan.mapping.GeoQueryDeserializer;
import org.fiware.ngsi.model.GeoQueryVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Stream;

public class GeoQueryDeserializerTest {

    private final GeoQueryDeserializer geoQueryDeserializer = new GeoQueryDeserializer();
    private final GeoQueryVO emptyGeoQuery = new GeoQueryVO();
    private static final ObjectMapper objectMapper = new JsonMapper();
    private final DeserializationContext ctxt = objectMapper._deserializationContext();

    @ParameterizedTest
    @MethodSource("provideJsonSerializedGeoQueries")
    public void testEmptyDeserializer(String json) {
        try (JsonParser parser = objectMapper.createParser(json)) {
            GeoQueryVO deserialized = geoQueryDeserializer.deserialize(parser, ctxt);

            Assertions.assertEquals(emptyGeoQuery, deserialized);
        }
    }

    private static Stream<String> provideJsonSerializedGeoQueries() throws JacksonException {
        return Stream.of(
                "invalid json",
                "",
                "{\"value\":\"Any valid json\"}",
                objectMapper.writeValueAsString(new GeoQueryVO())
        );
    }

}
