package io.github.wistefan.mapping.desc;

import io.github.wistefan.mapping.CacheSerdeableObjectMapper;
import io.github.wistefan.mapping.EntityVOSerializer;
import io.github.wistefan.mapping.desc.pojos.MySimplePojo;
import io.micronaut.core.type.Argument;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.ngsi.model.GeoPropertyVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class EntityVOSerializerTest {

    private EntityVOSerializer entityVOSerializer;
    private static final CacheSerdeableObjectMapper objectMapper = new CacheSerdeableObjectMapper();

    @BeforeEach
    public void setup() {
        entityVOSerializer = new EntityVOSerializer(objectMapper);
    }

    @Test
    public void testPolymorphicFields() {
        EntityVO entityVO = new EntityVO();
        entityVO.setCreatedAt(Instant.now());
        GeoPropertyVO location = new GeoPropertyVO();
        location.setUnitCode("AA");
        location.setValue("val");
        entityVO.setLocation(location);
        entityVO.setAdditionalProperties("dd", location);

        Optional<byte[]> s1 = entityVOSerializer.serialize(entityVO);
        Assertions.assertTrue(s1.isPresent());

        Optional<EntityVO> d1 = entityVOSerializer.deserialize(s1.get(), EntityVO.class);
        Assertions.assertTrue(d1.isPresent());
        Assertions.assertEquals(entityVO.getLocation().getType(), d1.get().getLocation().getType());

        Optional<byte[]> s2 = entityVOSerializer.serialize(List.of(entityVO));
        Assertions.assertTrue(s2.isPresent());

        Optional<?> d2 = entityVOSerializer.deserialize(s2.get(), Argument.of(List.class, EntityVO.class));
        Assertions.assertTrue(d2.isPresent());
    }

    @Test
    public void testListFields() {
        MySimplePojo pojo = new MySimplePojo();
        pojo.setNumbers(List.of(1,2,3));

        Optional<byte[]> s1 = entityVOSerializer.serialize(pojo);
        Assertions.assertTrue(s1.isPresent());

        Optional<MySimplePojo> d1 = entityVOSerializer.deserialize(s1.get(), MySimplePojo.class);
        Assertions.assertTrue(d1.isPresent());
        Assertions.assertEquals(pojo, d1.get());
    }

    @Test
    public void testListObjects() {
        MySimplePojo pojo = new MySimplePojo();
        pojo.setNumbers(List.of(1,2,3));
        pojo.setMyName("Name");

        Optional<byte[]> s1 = entityVOSerializer.serialize(List.of(pojo));
        Assertions.assertTrue(s1.isPresent());

        Optional<?> d1 = entityVOSerializer.deserialize(s1.get(), Argument.of(List.class, MySimplePojo.class));
        Assertions.assertTrue(d1.isPresent());
        Assertions.assertEquals(List.of(pojo), d1.get());
    }
}
