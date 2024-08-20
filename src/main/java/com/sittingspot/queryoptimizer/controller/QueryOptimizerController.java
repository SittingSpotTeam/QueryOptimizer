package com.sittingspot.queryoptimizer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sittingspot.queryoptimizer.DTO.QueryOutDTO;
import com.sittingspot.queryoptimizer.models.Area;
import com.sittingspot.queryoptimizer.models.Location;
import com.sittingspot.queryoptimizer.models.QueryResult;
import com.sittingspot.queryoptimizer.models.Tag;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.buf.UriUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api/v1")
public class QueryOptimizerController {

    @Value("${sittingspot.querydl.url}")
    private String querydlUrl;

    @GetMapping
    public List<QueryResult> getRecordedResult(@RequestParam("x") Double x,
                                               @RequestParam("y") Double y,
                                               @RequestParam("area") Double area,
                                               @RequestParam(value = "tags",required = false) List<Tag> tags,
                                               @RequestParam(value = "labels",required = false) List<String> labels) throws IOException, InterruptedException {

        var location = new Area(new Location(x,y),area);

        var queryDlRequestUrl = "http://"+ querydlUrl + "?x=" + x + "&y=" + y + "&area="+area;

        if(tags != null){
            queryDlRequestUrl += URLEncoder.encode("&tags="+tags,"UTF-8");
        }
        if(labels != null){
            queryDlRequestUrl += URLEncoder.encode("&labels="+labels,"UTF-8");
        }

        log.info("Sending request: " + queryDlRequestUrl);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(queryDlRequestUrl))
                .header("Content-Type", "application/json")
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

        // filter out spots that are outside the required area and return the result
        return ret.stream().filter(e -> distFrom(e.location().y(),e.location().x(), y,x) < area/1000 ).toList();
    }

    //return distance between two coordinates in km
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // miles (or 6371.0 kilometers)
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }
}
