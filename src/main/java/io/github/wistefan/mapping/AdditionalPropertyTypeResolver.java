package io.github.wistefan.mapping;

import org.fiware.ngsi.model.AdditionalPropertyObjectVO;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import tools.jackson.databind.jsontype.impl.TypeNameIdResolver;

import java.util.Collection;

/**
 * Resolver for our additional properties, to be used for deserialization.
 * It can deduce Property, GeoProperty, Relationship, PropertyList, GeoPropertyList and RelationShipList.
 * There is no need for a custom serializer, since the serializer has a concrete object at hand and the type is already
 * model immanent.
 */
public class AdditionalPropertyTypeResolver extends StdTypeResolverBuilder {

	public static final String NGSI_LD_TYPE_PROPERTY_NAME = "type";

	@Override
	public TypeDeserializer buildTypeDeserializer(DeserializationContext ctxt, JavaType baseType, Collection<NamedType> subtypes) {
		return new AdditionalPropertyDeserializer(
				baseType,
				// normal type-to-name resolver is sufficient
				TypeNameIdResolver.construct(ctxt.getConfig(), baseType, subtypes, false, true),
				// per ngsi-ld, the type is in the type-property
				NGSI_LD_TYPE_PROPERTY_NAME,
				false,
				// hopefully never needed, but default behaviour is sufficient
				defineDefaultImpl(ctxt, baseType),
				ctxt.constructType(AdditionalPropertyObjectVO.class));
	}

	@Override
	public TypeSerializer buildTypeSerializer(SerializationContext ctxt, JavaType baseType, Collection<NamedType> subtypes) {
		// no type id is written, the concrete VOs already carry their type
		return null;
	}
}
