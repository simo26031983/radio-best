import type { RawStation } from "./fetchStations.js";
import type { CountryCode, Station } from "./schema.js";

/** Crowd-sourced names often carry decorative junk ("..Radio..", "* Radio *");
 * strip leading/trailing non-alphanumeric symbols and collapse whitespace,
 * without touching the meaningful text in between. */
function cleanName(name: string): string {
  return name
    .trim()
    .replace(/\s+/g, " ")
    .replace(/^[^\p{L}\p{N}]+/u, "")
    .replace(/[^\p{L}\p{N}]+$/u, "");
}

/** radio-browser.info entries occasionally carry the literal text "null"
 * instead of an actually-empty field. */
function cleanOptionalText(value: string | null | undefined): string {
  const trimmed = value?.trim() ?? "";
  return trimmed.toLowerCase() === "null" ? "" : trimmed;
}

export function normalize(raw: RawStation, country: CountryCode): Station {
  return {
    id: `rb:${raw.stationuuid}`,
    name: cleanName(raw.name),
    country,
    streamUrl: raw.url_resolved || raw.url,
    faviconUrl: cleanOptionalText(raw.favicon),
    genre: cleanOptionalText(raw.tags),
    bitrate: raw.bitrate ?? 0,
    codec: cleanOptionalText(raw.codec),
  };
}
