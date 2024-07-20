package com.sittingspot.queryoptimizer.models;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record Query(UUID id, Area area, List<Tag> tags, List<String> labels,
                    List<QueryResult> results) implements Serializable {
}