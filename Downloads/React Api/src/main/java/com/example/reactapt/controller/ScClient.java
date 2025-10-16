package com.example.reactapt.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
// src/main/java/com/example/reactapt/controller/ScClient.java
@Component
public class ScClient {
    private static final String BASE = "https://api-v2.soundcloud.com";
    private final WebClient http;
    private final String clientId;

    public ScClient(@Value("${soundcloud.client-id}") String clientId) {
        this.clientId = clientId;
        System.out.println("[CONFIG] SoundCloud client_id = " + clientId);
        System.out.println("[CLASS] ScClient@" + System.identityHashCode(this)
                + " from " + ScClient.class.getProtectionDomain().getCodeSource().getLocation());

        this.http = WebClient.builder()
                .baseUrl("https://api-v2.soundcloud.com")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader("Origin", "https://soundcloud.com")
                .defaultHeader("Referer", "https://soundcloud.com/")
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build();



    }

    public <T> T get(String path, String query, ParameterizedTypeReference<T> type) {
        var b = UriComponentsBuilder.fromHttpUrl(BASE)
                .path(path)
                .queryParam("client_id", clientId)
                .queryParam("linked_partitioning", "1")
                .queryParam("app_locale", "en")
                .queryParam("app_version", "1760349581");
        if (query != null && !query.isBlank()) b.query(query);

        var url = b.build(true).toUriString();
        System.out.println("[SC GET] " + url);

        return http.get().uri(url)
                .retrieve()
                .onStatus(s -> !s.is2xxSuccessful(), resp ->
                        resp.bodyToMono(String.class)
                                .doOnNext(body -> System.err.println("[SC BODY] " + body))
                                .then(reactor.core.publisher.Mono.error(new RuntimeException("SC " + resp.statusCode())))
                )
                .bodyToMono(type)
                .block();
    }


    public <T> T getAbsolute(String nextHref, ParameterizedTypeReference<T> type) {
        var b = UriComponentsBuilder.fromHttpUrl(nextHref);
        var qp = b.build(true).getQueryParams();
        if (!qp.containsKey("client_id")) b.queryParam("client_id", clientId);
        if (!qp.containsKey("linked_partitioning")) b.queryParam("linked_partitioning", "1");
        String url = b.build(true).toUriString();

        System.out.println("[SC ABS] " + url);
        try {
            return http.get().uri(url).retrieve().bodyToMono(type).block();
        } catch (Exception e) {
            System.err.println("[SC ABS ERR] " + url);
            e.printStackTrace();
            throw e;
        }
    }
}
