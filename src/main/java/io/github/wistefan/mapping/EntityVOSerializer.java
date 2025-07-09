package io.github.wistefan.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Bean;
import io.micronaut.core.serialize.ObjectSerializer;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

@Singleton
@Bean
public class EntityVOSerializer implements ObjectSerializer {

	private final ObjectMapper objectMapper;

	public EntityVOSerializer(CacheSerdeableObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Optional<byte[]> serialize(Object object) throws SerializationException {
		try {
			return Optional.ofNullable(objectMapper.writeValueAsBytes(object));
		} catch (JsonProcessingException e) {
			throw new SerializationException("Error serializing object to JSON: " + e.getMessage(), e);
		}
	}

	@Override
	public void serialize(Object object, OutputStream outputStream) throws SerializationException {
		try {
			objectMapper.writeValue(outputStream, object);
		} catch (IOException e) {
			throw new SerializationException("Error serializing object to JSON: " + e.getMessage(), e);
		}
	}

	@Override
	public <T> Optional<T> deserialize(byte[] bytes, Class<T> requiredType) throws SerializationException {
		try {
			return Optional.ofNullable(objectMapper.readValue(bytes, requiredType));
		} catch (IOException e) {
			throw new SerializationException("Error deserializing object from JSON: " + e.getMessage(), e);
		}
	}

	@Override
	public <T> Optional<T> deserialize(InputStream inputStream, Class<T> requiredType) throws SerializationException {
		try {
			return Optional.ofNullable(objectMapper.readValue(inputStream, requiredType));
		} catch (IOException e) {
			throw new SerializationException("Error deserializing object from JSON: " + e.getMessage(), e);
		}
	}

	@Override
	public <T> Optional<T> deserialize(byte[] bytes, Argument<T> requiredType) throws SerializationException {
		try {
			return Optional.ofNullable(objectMapper.readValue(bytes, requiredType.getType()));
		} catch (Exception e) {
			throw new SerializationException("Error deserializing object from JSON: " + e.getMessage(), e);
		}
	}

	@Override
	public <T> Optional<T> deserialize(InputStream inputStream, Argument<T> requiredType) throws SerializationException {
		try {
			return Optional.ofNullable(objectMapper.readValue(inputStream, requiredType.getType()));
		} catch (IOException e) {
			throw new SerializationException("Error deserializing object from JSON: " + e.getMessage(), e);
		}
	}
}

