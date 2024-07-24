package com.sittingspot.queryoptimizer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sittingspot.queryoptimizer.DTO.QueryOutDTO;
import com.sittingspot.queryoptimizer.models.Area;
import com.sittingspot.queryoptimizer.models.QueryResult;
import com.sittingspot.queryoptimizer.models.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@RestController("/query-optimizer/api/v1")
public class QueryOptimizerController {

    @Value("${sittingspot.querydl.host}")
    private String querydlHost;

    @Value("${sittingspot.querydl.port}")
    private String querydlPort;

    @GetMapping("/")
    public List<QueryResult> getRecordedResult(@RequestParam("queryId") UUID queryId,
                                               @RequestParam("location") Area location,
                                               @RequestParam("tags") List<Tag> tags,
                                               @RequestParam("labels") List<String> labels) throws IOException, InterruptedException {

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+ querydlHost +":"+ querydlPort +"/?location="+location+"&tags="+tags+"&labels="+labels))
                .build();
        var result = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (result.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        List<QueryOutDTO> data = (new ObjectMapper()).readerForListOf(QueryOutDTO.class).readValue(result.body());

        if (data.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not enough data to optimize query");
        }

        // remove possible entry of the query to optimize
        data.removeIf(x -> x.id() == queryId);
        // gather all the result removing duplicates
        var ret = new HashSet<QueryResult>();
        for (QueryOutDTO query : data) {
            ret.addAll(query.results());
        }

        return ret.stream().toList();
    }
}
