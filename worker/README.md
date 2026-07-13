# worker

Cloudflare Worker + KV serving `GET /api/stations?country=FR|MA`. The Worker itself does no filtering or parsing at request time — it just reads a pre-split, pre-serialized JSON blob straight out of KV, so it stays fast and cheap on Workers' CPU-time billing.

## One-time setup (requires a free Cloudflare account)

```
npm install
npx wrangler login
npx wrangler kv namespace create STATIONS_KV
```

Copy the `id` printed by the last command into `wrangler.toml`'s `[[kv_namespaces]]` block (replacing `REPLACE_WITH_KV_NAMESPACE_ID`).

## Seeding station data

After running the curation script in `tools/curate-stations` (`npm run curate`), seed KV from its output:

```
npm run seed            # seeds the remote/production namespace
npm run seed -- --local # seeds wrangler's local dev KV store, for use with `npm run dev`
```

## Local development

```
npm run dev
curl "http://localhost:8787/api/stations?country=FR"
```

## Deploy

```
npm run deploy
```

CI (`.github/workflows/worker-deploy.yml`) deploys automatically on pushes to `main` that touch this directory, using the `CLOUDFLARE_API_TOKEN` / `CLOUDFLARE_ACCOUNT_ID` GitHub secrets.
