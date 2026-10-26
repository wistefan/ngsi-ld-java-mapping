package io.github.wistefan.mapping.desc;

import io.github.wistefan.mapping.NgsiLdMappingModule;
import org.fiware.ngsi.model.GeoQueryVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Stream;

public class GeoQueryDeserializerTest {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new NgsiLdMappingModule())
            .build();
    private final GeoQueryVO emptyGeoQuery = new GeoQueryVO();

    @ParameterizedTest
    @MethodSource("provideJsonSerializedGeoQueries")
    public void testEmptyDeserializer(String json) {
        Assertions.assertEquals(emptyGeoQuery, objectMapper.readValue(json, GeoQueryVO.class));
    }

    private static Stream<String> provideJsonSerializedGeoQueries() {
        return Stream.of(
                "{\"value\":\"Any valid json\"}",
                "{\"georel\":\"near;maxDistance==2000\",\"geometry\":\"Point\",\"coordinates\":[8.68,49.4]}",
                "{\"geometry\":\"Polygon\",\"coordinates\":[[[0,0],[0,1],[1,1],[0,0]]],\"nested\":{\"a\":{\"b\":1}}}",
                "\"a scalar value\"",
                objectMapper.writeValueAsString(new GeoQueryVO())
        );
    }

}
