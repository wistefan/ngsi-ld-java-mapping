package io.github.wistefan.mapping;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.fiware.ngsi.model.GeoQueryVO;

/**
 * Deserializer for GeoQuery
 * This empty deserializer was created just to avoid the IllegalArgumentException on
 * an unused empty field, but needed by an API specification. Should be implemented
 * in the future releases once this type will be used
 */
public class GeoQueryDeserializer extends StdDeserializer<GeoQueryVO> {

    public GeoQueryDeserializer() {
        this(null);
    }

    public GeoQueryDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public GeoQueryVO deserialize(final JsonParser p, final DeserializationContext context) {
        return new GeoQueryVO();
    }

}
