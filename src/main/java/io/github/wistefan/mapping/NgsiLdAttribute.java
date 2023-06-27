package io.github.wistefan.mapping;

import java.util.List;

public record NgsiLdAttribute(List<String> path, QueryAttributeType type) {
}
