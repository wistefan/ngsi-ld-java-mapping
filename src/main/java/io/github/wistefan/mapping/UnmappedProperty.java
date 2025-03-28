package io.github.wistefan.mapping;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Pojo class to be used for unmapped properties
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UnmappedProperty {

	private String name;
	private Object value;
}
