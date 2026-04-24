import { X } from "lucide-react";

export function PollDrawer({ poll, onClose }) {
  if (!poll) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-slate-950/70 backdrop-blur-sm">
      <div className="h-full w-full max-w-2xl overflow-y-auto border-l border-white/10 bg-[#071018] p-6 shadow-[0_30px_90px_rgba(0,0,0,0.45)]">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-xs uppercase tracking-[0.32em] text-slate-400">Poll Inspection</p>
            <h2 className="mt-2 font-['Space_Grotesk'] text-3xl text-white">Poll {poll.pollIndex}</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full border border-white/10 bg-white/6 p-3 text-slate-300 transition hover:bg-white/10"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="mt-6 grid gap-4">
          <div className="rounded-[26px] border border-white/10 bg-white/6 p-5">
            <p className="text-xs uppercase tracking-[0.24em] text-slate-400">Set ID</p>
            <p className="mt-2 font-['IBM_Plex_Mono'] text-slate-200">{poll.setId ?? "Unknown"}</p>
          </div>
          <div className="rounded-[26px] border border-white/10 bg-black/18 p-5">
            <p className="text-xs uppercase tracking-[0.24em] text-slate-400">Raw payload</p>
            <pre className="mt-4 overflow-x-auto rounded-2xl bg-slate-950/70 p-4 font-['IBM_Plex_Mono'] text-xs leading-6 text-emerald-100">
              {poll.rawPayload}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
}
