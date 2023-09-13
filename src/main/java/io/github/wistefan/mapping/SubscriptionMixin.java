package io.github.wistefan.mapping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fiware.ngsi.model.GeoQueryVO;

public abstract class SubscriptionMixin {
    /*
      The reason to ignore this field for now is that TMForum API notifications don't have geo queries,
      so it's done instead of writing custom deserializer for this field, because field types of geo query cannot be
      uniquely deduced
     */
    @JsonIgnore GeoQueryVO geoQ;
}
