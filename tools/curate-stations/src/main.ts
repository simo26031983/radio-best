import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { fetchStationsByCountry } from "./fetchStations.js";
import { filterReliable } from "./filterAndScore.js";
import { dedupeStations } from "./dedupe.js";
import { normalize } from "./normalize.js";
import type { CountryCode, Station, StationsFile } from "./schema.js";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const OUTPUT_DIR = path.join(__dirname, "..", "output");
const COUNTRIES: CountryCode[] = ["FR", "MA"];

async function curateCountry(country: CountryCode): Promise<Station[]> {
  const raw = await fetchStationsByCountry(country);
  const reliable = filterReliable(raw, country);
  const deduped = dedupeStations(reliable);
  return deduped
    .map((s) => normalize(s, country))
    .sort((a, b) => a.name.localeCompare(b.name, "fr"));
}

async function main() {
  await mkdir(OUTPUT_DIR, { recursive: true });

  const perCountry: Record<CountryCode, Station[]> = { FR: [], MA: [] };
  for (const country of COUNTRIES) {
    perCountry[country] = await curateCountry(country);
  }

  const generatedAt = new Date().toISOString();

  for (const country of COUNTRIES) {
    const file: StationsFile = {
      schemaVersion: 1,
      generatedAt,
      stations: perCountry[country],
    };
    const outPath = path.join(OUTPUT_DIR, `stations-${country.toLowerCase()}.json`);
    await writeFile(outPath, JSON.stringify(file, null, 2) + "\n", "utf-8");
    console.log(`[main] wrote ${perCountry[country].length} stations to ${outPath}`);
  }

  const merged: StationsFile = {
    schemaVersion: 1,
    generatedAt,
    stations: COUNTRIES.flatMap((c) => perCountry[c]),
  };
  const mergedPath = path.join(OUTPUT_DIR, "stations.json");
  await writeFile(mergedPath, JSON.stringify(merged, null, 2) + "\n", "utf-8");
  console.log(`[main] wrote ${merged.stations.length} total stations to ${mergedPath}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
