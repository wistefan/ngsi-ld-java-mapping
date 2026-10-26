package io.github.wistefan.mapping;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.annotation.JsonTypeResolver;

/**
 * Since the list objects(PropertyList, GeoPropertyList, RelationshipList) cannot be uniquely deduced, we need to mixin our custom resolve.
 * The normal jackson resolvers use type information provided by field of the object or fieldnames of the object or the overall signature.
 * Our lists are dependent on a field value(e.g. type) of the list objects, which is not supported by the default resolvers.
 */
// Jackson 3 ignores a custom resolver on Id.NONE; the id settings are unused by AdditionalPropertyTypeResolver.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = AdditionalPropertyTypeResolver.NGSI_LD_TYPE_PROPERTY_NAME)
@JsonTypeResolver(AdditionalPropertyTypeResolver.class)
public abstract class AdditionalPropertyMixin {
}
