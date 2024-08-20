package com.sittingspot.queryoptimizer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sittingspot.queryoptimizer.DTO.QueryOutDTO;
import com.sittingspot.queryoptimizer.models.Area;
import com.sittingspot.queryoptimizer.models.Location;
import com.sittingspot.queryoptimizer.models.QueryResult;
import com.sittingspot.queryoptimizer.models.Tag;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@RestController("/api/v1")
public class QueryOptimizerController {

    @Value("${sittingspot.querydl.url}")
    private String querydlUrl;

    @GetMapping("/")
    public List<QueryResult> getRecordedResult(@RequestParam("x") Double x,
                                               @RequestParam("y") Double y,
                                               @RequestParam("area") Double area,
                                               @RequestParam("tags") List<Tag> tags,
                                               @RequestParam("labels") List<String> labels) throws IOException, InterruptedException {

        var location = new Area(new Location(x,y),area);

        var queryDlRequestUrl = "http://"+ querydlUrl + "/query?x=" + x + "&y=" + y + "&area="+area;
        if(tags != null) {
            queryDlRequestUrl += "&tags=" + tags;
        }
        if(labels != null) {
            queryDlRequestUrl += "&labels=" + labels;
        }
        log.info("Sending request: " + queryDlRequestUrl);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(queryDlRequestUrl))
                .build();
        var result = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Got response code: " + result.statusCode());
        if (result.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        List<QueryOutDTO> data = (new ObjectMapper()).readerForListOf(QueryOutDTO.class).readValue(result.body());

        if (data.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not enough data to optimize query");
        }
        
        // gather all the result removing duplicates
        var ret = new HashSet<QueryResult>();
        for (QueryOutDTO query : data) {
            ret.addAll(query.results());
        }

        return ret.stream().toList();
    }
}
