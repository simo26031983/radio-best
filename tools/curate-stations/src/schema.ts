/**
 * Shared station schema — see /SCHEMA.md at repo root.
 * Mirrored by worker/src/types.ts and the Kotlin model in
 * app/src/main/kotlin/com/bestradio/app/data/model/Station.kt.
 * Keep all three in sync when this shape changes.
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
