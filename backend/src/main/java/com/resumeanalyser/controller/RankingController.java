package com.resumeanalyser.controller;

import com.resumeanalyser.dto.RankingEntry;
import com.resumeanalyser.dto.StatsResponse;
import com.resumeanalyser.service.RankingService;
import com.resumeanalyser.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RankingController {

    private final RankingService ranking;
    private final StatsService stats;

    public RankingController(RankingService ranking, StatsService stats) {
        this.ranking = ranking;
        this.stats = stats;
    }

    /** GET /api/ranking/{jobId} - candidates in descending order of match score. */
    @GetMapping("/ranking/{jobId}")
    public List<RankingEntry> ranking(@PathVariable("jobId") Long jobId) {
        return ranking.ranking(jobId);
    }

    /** GET /api/stats - dashboard numbers. */
    @GetMapping("/stats")
    public StatsResponse stats() {
        return stats.stats();
    }
}
