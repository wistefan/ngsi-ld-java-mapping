package io.github.wistefan.mapping.subscription;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fiware.ngsi.model.GeoQueryVO;

public abstract class SubscriptionMixin {
    @JsonIgnore GeoQueryVO geoQ;
}
