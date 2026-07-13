import type { RawStation } from "./fetchStations.js";

export interface ReliabilityThresholds {
  minVotes: number;
  minClickcount: number;
}

/** Morocco has far fewer radio-browser.info entries than France, so it gets a lower bar. */
export const THRESHOLDS: Record<string, ReliabilityThresholds> = {
  FR: { minVotes: 5, minClickcount: 50 },
  MA: { minVotes: 1, minClickcount: 5 },
};

function hasPlayableUrl(station: RawStation): boolean {
  const url = station.url_resolved || station.url;
  if (!url) return false;
  try {
    const parsed = new URL(url);
    return parsed.protocol === "http:" || parsed.protocol === "https:";
  } catch {
    return false;
  }
}

export function filterReliable(stations: RawStation[], countryCode: string): RawStation[] {
  const thresholds = THRESHOLDS[countryCode] ?? THRESHOLDS.FR;
  const filtered = stations.filter(
    (s) =>
      s.lastcheckok === 1 &&
      hasPlayableUrl(s) &&
      (s.votes >= thresholds.minVotes || s.clickcount >= thresholds.minClickcount)
  );
  console.log(
    `[filterAndScore] ${countryCode}: ${filtered.length}/${stations.length} stations kept ` +
      `(minVotes=${thresholds.minVotes}, minClickcount=${thresholds.minClickcount})`
  );
  return filtered;
}
