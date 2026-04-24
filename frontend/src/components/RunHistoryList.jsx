import { ChevronRight, Clock3 } from "lucide-react";
import { SectionCard } from "./SectionCard";
import { StatusBadge } from "./StatusBadge";
import { cn, formatDate } from "../lib/utils";

export function RunHistoryList({ runs, activeRunId, isLoading, onSelect }) {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 4 }).map((_, index) => (
          <div key={index} className="h-24 animate-pulse rounded-3xl bg-white/6" />
        ))}
      </div>
    );
  }

  if (runs.length === 0) {
    return (
      <div className="rounded-3xl border border-dashed border-white/12 bg-black/15 p-5 text-sm leading-6 text-slate-400">
        No runs yet. Start the first workflow from the launcher panel.
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {runs.map((run) => (
        <button
          key={run.runId}
          type="button"
          onClick={() => onSelect(run.runId)}
          className={cn(
            "w-full rounded-[24px] border px-4 py-4 text-left transition",
            activeRunId === run.runId
              ? "border-emerald-300/40 bg-emerald-300/10 shadow-[0_20px_50px_rgba(31,137,122,0.2)]"
              : "border-white/10 bg-black/20 hover:border-white/20 hover:bg-white/6",
          )}
        >
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="font-['IBM_Plex_Mono'] text-xs uppercase tracking-[0.24em] text-slate-400">
                {run.regNo}
              </p>
              <p className="mt-2 font-['Space_Grotesk'] text-lg text-white">{run.totalScore} pts</p>
            </div>
            <StatusBadge status={run.status} />
          </div>
          <div className="mt-4 flex items-center justify-between text-sm text-slate-400">
            <span className="inline-flex items-center gap-2">
              <Clock3 className="h-4 w-4" />
              {formatDate(run.createdAt)}
            </span>
            <span className="inline-flex items-center gap-2 text-slate-300">
              View
              <ChevronRight className="h-4 w-4" />
            </span>
          </div>
        </button>
      ))}
    </div>
  );
}
