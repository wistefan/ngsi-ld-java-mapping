package io.github.wistefan.mapping;

import org.fiware.ngsi.model.GeoQueryVO;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializer for GeoQuery
 * This empty deserializer was created just to avoid the IllegalArgumentException on
 * an unused empty field, but needed by an API specification. Should be implemented
 * in the future releases once this type will be used
 */
public class GeoQueryDeserializer extends StdDeserializer<GeoQueryVO> {

    public GeoQueryDeserializer() {
        this(GeoQueryVO.class);
    }

    public GeoQueryDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public GeoQueryVO deserialize(final JsonParser p, final DeserializationContext context) {
        // consume the value, Jackson 3 fails on trailing tokens by default
        p.skipChildren();
        return new GeoQueryVO();
    }

}
