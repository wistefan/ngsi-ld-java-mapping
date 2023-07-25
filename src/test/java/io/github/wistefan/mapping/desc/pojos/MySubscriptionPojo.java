package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "name")}))
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

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "expires")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "expires",
			targetClass = Instant.class)}))
	private Instant expires;

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "q")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "q")}))
	private String q;

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.GEO_QUERY, targetName = "geoQ")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.GEO_QUERY, targetName = "geoQ",
			targetClass = MyGeoQueryProperty.class)}))
	private MyGeoQueryProperty geoQ = new MyGeoQueryProperty();

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.ENTITY_INFO_LIST, targetName = "entities")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.ENTITY_INFO_LIST, targetName = "entities",
			targetClass = MyEntityInfoProperty.class)}))
	private List<MyEntityInfoProperty> entities;

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY_SET, targetName = "watchedAttributes")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY_SET, targetName = "watchedAttributes")}))
	private Set<String> watchedAttributes;

	@Getter(onMethod = @__({@AttributeGetter(value = AttributeType.NOTIFICATION_PARAMS, targetName = "notification")}))
	@Setter(onMethod = @__({@AttributeSetter(value = AttributeType.NOTIFICATION_PARAMS, targetName = "notification",
			targetClass = MyNotificationParamsProperty.class)}))
	private MyNotificationParamsProperty notification = new MyNotificationParamsProperty();
}
