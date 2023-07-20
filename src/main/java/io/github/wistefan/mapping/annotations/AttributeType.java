package io.github.wistefan.mapping.annotations;

/**
 * Enum to indicate the type of an attribute in NGSI-LD
 */
public enum AttributeType {

	PROPERTY,
	PROPERTY_LIST,
	GEO_PROPERTY,
	GEO_PROPERTY_LIST,
	GEO_QUERY,
	ENTITY_INFO_LIST,
	PROPERTY_SET,
	NOTIFICATION_PARAMS,
	RELATIONSHIP,
	RELATIONSHIP_LIST
}
