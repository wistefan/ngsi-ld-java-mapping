package io.github.wistefan.mapping.desc.pojos;

import io.github.wistefan.mapping.annotations.AttributeGetter;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.AttributeType;
import io.github.wistefan.mapping.annotations.MappingEnabled;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString
@MappingEnabled(entityType = ProductSpecification.TYPE_PRODUCT_SPECIFICATION)
public class ProductSpecification extends EntityWithId {

    public static final String TYPE_PRODUCT_SPECIFICATION = "product-specification";

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "href")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "href")}))
    private URI href;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "brand")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "brand")}))
    private String brand;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "description")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "description")}))
    private String description;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "isBundle")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "isBundle")}))
    private Boolean isBundle;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "lastUpdate")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "lastUpdate")}))
    private Instant lastUpdate;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "lifecycleStatus")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "lifecycleStatus")}))
    private String lifecycleStatus;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "name")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "name")}))
    private String name;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "productNumber")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "productNumber")}))
    private String productNumber;

    @Getter(onMethod = @__({@AttributeGetter(value = AttributeType.PROPERTY, targetName = "version")}))
    @Setter(onMethod = @__({@AttributeSetter(value = AttributeType.PROPERTY, targetName = "version")}))
    private String version;

    @Getter(onMethod = @__({
            @AttributeGetter(value = AttributeType.PROPERTY_LIST, targetName = "productSpecCharacteristic")}))
    @Setter(onMethod = @__({
            @AttributeSetter(value = AttributeType.PROPERTY_LIST, targetName = "productSpecCharacteristic", targetClass = ProductSpecificationCharacteristic.class)}))
    private List<ProductSpecificationCharacteristic> productSpecCharacteristic;


    public ProductSpecification(String id) {
        super(TYPE_PRODUCT_SPECIFICATION, id);
    }

    @Override
    public String getEntityState() {
        return lifecycleStatus;
    }
}
