import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { DashboardPage } from "./screens/DashboardPage";

vi.mock("../lib/api", () => ({
  fetchRuns: vi.fn().mockResolvedValue([]),
  fetchRun: vi.fn().mockResolvedValue(null),
  startRun: vi.fn().mockResolvedValue({ runId: "run-1", status: "RUNNING" }),
}));

function renderDashboard() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={["/"]}>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/runs/:runId" element={<DashboardPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

test("renders launcher and empty state", async () => {
  renderDashboard();

  expect(await screen.findByText(/Quiz Leaderboard Ops Console/i)).toBeInTheDocument();
  expect(screen.getByText(/Start a run to initialize the validator workflow/i)).toBeInTheDocument();
});

test("submits launcher form", async () => {
  const { startRun } = await import("../lib/api");
  const user = userEvent.setup();
  renderDashboard();

  const input = screen.getByLabelText(/Registration Number/i);
  await user.clear(input);
  await user.type(input, "2024CS101");
  await user.click(screen.getByRole("button", { name: /Start Poll Sequence/i }));

  await waitFor(() => {
    expect(startRun).toHaveBeenCalled();
    expect(startRun.mock.calls[0][0]).toBe("2024CS101");
  });
});
