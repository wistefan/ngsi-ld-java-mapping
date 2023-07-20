package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.*;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode
@ToString
@MappingEnabled(subscriptionType = "Subscription")
public class MySubscriptionPojo {
	private static final String SUBSCRIPTION_TYPE = "Subscription";

	private final URI id;

	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "name")}))
	private String name;

	public MySubscriptionPojo(String id) {
		this.id = URI.create(id);
	}

	@SubscriptionId
	public URI getId() {
		return id;
	}

	@SubscriptionType
	public String getType() {
		return SUBSCRIPTION_TYPE;
	}

	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "expires",
			targetClass = Instant.class)}))
	private Instant expires;

	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "q")}))
	private String q;

	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.GEO_QUERY, targetName = "geoQ",
			targetClass = MyGeoQueryProperty.class)}))
	private MyGeoQueryProperty geoQ = new MyGeoQueryProperty();

	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.ENTITY_INFO_LIST, targetName = "entities",
			targetClass = MyEntityInfoProperty.class)}))
	private List<MyEntityInfoProperty> entities;

	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY_SET, targetName = "watchedAttributes")}))
	private Set<String> watchedAttributes;

	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.NOTIFICATION_PARAMS, targetName = "notification",
			targetClass = MyNotificationParamsProperty.class)}))
	private MyNotificationParamsProperty notification = new MyNotificationParamsProperty();
}
