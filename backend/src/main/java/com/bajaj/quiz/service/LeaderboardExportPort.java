package com.bajaj.quiz.service;

import com.bajaj.quiz.dto.ApiDtos;

public interface LeaderboardExportPort {

    void export(ApiDtos.RunSummaryResponse summaryResponse);
}
