package io.github.wistefan.mapping;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pojo class to be used for unmapped properties
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnmappedProperty {

	private String name;
	private Object value;
}
