import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { loadRadioMondeStations } from "./radioMondeSource.js";
import { normalize } from "./normalize.js";
import type { CountryCode, Station, StationsFile } from "./schema.js";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const OUTPUT_DIR = path.join(__dirname, "..", "output");
const COUNTRIES: CountryCode[] = ["FR", "MA"];

/** Safety net against a bug observed in radio-monde-app's own dead-link-fixing
 * bot: its "find an alternative stream" step can occasionally produce a
 * corrupted URL (the same URL repeated hundreds of times, presumably from a
 * concatenation bug on their side). A real stream URL is never anywhere near
 * this long — drop anything absurd rather than shipping an unplayable,
 * multi-kilobyte "URL" to the app. */
const MAX_SANE_STREAM_URL_LENGTH = 500;

function hasSaneStreamUrl(station: Station): boolean {
  const ok = station.streamUrl.length <= MAX_SANE_STREAM_URL_LENGTH;
  if (!ok) {
    console.warn(
      `[main] dropping "${station.name}" (${station.id}): streamUrl is ${station.streamUrl.length} chars, looks corrupted`
    );
  }
  return ok;
}

/** radio-monde-app's own slug generator occasionally collides for two
 * genuinely different stations (e.g. "ABC Lounge Radio" and "ABC LOUNGE
 * Webradio" both slug to "abc-lounge-radio"). These are different stations,
 * not duplicates to merge — keep both, just make the id unique so Compose's
 * LazyColumn (which key indexes by station id) doesn't crash on the clash. */
function dedupeIds(stations: Station[]): Station[] {
  const seen = new Map<string, number>();
  return stations.map((station) => {
    const count = seen.get(station.id) ?? 0;
    seen.set(station.id, count + 1);
    return count === 0 ? station : { ...station, id: `${station.id}-${count + 1}` };
  });
}

async function main() {
  await mkdir(OUTPUT_DIR, { recursive: true });

  const rawStations = loadRadioMondeStations();
  console.log(`[main] loaded ${rawStations.length} raw stations from radio-monde-app`);

  const okStations = rawStations.filter((s) => s.status === "ok");
  console.log(`[main] ${okStations.length}/${rawStations.length} stations are currently "ok" (excluding "down")`);

  const normalized = dedupeIds(
    okStations
      .map(normalize)
      .filter((s): s is Station => s !== null)
      .filter(hasSaneStreamUrl)
  );
  console.log(`[main] ${normalized.length} stations normalized (unrecognized country codes / corrupted URLs dropped)`);

  const perCountry: Record<CountryCode, Station[]> = { FR: [], MA: [] };
  for (const station of normalized) {
    perCountry[station.country].push(station);
  }
  for (const country of COUNTRIES) {
    perCountry[country].sort((a, b) => a.name.localeCompare(b.name, "fr"));
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
