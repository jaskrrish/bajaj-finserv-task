import { useState } from "react";
import { ArrowRight, Sparkles } from "lucide-react";

export function RunLauncher({ onSubmit, isPending, error }) {
  const [regNo, setRegNo] = useState("2024CS101");

  return (
    <div className="rounded-[28px] border border-white/10 bg-slate-950/45 p-5">
      <div className="flex items-center gap-2 text-xs uppercase tracking-[0.32em] text-slate-400">
        <Sparkles className="h-4 w-4 text-emerald-300" />
        Launch Validator Run
      </div>
      <form
        className="mt-4 space-y-4"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit(regNo.trim());
        }}
      >
        <label className="block text-sm text-slate-300">
          <span className="mb-2 block">Registration Number</span>
          <input
            className="w-full rounded-2xl border border-white/10 bg-white/6 px-4 py-3 text-base text-white outline-none transition focus:border-emerald-300/50 focus:bg-white/8"
            value={regNo}
            onChange={(event) => setRegNo(event.target.value)}
            placeholder="2024CS101"
            required
          />
        </label>
        <button
          type="submit"
          disabled={isPending}
          className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-emerald-300 px-4 py-3 font-medium text-slate-950 transition hover:bg-emerald-200 disabled:cursor-not-allowed disabled:bg-emerald-100/50"
        >
          {isPending ? "Starting Run..." : "Start Poll Sequence"}
          <ArrowRight className="h-4 w-4" />
        </button>
      </form>
      {error ? <p className="mt-3 text-sm text-rose-300">{error}</p> : null}
    </div>
  );
}
