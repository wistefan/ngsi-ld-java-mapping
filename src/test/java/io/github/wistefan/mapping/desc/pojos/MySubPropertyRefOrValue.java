package io.github.wistefan.mapping.desc.pojos;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;

/**
 * Models the TMForum {@code *RefOrValue} pattern: a nested object exposing
 * reserved-word fields ({@code id}, {@code value}, {@code type}) alongside
 * other attributes. Used to exercise that those fields survive the
 * round-trip when the enclosing attribute is mapped through
 * {@link io.github.wistefan.mapping.annotations.AttributeType#PROPERTY}.
 */
@EqualsAndHashCode
@Data
public class MySubPropertyRefOrValue {

	private URI id;
	private URI href;
	private String value;
	private String type;
	private String name;
}
