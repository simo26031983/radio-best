# Station data schema

Single source of truth for the JSON shape shared by three places:

1. `tools/curate-stations/output/stations.json` — output of the offline curation script (Phase 2).
2. Cloudflare KV values (`stations:FR`, `stations:MA`) served by the Worker at `/api/stations?country=`.
3. `app/src/main/assets/stations.json` — bundled offline-first fallback copied from (1).

Keeping one schema in one place avoids the three drifting apart. If you change this shape, update all three consumers (the TypeScript type in `tools/curate-stations/src/schema.ts`, the Worker's `types.ts`, and the Kotlin `@Serializable` model in `app/src/main/kotlin/com/bestradio/app/data/model/Station.kt`) in the same change.

## Shape

```jsonc
{
  "schemaVersion": 1,
  "generatedAt": "2026-07-13T00:00:00Z",
  "stations": [
    {
      "id": "rb:9617a958-0601-11e8-ae97-52543be04c81",
      "name": "France Inter",
      "country": "FR",
      "streamUrl": "https://icecast.radiofrance.fr/franceinter-hifi.aac",
      "faviconUrl": "https://.../favicon.png",
      "genre": "talk,news",
      "bitrate": 128,
      "codec": "AAC"
    }
  ]
}
```

## Field notes

- `schemaVersion` — bump only on a breaking shape change; lets consumers detect stale/incompatible cached data.
- `generatedAt` — ISO-8601 UTC timestamp of the curation run that produced this file.
- `id` — **stable and append-only**. Derived from radio-browser.info's own `stationuuid` (prefixed `rb:` so a future non-radio-browser source can use a different prefix without collision). Favorites are stored on-device as a `Set<String>` of these IDs — never regenerate IDs from name/index on re-curation, or existing users' favorites silently break.
- `country` — ISO 3166-1 alpha-2, only `"FR"` or `"MA"` for this app.
- `streamUrl` — the resolved, playable stream URL (prefer radio-browser's `url_resolved` over raw `url`).
- `faviconUrl` — may be an empty string; UI must fall back to a placeholder icon, never show a broken-image glyph.
- `genre` — comma-joined tags, may be empty string.
- `bitrate` — kbps, `0` if unknown.
- `codec` — one of `"MP3" | "AAC" | "OGG"` (or whatever radio-browser reports; treat as free text, not a closed enum, since new codecs can appear).
