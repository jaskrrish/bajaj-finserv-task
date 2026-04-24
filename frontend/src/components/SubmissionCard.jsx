import { CheckCircle2, FileDigit, Send } from "lucide-react";
import { SectionCard } from "./SectionCard";

export function SubmissionCard({ submission, status, createdAt }) {
  return (
    <SectionCard title="Submission Receipt" eyebrow="One-shot Handoff">
      <div className="space-y-4">
        <div className="rounded-[26px] border border-white/10 bg-black/20 p-5">
          <div className="flex items-center gap-2 text-slate-200">
            <Send className="h-5 w-5 text-emerald-300" />
            Validator Submit State
          </div>
          {submission ? (
            <div className="mt-4 space-y-4">
              <div className="rounded-3xl border border-emerald-300/15 bg-emerald-300/8 p-4">
                <div className="flex items-center gap-2 text-emerald-100">
                  <CheckCircle2 className="h-5 w-5" />
                  {submission.message}
                </div>
                <p className="mt-3 text-sm leading-6 text-slate-200">
                  Submitted total: {submission.submittedTotal} | Expected total: {submission.expectedTotal ?? "n/a"}
                </p>
              </div>
              <div className="grid gap-3 text-sm text-slate-300">
                <div className="rounded-3xl border border-white/10 bg-white/6 p-4">
                  <p>Idempotent: {String(submission.idempotent)}</p>
                  <p className="mt-2">Correct: {String(submission.correct)}</p>
                </div>
                <div className="rounded-3xl border border-white/10 bg-white/6 p-4">
                  <p className="text-xs uppercase tracking-[0.24em] text-slate-400">Payloads archived</p>
                  <p className="mt-2 leading-6 text-slate-300">
                    Request and response bodies are stored in the backend for reviewer auditability and future Salesforce syncing.
                  </p>
                </div>
              </div>
            </div>
          ) : (
            <div className="mt-4 rounded-3xl border border-dashed border-white/10 px-4 py-5 text-sm leading-6 text-slate-400">
              {status === "FAILED"
                ? "Submission was skipped because the run failed before completion."
                : "Waiting for leaderboard completion before the backend submits once."}
            </div>
          )}
        </div>
        <div className="rounded-[26px] border border-white/10 bg-white/6 p-5">
          <div className="flex items-center gap-2 text-slate-200">
            <FileDigit className="h-5 w-5 text-sky-300" />
            Audit Context
          </div>
          <p className="mt-3 text-sm leading-6 text-slate-300">
            Run created at {createdAt}. The backend owns submit timing, so repeated UI interaction cannot trigger a duplicate handoff.
          </p>
        </div>
      </div>
    </SectionCard>
  );
}
