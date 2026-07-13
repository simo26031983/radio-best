# web

Static site published via GitHub Pages at `https://simo26031983.github.io/radio-best/`:

- `index.html` — landing page with the "Télécharger l'APK" button (links to the fixed `best-radio-debug.apk` GitHub Release asset, so this link never needs updating across releases).
- `stations-fr.json` / `stations-ma.json` — the station catalog, served as static files. The Android app fetches these directly (see `app/src/main/kotlin/com/bestradio/app/data/remote/`) as its best-effort remote refresh, falling back to the bundled offline copy on any failure.

Deployed automatically by `.github/workflows/pages-deploy.yml` on every push to `main` that touches this directory, and also by `.github/workflows/refresh-stations.yml`'s daily station-data refresh (see that workflow for how the catalog stays in sync with [`radio-monde-app`](https://github.com/simo26031983/radio-monde-app)'s own daily dead-link-fixing bot).

No Cloudflare account, no secrets beyond what GitHub Pages already provides for free.
