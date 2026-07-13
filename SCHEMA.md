# Station data schema

Single source of truth for the JSON shape shared by three places:

1. `tools/curate-stations/output/stations.json` — output of the offline curation script.
2. Cloudflare KV values (`stations:FR`, `stations:MA`) served by the Worker at `/api/stations?country=`.
3. `app/src/main/assets/stations.json` — bundled offline-first fallback copied from (1).

Keeping one schema in one place avoids the three drifting apart. If you change this shape, update all three consumers (the TypeScript type in `tools/curate-stations/src/schema.ts`, the Worker's `types.ts`, and the Kotlin `@Serializable` model in `app/src/main/kotlin/com/bestradio/app/data/model/Station.kt`) in the same change.

## Data source

Curated from the [`radio-monde-app`](https://github.com/simo26031983/radio-monde-app) project's `stations.json` (a sibling local checkout, private repo), whose own GitHub Action (`check-streams.yml`, daily cron) automatically re-checks every stream URL and swaps in a working alternative from radio-browser.info if one dies — so this project inherits that maintenance instead of running its own dead-link checking. `tools/curate-stations/src/main.ts` filters to `status === "ok"` and normalizes into the shape below. This is currently a manual, occasional re-run (`npm run curate` in `tools/curate-stations`); an automated pull is planned for once Cloudflare secrets exist (Phase 7), so the catalog can refresh without a manual step.

## Shape

```jsonc
{
  "schemaVersion": 1,
  "generatedAt": "2026-07-13T00:00:00Z",
  "stations": [
    {
      "id": "rm:france-inter",
      "name": "France Inter",
      "country": "FR",
      "streamUrl": "https://icecast.radiofrance.fr/franceinter-hifi.aac",
      "faviconUrl": "https://.../favicon.png",
      "genre": "",
      "bitrate": 0,
      "codec": ""
    }
  ]
}
```

## Field notes

- `schemaVersion` — bump only on a breaking shape change; lets consumers detect stale/incompatible cached data.
- `generatedAt` — ISO-8601 UTC timestamp of the curation run that produced this file.
- `id` — **stable and append-only**. Prefixed `rm:` + radio-monde-app's own station id (so a future source can use a different prefix without collision). Favorites are stored on-device as a `Set<String>` of these IDs — never regenerate IDs from name/index on re-curation, or existing users' favorites silently break. (Switching from the prior radio-browser.info source to radio-monde-app changed every ID — a one-time favorites reset, acceptable pre-1.0.)
- `country` — ISO 3166-1 alpha-2, only `"FR"` or `"MA"` for this app.
- `streamUrl` — the station's primary stream URL as maintained by radio-monde-app's daily checker; its `fallbackUrls` array isn't currently carried into this schema.
- `faviconUrl` — resolved exactly like radio-monde-app's own UI does: the station's curated `favicon` field if present, else `https://www.google.com/s2/favicons?domain=<domain>&sz=128`, else empty string. May be empty; UI must fall back to a placeholder icon, never show a broken-image glyph.
- `genre` / `bitrate` / `codec` — not provided by this data source; always `""` / `0` / `""` for now. Kept in the schema since the UI already treats them as optional (blank-safe), and a future source could populate them again.
