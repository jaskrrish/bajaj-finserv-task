import { cn } from "../lib/utils";

const statusClasses = {
  RUNNING: "bg-sky-400/12 text-sky-200 border-sky-300/20",
  COMPLETED: "bg-emerald-400/12 text-emerald-200 border-emerald-300/20",
  FAILED: "bg-rose-400/12 text-rose-200 border-rose-300/20",
};

export function StatusBadge({ status }) {
  return (
    <span
      className={cn(
        "inline-flex rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-[0.24em]",
        statusClasses[status] ?? "bg-white/10 text-slate-200 border-white/10",
      )}
    >
      {status}
    </span>
  );
}
