package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@ToString
@Data
public class MyGeoQueryProperty {

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "georel")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "georel")}))
    private String georel;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "coordinates")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "coordinates")}))
    private List<List<Double>> coordinates = new ArrayList<>();

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "geometry")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "geometry")}))
    private String geometry;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "geoproperty")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "geoproperty")}))
    private String geoproperty;
}
