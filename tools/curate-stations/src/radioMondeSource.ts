import { readFileSync } from "node:fs";

/**
 * Source: the sibling "radio-monde-app" project (c:\Working_Folder\git_projects\radio-monde-app),
 * whose stations.json is refreshed daily by its own GitHub Action
 * (.github/workflows/check-streams.yml) — dead stream URLs get replaced
 * automatically. That repo is private, so for now this reads the local
 * checkout directly; the path is intentionally a plain constant since this
 * is a manual, occasional maintenance script, not a runtime dependency.
 *
 * Follow-up (Phase 7, once Cloudflare secrets exist): wire an automated pull
 * from that repo (a scoped read-only token) so the Worker/KV catalog stays
 * fresh without a manual re-run of this script each time.
 */
const RADIO_MONDE_STATIONS_PATH =
  "c:\\Working_Folder\\git_projects\\radio-monde-app\\stations.json";

export interface RawRadioMondeStation {
  id: string;
  name: string;
  country: string; // lowercase, e.g. "fr" | "ma"
  domain: string;
  color: string;
  url: string;
  favicon?: string;
  fallbackUrls: string[];
  status: "ok" | "down";
  lastChecked: string;
}

interface RadioMondeFile {
  version: string;
  lastChecked: string;
  stations: RawRadioMondeStation[];
}

export function loadRadioMondeStations(): RawRadioMondeStation[] {
  const raw = readFileSync(RADIO_MONDE_STATIONS_PATH, "utf-8");
  const parsed = JSON.parse(raw) as RadioMondeFile;
  return parsed.stations;
}
