package io.github.wistefan.mapping;

import io.micronaut.context.annotation.Bean;
import io.micronaut.core.serialize.ObjectSerializer;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;
import jakarta.inject.Singleton;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

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
        } catch (JacksonException e) {
            throw new SerializationException("Error serializing object to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public void serialize(Object object, OutputStream outputStream) throws SerializationException {
        try {
            objectMapper.writeValue(outputStream, object);
        } catch (JacksonException e) {
            throw new SerializationException("Error serializing object to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> Optional<T> deserialize(byte[] bytes, Class<T> requiredType) throws SerializationException {
        try {
            return Optional.ofNullable(objectMapper.readValue(bytes, requiredType));
        } catch (JacksonException e) {
            throw new SerializationException("Error deserializing object from JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> Optional<T> deserialize(InputStream inputStream, Class<T> requiredType) throws SerializationException {
        try {
            return Optional.ofNullable(objectMapper.readValue(inputStream, requiredType));
        } catch (JacksonException e) {
            throw new SerializationException("Error deserializing object from JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> Optional<T> deserialize(byte[] bytes, Argument<T> requiredType) throws SerializationException {
        try {
            JavaType javaType = JacksonConfiguration.constructType(requiredType, objectMapper.getTypeFactory());
            return Optional.ofNullable(objectMapper.readValue(bytes, javaType));
        } catch (Exception e) {
            throw new SerializationException("Error deserializing object from JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> Optional<T> deserialize(InputStream inputStream, Argument<T> requiredType) throws SerializationException {
        try {
            JavaType javaType = JacksonConfiguration.constructType(requiredType, objectMapper.getTypeFactory());
            return Optional.ofNullable(objectMapper.readValue(inputStream, javaType));
        } catch (JacksonException e) {
            throw new SerializationException("Error deserializing object from JSON: " + e.getMessage(), e);
        }
    }
}

