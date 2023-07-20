package io.github.wistefan.mapping.desc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wistefan.mapping.SubscriptionVOMapper;
import io.github.wistefan.mapping.desc.pojos.*;
import org.fiware.ngsi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubscriptionVOMapperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ANY_QUERY = "some_query";
    private static final String ANY_ENDPOINT_URI = "some_uri";
    private static final String ANY_SUBSCRIPTION_ID = "urn:ngsi-ld:complex-pojo:subscription";
    private static final String ANY_ENTITY_ID = "urn:ngsi-ld:pojo:entity";
    private static final String ANY_GEO_QUERY_GEOMETRY = "some_geometry";
    private static final String ANY_WATCHED_ATTRIBUTE = "temperature";
    private SubscriptionVOMapper subscriptionVOMapper;

    @BeforeEach
    public void setup() {
        subscriptionVOMapper = new SubscriptionVOMapper(OBJECT_MAPPER);
    }

    @DisplayName("Well known properties should properly be mapped.")
    @Test
    void mapWithWellKnown() {
        SubscriptionVO subscriptionVO = new SubscriptionVO().id(URI.create(ANY_SUBSCRIPTION_ID))
                .type(SubscriptionVO.Type.SUBSCRIPTION);
        subscriptionVO.setExpires(Instant.MAX);
        subscriptionVO.setQ(ANY_QUERY);

        MySubscriptionPojo expectedSubscription = new MySubscriptionPojo(ANY_SUBSCRIPTION_ID);
        expectedSubscription.setExpires(Instant.MAX);
        expectedSubscription.setQ(ANY_QUERY);

        assertEquals(expectedSubscription, subscriptionVOMapper.fromSubscriptionVO(subscriptionVO,
                MySubscriptionPojo.class).block(), "Well known properties should properly be mapped.");
    }

    @DisplayName("Geo query property should properly be mapped.")
    @Test
    void mapWithGeoQuery() {
        SubscriptionVO subscriptionVO = new SubscriptionVO().id(URI.create(ANY_SUBSCRIPTION_ID))
                .type(SubscriptionVO.Type.SUBSCRIPTION);
        GeoQueryVO geoQ = new GeoQueryVO();
        geoQ.setGeometry(ANY_GEO_QUERY_GEOMETRY);
        subscriptionVO.setGeoQ(geoQ);

        MySubscriptionPojo expectedSubscription = new MySubscriptionPojo(ANY_SUBSCRIPTION_ID);
        MyGeoQueryProperty geoQueryProperty = new MyGeoQueryProperty();
        geoQueryProperty.setGeometry(ANY_GEO_QUERY_GEOMETRY);
        expectedSubscription.setGeoQ(geoQueryProperty);

        assertEquals(expectedSubscription, subscriptionVOMapper.fromSubscriptionVO(subscriptionVO,
                MySubscriptionPojo.class).block(), "Geo query property should properly be mapped.");
    }

    @DisplayName("Entity info property should properly be mapped.")
    @Test
    void mapWithEntityInfo() {
        SubscriptionVO subscriptionVO = new SubscriptionVO().id(URI.create(ANY_SUBSCRIPTION_ID))
                .type(SubscriptionVO.Type.SUBSCRIPTION);
        List<EntityInfoVO> entityInfoVOS = new ArrayList<>();
        EntityInfoVO entityInfoVO = new EntityInfoVO();
        entityInfoVO.setId(ANY_ENTITY_ID);
        entityInfoVOS.add(entityInfoVO);
        subscriptionVO.setEntities(entityInfoVOS);

        MySubscriptionPojo expectedSubscription = new MySubscriptionPojo(ANY_SUBSCRIPTION_ID);
        List<MyEntityInfoProperty> entityInfoProperties = new ArrayList<>();
        MyEntityInfoProperty entityInfoProperty = new MyEntityInfoProperty();
        entityInfoProperty.setId(ANY_ENTITY_ID);
        entityInfoProperties.add(entityInfoProperty);
        expectedSubscription.setEntities(entityInfoProperties);

        assertEquals(expectedSubscription, subscriptionVOMapper.fromSubscriptionVO(subscriptionVO,
                MySubscriptionPojo.class).block(), "Entity info property should properly be mapped.");
    }

    @DisplayName("Watched attributes property should properly be mapped.")
    @Test
    void mapWithWatchedAttributes() {
        SubscriptionVO subscriptionVO = new SubscriptionVO().id(URI.create(ANY_SUBSCRIPTION_ID))
                .type(SubscriptionVO.Type.SUBSCRIPTION);
        Set<String> watchedAttrs = new HashSet<>();
        watchedAttrs.add(ANY_WATCHED_ATTRIBUTE);
        subscriptionVO.setWatchedAttributes(watchedAttrs);

        MySubscriptionPojo expectedSubscription = new MySubscriptionPojo(ANY_SUBSCRIPTION_ID);
        expectedSubscription.setWatchedAttributes(watchedAttrs);

        assertEquals(expectedSubscription, subscriptionVOMapper.fromSubscriptionVO(subscriptionVO,
                MySubscriptionPojo.class).block(), "Watched attributes property should properly be mapped.");
    }

    @DisplayName("Notification params property should properly be mapped.")
    @Test
    void mapWithNotificationParams() {
        SubscriptionVO subscriptionVO = new SubscriptionVO().id(URI.create(ANY_SUBSCRIPTION_ID))
                .type(SubscriptionVO.Type.SUBSCRIPTION);
        NotificationParamsVO notificationParamsVO = new NotificationParamsVO();
        Set<String> watchedAttrs = new HashSet<>();
        watchedAttrs.add(ANY_WATCHED_ATTRIBUTE);
        notificationParamsVO.setAttributes(watchedAttrs);
        EndpointVO endpointVO = new EndpointVO();
        endpointVO.setUri(URI.create(ANY_ENDPOINT_URI));
        notificationParamsVO.setEndpoint(endpointVO);
        subscriptionVO.setNotification(notificationParamsVO);

        MySubscriptionPojo expectedSubscription = new MySubscriptionPojo(ANY_SUBSCRIPTION_ID);
        MyNotificationParamsProperty myNotificationParamsProperty = new MyNotificationParamsProperty();
        myNotificationParamsProperty.setAttributes(watchedAttrs);
        MyNotificationParamsEndpointProperty endpointProperty = new MyNotificationParamsEndpointProperty();
        endpointProperty.setUri(ANY_ENDPOINT_URI);
        myNotificationParamsProperty.setEndpoint(endpointProperty);
        expectedSubscription.setNotification(myNotificationParamsProperty);

        assertEquals(expectedSubscription, subscriptionVOMapper.fromSubscriptionVO(subscriptionVO,
                MySubscriptionPojo.class).block(), "Notification params property should properly be mapped.");
    }
}
