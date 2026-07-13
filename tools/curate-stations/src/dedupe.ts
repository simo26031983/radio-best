import type { RawStation } from "./fetchStations.js";

function normalizeKey(name: string): string {
  return name
    .toLowerCase()
    .normalize("NFD")
    .replace(new RegExp("[\\u0300-\\u036f]", "g"), "") // strip combining diacritics (accents)
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

/** Collapses near-duplicate entries (same station listed multiple times under
 * slightly different names/streams), keeping the one with the most votes. */
export function dedupeStations(stations: RawStation[]): RawStation[] {
  const byKey = new Map<string, RawStation>();
  for (const station of stations) {
    const key = normalizeKey(station.name);
    const existing = byKey.get(key);
    if (!existing || station.votes > existing.votes) {
      byKey.set(key, station);
    }
  }
  const deduped = [...byKey.values()];
  console.log(`[dedupe] ${deduped.length}/${stations.length} stations kept after dedupe`);
  return deduped;
}
