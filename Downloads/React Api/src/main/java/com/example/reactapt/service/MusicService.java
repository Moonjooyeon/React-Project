package com.example.reactapt.service;

import com.example.reactapt.controller.ScClient;
import com.example.reactapt.config.DTO.ScPaging;
import com.example.reactapt.config.DTO.ScChartItem;
import com.example.reactapt.config.DTO.ScTrack;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
// import 생략
@Service
public class MusicService {
    private final ScClient sc;
    public MusicService(ScClient sc) { this.sc = sc; }

    public ScPaging<ScChartItem> getTrending(String genre, int limit, String cursor) {
        System.out.println("[SRV] getTrending in, genre=" + genre + ", limit=" + limit + ", cursor=" + cursor);

        if (cursor != null && !cursor.isBlank()) {
            System.out.println("[SRV] -> getAbsolute");
            return sc.getAbsolute(cursor, new ParameterizedTypeReference<ScPaging<ScChartItem>>() {});
        }

        var q = new StringBuilder()
                .append("kind=top")
                .append("&limit=").append(limit)
                .append("&high_tier_only=false")
                .append("&offset=0");
        if (genre != null && !genre.isBlank()) {
            q.append("&genre=soundcloud:genres:").append(genre);
        }

        System.out.println("[SRV] -> sc.get /charts ?" + q);

        try {
            return sc.get("/charts", q.toString(),
                    new ParameterizedTypeReference<ScPaging<ScChartItem>>() {});
        } catch (Exception e) {
            System.err.println("[SRV] charts failed → fallback: " + e.getMessage());
            return fallbackWithTracks(genre, limit);
        }
    }

    private ScPaging<ScChartItem> fallbackWithTracks(String genre, int limit) {
        // ✅ linked_partitioning은 ScClient가 자동으로 붙임. 여기선 붙이지 않음.
        var q = (genre == null || genre.isBlank()) ? "" : genre;
        var query = "q=" + encode(q) + "&limit=" + limit;

        // ✅ /search/tracks 로 대체
        var page = sc.get("/search/tracks", query,
                new ParameterizedTypeReference<ScPaging<ScTrack>>() {});

        var items = (page.collection() == null)
                ? java.util.List.<ScChartItem>of()
                : page.collection().stream()
                .map(t -> new ScChartItem(t))
                .toList();

        // ✅ record는 세터 없음 → 새 인스턴스로 반환
        return new ScPaging<>(
                items,
                page.nextHref(),
                page.queryUrn(),
                page.totalResults()
        );
    }


    public ScPaging<ScTrack> searchTracks(String q, int limit, String cursor) {
        if (cursor != null && !cursor.isBlank()) {
            return sc.getAbsolute(cursor, new ParameterizedTypeReference<ScPaging<ScTrack>>() {});
        }
        var query = "q=" + encode(q) + "&limit=" + limit + "&linked_partitioning=1";
        return sc.get("/search/tracks", query,
                new ParameterizedTypeReference<ScPaging<ScTrack>>() {});
    }

    private static String encode(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}

