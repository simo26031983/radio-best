# Changelog

## 1.0.2

- Pipeline de release signée (`android-release.yml`) : première release APK signée pour distribution directe, sans compte Play Store.

## 1.0.1

- Nouvelle icône d'application (dégradé + glyphe play/ondes radio).

## 1.0.0

- Message d'erreur ("station indisponible" vs "hors ligne") + bouton Réessayer sur échec de lecture.
- Descriptions d'accessibilité (lecteur d'écran) sur le bouton favori et les lignes de station.
- README et documentation finalisés.

## 0.7.1

- Filtre de sécurité contre les URLs de stream corrompues (bug détecté dans le bot de `radio-monde-app`).

## 0.7.0

- Remplacement de Cloudflare par GitHub Pages : page de téléchargement + catalogue de stations en JSON statique.
- Rafraîchissement quotidien automatique du catalogue depuis `radio-monde-app` (GitHub Actions cron).

## 0.6.1

- Changement de source de données : `radio-browser.info` → catalogue maintenu de `radio-monde-app` (1009 stations, liens morts corrigés automatiquement en amont).

## 0.6.0

- Texte "en cours de lecture" en direct (métadonnées ICY), propagé au téléphone, à la notification et à Android Auto.
- Audit batterie/wakelock/cycle de vie du service, logs de diagnostic (debug uniquement).

## 0.5.0 – 0.5.2

- Intégration Android Auto complète (`MediaLibraryService`, arborescence Favoris/France/Maroc).
- Correction d'un blocage de la navigation Android Auto (spinner infini) découvert lors des premiers tests réels.
- Logos de station (Coil), suppression du sous-titre "genre" sur la liste Android Auto (retour utilisateur).

## 0.4.0

- Favoris et persistance (DataStore), reprise instantanée de la dernière station.
- Bascule permanente France/Maroc/Favoris (au lieu d'un choix figé au premier lancement).

## 0.3.0 – 0.3.1

- Lecture audio (ExoPlayer) et UI de base (liste, mini-lecteur).
- Corrections suite aux premiers tests réels : flux HTTP bloqués (cleartext), notification non cliquable, recouvrement barre de navigation, recherche insensible aux accents.

## 0.2.0

- Première liste de stations curée, API de stations, modèles de données partagés.

## 0.1.0 – 0.1.1

- Squelette du projet (Gradle, Compose, CI GitHub Actions).
