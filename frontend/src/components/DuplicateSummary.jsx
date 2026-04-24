import { ShieldAlert } from "lucide-react";
import { SectionCard } from "./SectionCard";

export function DuplicateSummary({ run }) {
  const suppressionRate =
    run.uniqueEvents + run.duplicateEvents === 0
      ? 0
      : Math.round((run.duplicateEvents / (run.uniqueEvents + run.duplicateEvents)) * 100);

  return (
    <SectionCard title="Deduplication" eyebrow="Signal Integrity">
      <div className="space-y-4">
        <div className="rounded-[26px] border border-white/10 bg-black/22 p-5">
          <div className="flex items-center gap-2 text-slate-300">
            <ShieldAlert className="h-5 w-5 text-amber-300" />
            Duplicate Suppression Summary
          </div>
          <div className="mt-5 grid gap-3 sm:grid-cols-2">
            <div className="rounded-3xl border border-white/10 bg-white/6 p-4">
              <p className="text-sm text-slate-400">Unique events</p>
              <p className="mt-2 font-['Space_Grotesk'] text-3xl text-white">{run.uniqueEvents}</p>
            </div>
            <div className="rounded-3xl border border-white/10 bg-white/6 p-4">
              <p className="text-sm text-slate-400">Dropped duplicates</p>
              <p className="mt-2 font-['Space_Grotesk'] text-3xl text-white">{run.duplicateEvents}</p>
            </div>
          </div>
        </div>
        <div className="rounded-[26px] border border-amber-300/15 bg-amber-300/8 p-5">
          <p className="text-sm leading-6 text-amber-100">
            {suppressionRate}% of observed events were suppressed as duplicate deliveries. The backend uniqueness guard is the final line of defense, so retries still stay safe.
          </p>
        </div>
      </div>
    </SectionCard>
  );
}
