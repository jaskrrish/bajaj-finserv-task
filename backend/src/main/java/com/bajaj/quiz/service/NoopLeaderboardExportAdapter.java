package com.bajaj.quiz.service;

import com.bajaj.quiz.dto.ApiDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopLeaderboardExportAdapter implements LeaderboardExportPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoopLeaderboardExportAdapter.class);

    @Override
    public void export(ApiDtos.RunSummaryResponse summaryResponse) {
        LOGGER.info("Salesforce export adapter not enabled for run {}", summaryResponse.runId());
    }
}
