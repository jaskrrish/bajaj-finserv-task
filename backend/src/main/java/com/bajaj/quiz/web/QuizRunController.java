package com.bajaj.quiz.web;

import com.bajaj.quiz.dto.ApiDtos;
import com.bajaj.quiz.service.QuizRunAsyncProcessor;
import com.bajaj.quiz.service.QuizRunService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/runs")
public class QuizRunController {

    private final QuizRunService quizRunService;
    private final QuizRunAsyncProcessor quizRunAsyncProcessor;

    public QuizRunController(QuizRunService quizRunService, QuizRunAsyncProcessor quizRunAsyncProcessor) {
        this.quizRunService = quizRunService;
        this.quizRunAsyncProcessor = quizRunAsyncProcessor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiDtos.StartRunResponse startRun(@Valid @RequestBody ApiDtos.StartRunRequest request) {
        ApiDtos.StartRunResponse response = quizRunService.startRun(request.regNo());
        quizRunAsyncProcessor.orchestrate(response.runId());
        return response;
    }

    @GetMapping
    public List<ApiDtos.RunListItem> getRuns() {
        return quizRunService.getRuns();
    }

    @GetMapping("/{runId}")
    public ApiDtos.RunSummaryResponse getRun(@PathVariable UUID runId) {
        return quizRunService.getRun(runId);
    }

    @GetMapping("/{runId}/polls")
    public List<ApiDtos.PollMessageView> getPolls(@PathVariable UUID runId) {
        return quizRunService.getPolls(runId);
    }

    @GetMapping("/{runId}/leaderboard")
    public List<ApiDtos.LeaderboardEntryView> getLeaderboard(@PathVariable UUID runId) {
        return quizRunService.getLeaderboard(runId);
    }

    @GetMapping("/{runId}/submission")
    public ApiDtos.SubmissionRecordView getSubmission(@PathVariable UUID runId) {
        return quizRunService.getSubmission(runId);
    }
}
