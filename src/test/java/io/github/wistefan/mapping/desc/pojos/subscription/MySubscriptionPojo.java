package io.github.wistefan.mapping.desc.pojos.subscription;

import io.github.wistefan.mapping.annotations.MappingEnabled;
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
@Getter
@MappingEnabled(entityType = MySubscriptionPojo.ENTITY_TYPE)
public class MySubscriptionPojo {
	public static final String ENTITY_TYPE = "Subscription";

	private final URI id;
	private final String type;

	@Setter
	private String name;

	public MySubscriptionPojo(String id) {
		this.id = URI.create(id);
		this.type = ENTITY_TYPE;
	}

	@Setter
	private Instant expires;

	@Setter
	private String q;

	@Setter
	private List<MyEntityInfoProperty> entities;

	@Setter
	private Set<String> watchedAttributes;

	@Setter
	private MyNotificationParamsProperty notification = new MyNotificationParamsProperty();
}
