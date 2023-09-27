package io.github.wistefan.mapping.desc.pojos;

import lombok.Data;

import java.net.URI;

@Data
public class ProductSpecificationCharacteristic {

    private String id;
    private Boolean configurable;
    private String description;
    private Boolean extensible;
    private Boolean isUnique;
    private Integer maxCardinality;
    private Integer minCardinality;
    private String name;
    private String regex;
    private String valueType;
    private String atValueSchemaLocation;
    private String atBaseType;
    private URI atSchemaLocation;
    private String atType;
}
