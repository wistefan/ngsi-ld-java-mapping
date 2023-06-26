package io.github.wistefan.mapping;

import io.github.wistefan.mapping.annotations.MappingEnabled;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Abstract superclass for mappers between NGSI-LD and the annotated objects
 */
public abstract class Mapper {

	/**
	 * Check if mapping is enabled for the given target class and return the {@link MappingEnabled} annotation in that case.
	 */
	protected <T> Optional<MappingEnabled> isMappingEnabled(Class<T> tClass) {
		return Arrays.stream(tClass.getAnnotations())
				.filter(MappingEnabled.class::isInstance)
				.map(MappingEnabled.class::cast)
				.findFirst();
	}

}
