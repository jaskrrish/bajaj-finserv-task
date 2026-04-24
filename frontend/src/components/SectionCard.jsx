import { cn } from "../lib/utils";

export function SectionCard({ title, eyebrow, children, className }) {
  return (
    <section
      className={cn(
        "rounded-[28px] border border-white/10 bg-white/6 p-5 shadow-[0_24px_60px_rgba(0,0,0,0.32)] backdrop-blur-xl",
        className,
      )}
    >
      {(eyebrow || title) && (
        <header className="mb-5 flex items-start justify-between gap-3">
          <div>
            {eyebrow ? (
              <p className="text-[11px] uppercase tracking-[0.32em] text-slate-400">
                {eyebrow}
              </p>
            ) : null}
            {title ? (
              <h2 className="mt-2 font-['Space_Grotesk'] text-xl font-medium text-white">
                {title}
              </h2>
            ) : null}
          </div>
        </header>
      )}
      {children}
    </section>
  );
}
