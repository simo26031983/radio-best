import { handleGetStations } from "./handlers/stations.js";
import type { Env } from "./types.js";

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    if (url.pathname === "/api/stations" && request.method === "GET") {
      return handleGetStations(request, env);
    }

    return new Response("Not found", { status: 404 });
  },
};
