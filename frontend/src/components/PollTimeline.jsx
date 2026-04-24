import { Eye, TimerReset } from "lucide-react";
import { SectionCard } from "./SectionCard";

export function PollTimeline({ polls, status, onInspect }) {
  return (
    <SectionCard title="Poll Timeline" eyebrow="Validator Responses">
      <div className="space-y-3">
        {Array.from({ length: 10 }).map((_, pollIndex) => {
          const poll = polls.find((item) => item.pollIndex === pollIndex);
          const state = poll ? "received" : status === "FAILED" ? "failed" : "pending";

          return (
            <div
              key={pollIndex}
              className="flex items-center justify-between gap-4 rounded-[22px] border border-white/10 bg-black/18 px-4 py-4"
            >
              <div>
                <p className="font-['IBM_Plex_Mono'] text-xs uppercase tracking-[0.24em] text-slate-400">
                  Poll {pollIndex}
                </p>
                <p className="mt-2 font-['Space_Grotesk'] text-xl text-white">
                  {state === "received" ? `${poll.eventsCount} events captured` : state === "failed" ? "Run failed" : "Awaiting response"}
                </p>
              </div>
              <div className="flex items-center gap-2">
                {poll ? (
                  <button
                    type="button"
                    onClick={() => onInspect(poll)}
                    className="inline-flex items-center gap-2 rounded-full border border-emerald-300/20 bg-emerald-300/10 px-3 py-2 text-sm text-emerald-100 transition hover:bg-emerald-300/16"
                  >
                    <Eye className="h-4 w-4" />
                    Inspect
                  </button>
                ) : (
                  <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/6 px-3 py-2 text-sm text-slate-400">
                    <TimerReset className="h-4 w-4" />
                    Pending
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </SectionCard>
  );
}
