import { Trophy } from "lucide-react";
import { SectionCard } from "./SectionCard";

export function LeaderboardPanel({ leaderboard, totalScore }) {
  return (
    <SectionCard title="Leaderboard" eyebrow="Computed Output">
      <div className="rounded-[26px] border border-white/10 bg-black/20 p-5">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-2 text-slate-200">
            <Trophy className="h-5 w-5 text-amber-300" />
            Final Ranking
          </div>
          <div className="text-right">
            <p className="text-xs uppercase tracking-[0.24em] text-slate-400">Total</p>
            <p className="font-['Space_Grotesk'] text-2xl text-white">{totalScore}</p>
          </div>
        </div>
        <div className="mt-4 space-y-3">
          {leaderboard.length === 0 ? (
            <div className="rounded-3xl border border-dashed border-white/10 px-4 py-5 text-sm text-slate-400">
              Leaderboard will appear after the poll sequence completes.
            </div>
          ) : (
            leaderboard.map((entry) => (
              <div
                key={entry.rank + entry.participant}
                className="grid grid-cols-[60px_1fr_auto] items-center gap-3 rounded-[22px] border border-white/10 bg-white/6 px-4 py-4"
              >
                <span className="font-['IBM_Plex_Mono'] text-sm text-slate-400">#{entry.rank}</span>
                <span className="font-medium text-white">{entry.participant}</span>
                <span className="font-['Space_Grotesk'] text-2xl text-emerald-200">{entry.totalScore}</span>
              </div>
            ))
          )}
        </div>
      </div>
    </SectionCard>
  );
}
