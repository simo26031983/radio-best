import type { CountryCode, Env } from "../types.js";

const VALID_COUNTRIES: CountryCode[] = ["FR", "MA"];

export async function handleGetStations(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);
  const country = url.searchParams.get("country")?.toUpperCase();

  if (!country || !VALID_COUNTRIES.includes(country as CountryCode)) {
    return Response.json(
      { error: "Invalid or missing 'country' query param. Expected 'FR' or 'MA'." },
      { status: 400 }
    );
  }

  const value = await env.STATIONS_KV.get(`stations:${country}`);
  if (!value) {
    return Response.json({ error: `No station data seeded for country '${country}'.` }, { status: 404 });
  }

  return new Response(value, {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "Cache-Control": "public, max-age=3600",
      "Access-Control-Allow-Origin": "*",
    },
  });
}
