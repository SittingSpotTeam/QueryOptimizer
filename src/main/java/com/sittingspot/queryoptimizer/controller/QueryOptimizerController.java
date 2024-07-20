package com.sittingspot.queryoptimizer.controller;

import com.sittingspot.queryoptimizer.models.Area;
import com.sittingspot.queryoptimizer.models.QueryResult;
import com.sittingspot.queryoptimizer.models.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController("/query-optimizer/api/v1")
public class QueryOptimizerController {

    @GetMapping("/")
    public List<QueryResult> getRecordedResult(@RequestParam("queryId") UUID queryId,
                                               @RequestParam("location") Area location,
                                               @RequestParam("tags") List<Tag> tags,
                                               @RequestParam("labels") List<String> labels){

        return List.of();
    }
}
