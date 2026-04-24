const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8081/api";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.detail ?? "Request failed");
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export function fetchRuns() {
  return request("/runs");
}

export function fetchRun(runId) {
  return request(`/runs/${runId}`);
}

export function startRun(regNo) {
  return request("/runs", {
    method: "POST",
    body: JSON.stringify({ regNo }),
  });
}
