import type { RawRadioMondeStation } from "./radioMondeSource.js";
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

/** Matches radio-monde-app's own client-side logic exactly (index.html):
 * prefer the station's own favicon (curated Streema artwork for ~1/4 of
 * stations), else derive one from the station's website via Google's public
 * favicon service. */
function resolveFaviconUrl(station: RawRadioMondeStation): string {
  if (station.favicon && station.favicon.trim() !== "") {
    return station.favicon.trim();
  }
  if (station.domain && station.domain.trim() !== "") {
    return `https://www.google.com/s2/favicons?domain=${encodeURIComponent(station.domain.trim())}&sz=128`;
  }
  return "";
}

function toCountryCode(raw: string): CountryCode | null {
  switch (raw.trim().toLowerCase()) {
    case "fr":
      return "FR";
    case "ma":
      return "MA";
    default:
      return null;
  }
}

export function normalize(raw: RawRadioMondeStation): Station | null {
  const country = toCountryCode(raw.country);
  if (!country) return null;

  return {
    id: `rm:${raw.id}`,
    name: cleanName(raw.name),
    country,
    streamUrl: raw.url,
    faviconUrl: resolveFaviconUrl(raw),
    genre: "",
    bitrate: 0,
    codec: "",
  };
}
