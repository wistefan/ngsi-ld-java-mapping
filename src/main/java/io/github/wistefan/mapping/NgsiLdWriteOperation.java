package io.github.wistefan.mapping;

/**
 * The NGSI-LD write operation an {@link org.fiware.ngsi.model.EntityVO} is built for when mapping a java object to the
 * NGSI-LD model ({@link JavaObjectMapper#toEntityVO(Object, NgsiLdWriteOperation)}). Reading from the broker
 * maps in the other direction and is not affected. NGSI-LD requires different handling of a list-type
 * attribute (Property/Relationship) that ends up with no instances:
 * <ul>
 *     <li>{@link #CREATE} - the attribute is simply omitted, since the entity does not exist
 *     yet and there is nothing to delete.</li>
 *     <li>{@link #UPDATE} - the attribute is represented with a single NGSI-LD Null instance
 *     ({@code urn:ngsi-ld:null}), so the broker deletes the (default) attribute instance
 *     instead of rejecting an empty array.</li>
 * </ul>
 */
public enum NgsiLdWriteOperation {
	CREATE,
	UPDATE
}
