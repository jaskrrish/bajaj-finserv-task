package com.bajaj.quiz.service;

import com.bajaj.quiz.client.ValidatorModels;
import com.bajaj.quiz.entity.DedupedEvent;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LeaderboardCalculator {

    public Map<String, Integer> aggregateScores(List<DedupedEvent> events) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (DedupedEvent event : events) {
            totals.merge(event.getParticipant(), event.getScore(), Integer::sum);
        }
        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    public List<ValidatorModels.LeaderboardItem> toSubmissionLeaderboard(Map<String, Integer> totals) {
        return totals.entrySet().stream()
                .map(entry -> new ValidatorModels.LeaderboardItem(entry.getKey(), entry.getValue()))
                .toList();
    }
}
