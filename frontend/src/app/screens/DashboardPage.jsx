import { useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useNavigate, useParams } from "react-router-dom";
import { AlertCircle, BarChart3, DatabaseZap, Radar } from "lucide-react";
import { fetchRun, fetchRuns, startRun } from "../../lib/api";
import { queryClient } from "../queryClient";
import { formatDate, formatDuration } from "../../lib/utils";
import { RunLauncher } from "../../components/RunLauncher";
import { RunHistoryList } from "../../components/RunHistoryList";
import { SectionCard } from "../../components/SectionCard";
import { RunHero } from "../../components/RunHero";
import { DuplicateSummary } from "../../components/DuplicateSummary";
import { PollTimeline } from "../../components/PollTimeline";
import { LeaderboardPanel } from "../../components/LeaderboardPanel";
import { SubmissionCard } from "../../components/SubmissionCard";
import { PollDrawer } from "../../components/PollDrawer";

function getSelectedRunId(runId, runs) {
  if (runId) {
    return runId;
  }

  return runs?.[0]?.runId ?? null;
}

export function DashboardPage() {
  const navigate = useNavigate();
  const { runId } = useParams();
  const [selectedPoll, setSelectedPoll] = useState(null);

  const runsQuery = useQuery({
    queryKey: ["runs"],
    queryFn: fetchRuns,
    refetchInterval: (query) =>
      query.state.data?.some((run) => run.status === "RUNNING") ? 3_000 : false,
  });

  const activeRunId = getSelectedRunId(runId, runsQuery.data);

  const runQuery = useQuery({
    queryKey: ["run", activeRunId],
    queryFn: () => fetchRun(activeRunId),
    enabled: Boolean(activeRunId),
    refetchInterval: (query) =>
      query.state.data?.status === "RUNNING" ? 2_000 : false,
  });

  const startRunMutation = useMutation({
    mutationFn: startRun,
    onSuccess: async (response) => {
      await queryClient.invalidateQueries({ queryKey: ["runs"] });
      navigate(`/runs/${response.runId}`);
    },
  });

  const selectedRun = runQuery.data;
  const metrics = useMemo(() => {
    if (!selectedRun) {
      return [];
    }

    return [
      {
        label: "Elapsed",
        value: formatDuration(selectedRun.createdAt, selectedRun.completedAt),
        icon: Radar,
      },
      {
        label: "Poll Progress",
        value: `${selectedRun.pollsCompleted}/10`,
        icon: DatabaseZap,
      },
      {
        label: "Total Score",
        value: selectedRun.totalScore,
        icon: BarChart3,
      },
    ];
  }, [selectedRun]);

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(27,81,80,0.35),_transparent_30%),linear-gradient(180deg,_#07111a_0%,_#08131d_32%,_#03070b_100%)] text-slate-50">
      <div className="mx-auto flex min-h-screen max-w-[1600px] flex-col gap-6 px-4 py-6 lg:px-6">
        <header className="grid gap-4 rounded-[32px] border border-white/10 bg-white/6 p-5 shadow-[0_30px_80px_rgba(0,0,0,0.35)] backdrop-blur-xl lg:grid-cols-[1.25fr_0.75fr]">
          <div className="space-y-4">
            <div className="inline-flex items-center gap-2 rounded-full border border-emerald-300/20 bg-emerald-300/10 px-3 py-1 text-xs font-medium uppercase tracking-[0.32em] text-emerald-200">
              Bajaj Finserv Health
            </div>
            <div className="space-y-3">
              <h1 className="max-w-2xl font-['Space_Grotesk'] text-4xl font-semibold tracking-tight text-white md:text-5xl">
                Quiz Leaderboard Ops Console
              </h1>
              <p className="max-w-2xl text-sm leading-7 text-slate-300 md:text-base">
                Monitor ten delayed validator polls, inspect duplicate suppression, and verify the one-shot leaderboard submission without losing the audit trail.
              </p>
            </div>
          </div>
          <RunLauncher
            isPending={startRunMutation.isPending}
            error={startRunMutation.error?.message}
            onSubmit={(regNo) => startRunMutation.mutate(regNo)}
          />
        </header>

        <section className="grid flex-1 gap-6 xl:grid-cols-[280px_minmax(0,1fr)_360px]">
          <SectionCard title="Run History" eyebrow="Execution Ledger" className="min-h-[420px]">
            <RunHistoryList
              runs={runsQuery.data ?? []}
              isLoading={runsQuery.isLoading}
              activeRunId={activeRunId}
              onSelect={(nextRunId) => navigate(`/runs/${nextRunId}`)}
            />
          </SectionCard>

          <div className="grid gap-6">
            {selectedRun ? (
              <>
                <RunHero run={selectedRun} metrics={metrics} />
                <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
                  <PollTimeline
                    polls={selectedRun.polls}
                    status={selectedRun.status}
                    onInspect={setSelectedPoll}
                  />
                  <DuplicateSummary run={selectedRun} />
                </div>
              </>
            ) : (
              <SectionCard title="No Run Selected" eyebrow="Startup State">
                <div className="flex min-h-[280px] flex-col items-center justify-center gap-3 text-center text-slate-400">
                  <AlertCircle className="h-10 w-10 text-amber-300" />
                  <p className="font-['Space_Grotesk'] text-xl text-white">Start a run to initialize the validator workflow.</p>
                  <p className="max-w-md text-sm leading-6">
                    Once a run is started, the console will track poll progress, duplicate suppression, leaderboard generation, and the submission receipt in one place.
                  </p>
                </div>
              </SectionCard>
            )}
          </div>

          <div className="grid gap-6">
            <LeaderboardPanel leaderboard={selectedRun?.leaderboard ?? []} totalScore={selectedRun?.totalScore ?? 0} />
            <SubmissionCard submission={selectedRun?.submission} status={selectedRun?.status} createdAt={formatDate(selectedRun?.createdAt)} />
          </div>
        </section>
      </div>

      <PollDrawer poll={selectedPoll} onClose={() => setSelectedPoll(null)} />
    </main>
  );
}
