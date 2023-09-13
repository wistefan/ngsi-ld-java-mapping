package io.github.wistefan.mapping.desc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wistefan.mapping.JavaObjectMapper;
import io.github.wistefan.mapping.desc.pojos.subscription.MyNotificationParamsEndpointProperty;
import io.github.wistefan.mapping.desc.pojos.subscription.MyNotificationParamsProperty;
import io.github.wistefan.mapping.desc.pojos.subscription.MySubscriptionPojo;
import io.github.wistefan.mapping.SubscriptionMixin;
import org.fiware.ngsi.model.SubscriptionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaObjectMapperSubscriptionTest {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private JavaObjectMapper javaObjectMapper;


	@BeforeEach
	public void setup() {
		javaObjectMapper = new JavaObjectMapper();
		OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		OBJECT_MAPPER.addMixIn(SubscriptionVO.class, SubscriptionMixin.class);
	}

	@DisplayName("Query mapping")
	@Test
	void testQueryMapping() {
		MySubscriptionPojo myPojo = createSubscription();

		assertEquals(myPojo.getQ(), javaObjectMapper.toSubscriptionVO(myPojo).getQ(),
				"The pojo should have the same query");
	}

	@DisplayName("Notification endpoint mapping")
	@Test
	void testNotificationEndpointMapping() {
		MySubscriptionPojo myPojo = createSubscription();

		assertEquals(myPojo.getNotification().getEndpoint().getUri(), javaObjectMapper.toSubscriptionVO(myPojo).getNotification().getEndpoint().getUri(),
				"The pojo should have the same notification endpoint");
	}

	private MySubscriptionPojo createSubscription() {
		MySubscriptionPojo myPojo = new MySubscriptionPojo("urn:ngsi-ld:my-pojo:the-test-pojo");
		myPojo.setQ("eventType=custom");
		myPojo.setNotification(createNotification());

		return myPojo;
	}

	private MyNotificationParamsEndpointProperty createEndpoint() {
		MyNotificationParamsEndpointProperty endpointProperty = new MyNotificationParamsEndpointProperty();
		endpointProperty.setUri(URI.create("test.com"));
		endpointProperty.setAccept("application/ld+json");
		return endpointProperty;
	}

	private MyNotificationParamsProperty createNotification() {
		MyNotificationParamsProperty notificationParamsProperty = new MyNotificationParamsProperty();
		notificationParamsProperty.setEndpoint(createEndpoint());
		notificationParamsProperty.setFormat("keyValues");
		return notificationParamsProperty;
	}

}