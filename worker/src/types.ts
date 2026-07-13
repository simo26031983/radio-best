/**
 * Shared station schema — see /SCHEMA.md at repo root.
 * Mirrors tools/curate-stations/src/schema.ts and the Kotlin model in
 * app/src/main/kotlin/com/bestradio/app/data/model/Station.kt.
 */

export type CountryCode = "FR" | "MA";

export interface Station {
  id: string;
  name: string;
  country: CountryCode;
  streamUrl: string;
  faviconUrl: string;
  genre: string;
  bitrate: number;
  codec: string;
}

export interface StationsFile {
  schemaVersion: 1;
  generatedAt: string;
  stations: Station[];
}

export interface Env {
  STATIONS_KV: KVNamespace;
}
