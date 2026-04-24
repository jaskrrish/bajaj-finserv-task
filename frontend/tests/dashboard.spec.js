import { test, expect } from "@playwright/test";

test("renders dashboard with mocked run details", async ({ page }) => {
  await page.route("**/api/runs", async (route) => {
    if (route.request().method() === "POST") {
      await route.fulfill({
        status: 202,
        contentType: "application/json",
        body: JSON.stringify({ runId: "run-1", status: "RUNNING" }),
      });
      return;
    }

    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify([
        {
          runId: "run-1",
          regNo: "2024CS101",
          status: "COMPLETED",
          pollsCompleted: 10,
          uniqueEvents: 3,
          duplicateEvents: 1,
          totalScore: 60,
          createdAt: "2026-04-24T12:30:00Z",
          completedAt: "2026-04-24T12:31:00Z"
        }
      ]),
    });
  });

  await page.route("**/api/runs/run-1", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        runId: "run-1",
        regNo: "2024CS101",
        setId: "SET_1",
        status: "COMPLETED",
        pollsCompleted: 10,
        uniqueEvents: 3,
        duplicateEvents: 1,
        totalScore: 60,
        failureReason: null,
        createdAt: "2026-04-24T12:30:00Z",
        updatedAt: "2026-04-24T12:31:00Z",
        completedAt: "2026-04-24T12:31:00Z",
        polls: [
          {
            id: "poll-1",
            pollIndex: 0,
            setId: "SET_1",
            eventsCount: 2,
            rawPayload: "{\"events\":[{\"participant\":\"Alice\"}]}",
            receivedAt: "2026-04-24T12:30:05Z"
          }
        ],
        dedupedEvents: [],
        leaderboard: [
          { rank: 1, participant: "Alice", totalScore: 40 },
          { rank: 2, participant: "Bob", totalScore: 20 }
        ],
        submission: {
          correct: true,
          idempotent: true,
          submittedTotal: 60,
          expectedTotal: 60,
          message: "Correct!",
          submittedAt: "2026-04-24T12:31:00Z",
          requestPayload: "{}",
          responsePayload: "{}"
        }
      }),
    });
  });

  await page.goto("/");

  await expect(page.getByText("Quiz Leaderboard Ops Console")).toBeVisible();
  await expect(page.getByText("Alice")).toBeVisible();
  await expect(page.getByText("Correct!")).toBeVisible();
});
