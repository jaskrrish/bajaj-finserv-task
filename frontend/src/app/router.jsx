import { createBrowserRouter } from "react-router-dom";
import { DashboardPage } from "./screens/DashboardPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <DashboardPage />,
  },
  {
    path: "/runs/:runId",
    element: <DashboardPage />,
  },
]);
