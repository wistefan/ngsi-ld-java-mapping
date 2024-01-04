package io.github.wistefan.mapping.desc;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wistefan.mapping.GeoQueryDeserializer;
import org.fiware.ngsi.model.GeoQueryVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

public class GeoQueryDeserializerTest {

    private final GeoQueryDeserializer geoQueryDeserializer = new GeoQueryDeserializer();
    private final GeoQueryVO emptyGeoQuery = new GeoQueryVO();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final DeserializationContext ctxt = objectMapper.getDeserializationContext();

    @ParameterizedTest
    @MethodSource("provideJsonSerializedGeoQueries")
    public void testEmptyDeserializer(String json) throws IOException {
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            GeoQueryVO deserialized = geoQueryDeserializer.deserialize(parser, ctxt);

            Assertions.assertEquals(emptyGeoQuery, deserialized);
        }
    }

    private static Stream<String> provideJsonSerializedGeoQueries() throws JsonProcessingException {
        return Stream.of(
                "invalid json",
                "",
                "{\"value\":\"Any valid json\"}",
                objectMapper.writeValueAsString(new GeoQueryVO())
        );
    }

}
