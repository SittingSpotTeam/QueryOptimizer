package com.sittingspot.queryoptimizer.DTO;

import com.sittingspot.queryoptimizer.models.Area;
import com.sittingspot.queryoptimizer.models.QueryResult;
import com.sittingspot.queryoptimizer.models.Tag;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record QueryOutDTO(UUID id, Area area, List<Tag> tags, List<String> labels,
                          List<QueryResult> results) implements Serializable {
}