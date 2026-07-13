# best-radio

Application Android native pour écouter les radios françaises et marocaines, optimisée en priorité pour **Android Auto** (démarrage immédiat des flux, fiabilité de connexion, faible consommation batterie), et pour smartphone en usage secondaire.

## Fonctionnalités prévues

- Choix du pays : France ou Maroc
- Favoris (indépendants du pays sélectionné)
- Démarrage quasi instantané des flux radio
- Intégration Android Auto complète (arborescence de navigation, reprise de lecture, gestion des interruptions d'appel/navigation)
- Liste de stations mise à jour côté serveur sans recompiler l'app (API Cloudflare Worker), avec repli hors-ligne intégré à l'app

## Architecture

- **`app/`** — application Android (Kotlin, Jetpack Compose, Media3/ExoPlayer, `MediaLibraryService` pour Android Auto). Voir le plan de développement pour le détail des couches (`data/`, `playback/`, `ui/`).
- **`tools/curate-stations/`** — script de curation hors-ligne (Node/TypeScript) qui interroge [radio-browser.info](https://api.radio-browser.info/) et produit le JSON des stations FR/MA (voir [`SCHEMA.md`](SCHEMA.md)).
- **`worker/`** — Cloudflare Worker + KV servant `GET /api/stations?country=FR|MA`.
- **`web/`** — page Cloudflare Pages statique avec le lien de téléchargement de l'APK.
- **`.github/workflows/`** — CI (build/lint/test Android) et déploiement Cloudflare, sans dépendance à un environnement Android local.

## Build

Le build Android se fait via GitHub Actions (aucun SDK Android local requis). Pour construire en local si vous avez le JDK 17+ et le SDK Android installés :

```
./gradlew assembleDebug
```

L'APK debug est publié en tant que GitHub Release à chaque phase de développement pour permettre les tests réels sur téléphone et Android Auto.

## Statut

Développement par phases versionnées (`versionCode`/`versionName` incrémentés à chaque étape, un commit par phase). Voir l'historique git pour la progression.
