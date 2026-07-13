import { readFileSync } from "node:fs";

/**
 * Source: the sibling "radio-monde-app" project, whose stations.json is
 * refreshed daily by its own GitHub Action (.github/workflows/check-streams.yml)
 * — dead stream URLs get replaced automatically. That repo is private.
 *
 * Path resolution:
 *  - RADIO_MONDE_STATIONS_PATH env var if set (used by
 *    .github/workflows/refresh-stations.yml, which checks out radio-monde-app
 *    into a sibling directory in the CI runner).
 *  - otherwise the local Windows checkout path, for manual runs on this dev
 *    machine.
 */
const RADIO_MONDE_STATIONS_PATH =
  process.env.RADIO_MONDE_STATIONS_PATH ??
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
