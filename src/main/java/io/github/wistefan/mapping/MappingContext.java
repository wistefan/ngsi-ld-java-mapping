package io.github.wistefan.mapping;

/**
 * Distinguishes why an {@link EntityVO} is being built, since NGSI-LD requires different
 * handling of a list-type attribute (Property/Relationship) that ends up with no instances:
 * <ul>
 *     <li>{@link #CREATE} - the attribute is simply omitted, since the entity does not exist
 *     yet and there is nothing to delete.</li>
 *     <li>{@link #UPDATE} - the attribute is represented with a single NGSI-LD Null instance
 *     ({@code urn:ngsi-ld:null}), so the broker deletes the (default) attribute instance
 *     instead of rejecting an empty array.</li>
 * </ul>
 */
public enum MappingContext {
	CREATE,
	UPDATE
}
