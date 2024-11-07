package io.github.wistefan.mapping.desc.pojos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Data
public class MyLocation {
    public String type="Point";
    public double[] coordinates;
}
