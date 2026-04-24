import { ShieldCheck } from "lucide-react";
import { SectionCard } from "./SectionCard";
import { StatusBadge } from "./StatusBadge";
import { formatDate } from "../lib/utils";

export function RunHero({ run, metrics }) {
  return (
    <SectionCard title="Active Run" eyebrow="Live Validator State">
      <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <div className="space-y-4">
          <div className="flex flex-wrap items-center gap-3">
            <StatusBadge status={run.status} />
            <span className="rounded-full border border-white/10 bg-white/8 px-3 py-1 font-['IBM_Plex_Mono'] text-xs uppercase tracking-[0.24em] text-slate-300">
              {run.setId ?? "Awaiting Set ID"}
            </span>
          </div>
          <div>
            <p className="font-['IBM_Plex_Mono'] text-xs uppercase tracking-[0.3em] text-slate-400">
              Registration
            </p>
            <h2 className="mt-2 font-['Space_Grotesk'] text-4xl font-semibold text-white">
              {run.regNo}
            </h2>
          </div>
          <div className="grid gap-3 md:grid-cols-3">
            {metrics.map((metric) => (
              <div key={metric.label} className="rounded-[24px] border border-white/10 bg-black/20 p-4">
                <metric.icon className="h-5 w-5 text-emerald-300" />
                <p className="mt-4 text-sm text-slate-400">{metric.label}</p>
                <p className="mt-1 font-['Space_Grotesk'] text-2xl text-white">{metric.value}</p>
              </div>
            ))}
          </div>
        </div>
        <div className="rounded-[28px] border border-emerald-300/15 bg-emerald-300/8 p-5">
          <div className="flex items-center gap-2 text-sm text-emerald-100">
            <ShieldCheck className="h-5 w-5" />
            Assignment Guarantees
          </div>
          <ul className="mt-4 space-y-3 text-sm leading-6 text-slate-200">
            <li>Exactly 10 polls executed in order from 0 to 9.</li>
            <li>Duplicate events suppressed by `roundId + participant`.</li>
            <li>Leaderboard persisted before the one-shot submit call.</li>
            <li>Submission receipt retained for audit and future Salesforce export.</li>
          </ul>
          <div className="mt-5 border-t border-white/10 pt-4 text-xs uppercase tracking-[0.24em] text-slate-400">
            Started {formatDate(run.createdAt)}
          </div>
        </div>
      </div>
    </SectionCard>
  );
}
