# best-radio

Application Android native pour écouter les radios françaises et marocaines, optimisée en priorité pour **Android Auto** (démarrage immédiat des flux, fiabilité de connexion, faible consommation batterie), et pour smartphone en usage secondaire.

Testée en conditions réelles : téléphone Android, et Android Auto via [Google's Desktop Head Unit](https://github.com/simo26031983/android-emulators/tree/main/android-auto-dhu) (voir ce dépôt frère pour le simuler sans voiture).

## Fonctionnalités

- Choix du pays (France / Maroc) via onglets toujours accessibles, plus un onglet Favoris (indépendant du pays)
- Recherche de station insensible aux accents/majuscules
- Démarrage quasi instantané des flux radio (`ExoPlayer` réglé pour le direct, pas pour la VOD)
- Logos de station, texte "en cours de lecture" (métadonnées ICY) affiché en direct
- Reprise instantanée de la dernière station écoutée au lancement de l'app
- Message d'erreur + bouton "Réessayer" en cas de flux mort ou de perte réseau (distingue les deux cas)
- Intégration Android Auto complète : arborescence Favoris/France/Maroc, reprise de lecture, notification cliquable
- Catalogue de stations mis à jour automatiquement chaque jour, sans recompiler l'app

## Architecture

- **`app/`** — application Android (Kotlin, Jetpack Compose, Media3/ExoPlayer, `MediaLibraryService` pour Android Auto). Voir [`SCHEMA.md`](SCHEMA.md) pour le schéma des données de stations.
- **`tools/curate-stations/`** — script de curation (Node/TypeScript) qui transforme le `stations.json` de [radio-monde-app](https://github.com/simo26031983/radio-monde-app) (lui-même maintenu par un bot quotidien qui corrige les liens morts) vers le schéma partagé de ce projet.
- **`web/`** — site statique [GitHub Pages](https://simo26031983.github.io/radio-best/) : page de téléchargement de l'APK + catalogue de stations servi en JSON statique (aucune logique serveur nécessaire).
- **`.github/workflows/`** :
  - `android-ci.yml` — build/lint/test à chaque push (dont le build release minifié, pour attraper toute casse R8), publie l'APK release signé (`best-radio.apk`) et l'AAB (`best-radio.aab`, pour la Play Console) en GitHub Release sur les tags de version.
  - `pages-deploy.yml` — déploie `web/` sur GitHub Pages.
  - `refresh-stations.yml` — cron quotidien (4h UTC) qui récupère la dernière version du catalogue depuis radio-monde-app et republie `web/stations-*.json`.

Aucun compte externe requis (pas de Cloudflare) — tout repose sur GitHub, déjà utilisé pour le code et les releases.

## Build

Le build Android se fait via GitHub Actions (aucun SDK Android local requis). Pour construire en local avec le JDK 17+ et le SDK Android installés :

```
./gradlew assembleDebug
```

L'APK **release signé** est publié en tant que GitHub Release à chaque version taguée, pour les tests réels sur téléphone et Android Auto. La signature est stable d'une version à l'autre, donc les mises à jour s'installent par-dessus l'ancienne version sans désinstaller.

### Signature release

Les identifiants de signature ne sont jamais commités. Deux sources possibles :

- **En local** : un fichier `keystore.properties` à la racine du dépôt (ignoré par git) avec les clés `storeFile`, `storePassword`, `keyAlias`, `keyPassword`.
- **En CI** : les secrets GitHub `RELEASE_KEYSTORE_BASE64` (le fichier `.jks` encodé en base64 : `base64 -w0 bestradio-upload.jks`), `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.

Sans identifiants, `assembleRelease` produit un APK non signé (utile pour vérifier R8) ; la CI refuse alors de publier la release.

## Statut

Développement par étapes versionnées (`versionCode`/`versionName` incrémentés à chaque changement, un commit par étape). Voir [`CHANGELOG.md`](CHANGELOG.md) pour le détail des versions, et l'historique git pour la progression complète.
