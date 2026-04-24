import clsx from "clsx";

export function cn(...values) {
  return clsx(values);
}

export function formatDate(date) {
  if (!date) {
    return "Pending";
  }

  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(date));
}

export function formatDuration(startedAt, completedAt) {
  if (!startedAt) {
    return "--";
  }

  const end = completedAt ? new Date(completedAt) : new Date();
  const diffSeconds = Math.max(0, Math.round((end - new Date(startedAt)) / 1000));
  const minutes = Math.floor(diffSeconds / 60);
  const seconds = diffSeconds % 60;
  return `${minutes}m ${seconds}s`;
}
