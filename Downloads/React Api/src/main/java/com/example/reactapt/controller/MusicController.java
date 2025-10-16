// src/main/java/com/example/reactapt/controller/MusicController.java
package com.example.reactapt.controller;

import com.example.reactapt.config.DTO.ScChartItem;
import com.example.reactapt.config.DTO.ScPaging;
import com.example.reactapt.config.DTO.ScTrack;
import com.example.reactapt.service.MusicService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MusicController {

    private final MusicService svc;
    public MusicController(MusicService svc) { this.svc = svc; }

    // ✅ 차트: /api/charts/trending
    @GetMapping("/charts/trending")
    public ScPaging<ScChartItem> trending(
            @RequestParam(defaultValue = "") String genre,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor
    ) {
        System.out.println("[CTRL] trending genre=" + genre + ", limit=" + limit + ", cursor=" + cursor);
        var r = svc.getTrending(genre, limit, cursor);   // ✅ 인스턴스 호출
        System.out.println("[CTRL] trending OK: collection=" + (r == null ? -1 : r.collection().size()));
        return r;
    }

    // ✅ 검색: /api/search
    @GetMapping("/search")
    public ScPaging<ScTrack> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor
    ) {
        return svc.searchTracks(q, limit, cursor);
    }
    @GetMapping("/ping")
    public String ping() {
        System.out.println("[CTRL] ping");
        return "ok";
    }

}
