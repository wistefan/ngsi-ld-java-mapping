package io.github.wistefan.mapping;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.fiware.ngsi.model.*;

import java.util.Set;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for the {@link org.fiware.ngsi.model.AdditionalPropertyVO}
 * It can differentiate between list and object type properties and handle them with the fitting serializer.
 */
public class AdditionalPropertyDeserializer extends AsArrayTypeDeserializer {

	private static final Set<String> NGSI_LD_TYPES = Set.of("Property", "GeoProperty", "Relationship");

	/**
	 * Default property-type deserializer can be used for the individual objects.
	 */
	private final AsPropertyTypeDeserializer additionalPropertyObjectDeser;

	public AdditionalPropertyDeserializer(JavaType bt, TypeIdResolver idRes, String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
		super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl);
		additionalPropertyObjectDeser = new AsPropertyTypeDeserializer(
				TypeFactory.defaultInstance().constructType(new TypeReference<AdditionalPropertyObjectVO>() {}),
				idRes,
				AdditionalPropertyTypeResolver.NGSI_LD_TYPE_PROPERTY_NAME,
				false,
				defaultImpl);

	}

	@Override
	protected Object _deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

		// default behaviour
		if (p.canReadTypeId()) {
			Object typeId = p.getTypeId();
			if (typeId != null) {
				return _deserializeWithNativeTypeId(p, ctxt, typeId);
			}
		}

		// Handle primitive values that cannot be NGSI-LD properties.
		// These occur when complex Java objects with plain-typed fields are stored as
		// NGSI-LD sub-properties (e.g. a Characteristic value containing a nested object
		// whose fields happen to be named "type" or "properties").
		JsonToken currentToken = p.currentToken();
		if (currentToken == JsonToken.VALUE_STRING) {
			return new PropertyVO().value(p.getText());
		} else if (currentToken == JsonToken.VALUE_NUMBER_INT) {
			return new PropertyVO().value(p.getLongValue());
		} else if (currentToken == JsonToken.VALUE_NUMBER_FLOAT) {
			return new PropertyVO().value(p.getDoubleValue());
		} else if (currentToken == JsonToken.VALUE_TRUE || currentToken == JsonToken.VALUE_FALSE) {
			return new PropertyVO().value(p.getBooleanValue());
		} else if (currentToken == JsonToken.VALUE_NULL) {
			return null;
		}

		// if no type id is present we need the next token to decide if its an object or an array
		JsonToken t = p.nextToken();
		// case FIELD_NAME:
		// If the token is of type FIELD_NAME, no START_ARRAY was present, thus we can take the
		// object and directly serialize it with the standard deserializer
		if (t == JsonToken.FIELD_NAME) {
			// Buffer the full object so we can peek for the NGSI-LD "type" discriminator
			// before committing to the typed deserializer. Without this, a plain JSON object
			// (e.g. a Characteristic value whose fields are named "type" or "properties")
			// causes an InvalidTypeIdException.
			TokenBuffer buffer = new TokenBuffer(p, ctxt);
			buffer.writeStartObject();
			while (p.currentToken() != JsonToken.END_OBJECT && p.currentToken() != null) {
				String fieldName = p.currentName();
				buffer.writeFieldName(fieldName);
				p.nextToken();
				buffer.copyCurrentStructure(p);
				p.nextToken();
			}
			buffer.writeEndObject();

			// Peek: is the "type" field present with a valid NGSI-LD value?
			if (hasNgsiLdType(buffer)) {
				JsonParser bp = buffer.asParserOnFirstToken();
				bp.nextToken(); // START_OBJECT -> FIELD_NAME
				return additionalPropertyObjectDeser.deserializeTypedFromObject(bp, ctxt);
			} else {
				// Plain object — not an NGSI-LD property. Wrap its value in a PropertyVO.
				JsonParser bp = buffer.asParserOnFirstToken();
				Object plainValue = ctxt.readValue(bp, Object.class);
				return new PropertyVO().value(plainValue);
			}
		}
		// case START_OBJECT
		// If a start-object(e.g. '{') token is present, a START_ARRAY was present and we have at least one
		// one object to be deserialized. The parser is handed over to the specialized method.
		if (t == JsonToken.START_OBJECT) {
			return deserializeArray(p, ctxt);
		} else if (t == JsonToken.END_ARRAY) {
			// we received an empty list
			return new PropertyListVO();
		}

		return super._deserialize(p, ctxt);
	}

	/**
	 * Peek inside a TokenBuffer to check whether the buffered object contains a "type"
	 * field whose value is a valid NGSI-LD property type (Property, GeoProperty, Relationship).
	 */
	private boolean hasNgsiLdType(TokenBuffer buffer) throws IOException {
		JsonParser peek = buffer.asParserOnFirstToken();
		// START_OBJECT
		while (peek.nextToken() == JsonToken.FIELD_NAME) {
			String name = peek.currentName();
			JsonToken valueToken = peek.nextToken();
			if (AdditionalPropertyTypeResolver.NGSI_LD_TYPE_PROPERTY_NAME.equals(name)
					&& valueToken == JsonToken.VALUE_STRING
					&& NGSI_LD_TYPES.contains(peek.getText())) {
				return true;
			}
			// Skip the value (could be object/array)
			peek.skipChildren();
		}
		return false;
	}

	/**
	 * Deserializes an array of NGSI-LD properties(e.g. Property, GeoProperty or Relationship) to there concrete
	 * list types(e.g. PropertyList, GeoPropertyList, RelationshipList)
	 * @param p the current parser
	 * @param ctxt the desrialization context to use
	 * @return object of the deserialized list, will be a PropertyList, GeoPropertyList or RelationshipList type
	 * @throws IOException
	 */
	private Object deserializeArray(JsonParser p, DeserializationContext ctxt) throws IOException {
		List<Object> deserializedObjects = new ArrayList<>();
		JsonToken next;
		do {
			next = p.nextToken();
			if (next == JsonToken.FIELD_NAME) {
				deserializedObjects.add(additionalPropertyObjectDeser.deserializeTypedFromObject(p, ctxt));
			}
		} while (next != JsonToken.END_ARRAY);

		if (deserializedObjects.isEmpty()) {
			// any empty list is sufficient
			return new PropertyListVO();
		}
		// get type of first object to decide the list type
		Object firstObject = deserializedObjects.get(0);
		if (firstObject instanceof PropertyVO) {
			PropertyListVO propertyVOS = new PropertyListVO();
			deserializedObjects.stream().map(PropertyVO.class::cast).forEach(propertyVOS::add);
			return propertyVOS;
		} else if (firstObject instanceof GeoPropertyVO) {
			GeoPropertyListVO geoPropertyVOS = new GeoPropertyListVO();
			deserializedObjects.stream().map(GeoPropertyVO.class::cast).forEach(geoPropertyVOS::add);
			return geoPropertyVOS;
		} else if (firstObject instanceof RelationshipVO) {
			RelationshipListVO relationshipVOS = new RelationshipListVO();
			deserializedObjects.stream().map(RelationshipVO.class::cast).forEach(relationshipVOS::add);
			return relationshipVOS;
		}
		throw new MappingException("Was not able to deserialize the array.");
	}

}
