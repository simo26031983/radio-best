# Touti Score 🃏⭐

Appli Android pour compter les points du **Touti** à la marocaine, 2 contre 2.

Projet Gradle indépendant de best-radio (il réutilise juste le même catalogue de versions `gradle/libs.versions.toml`).

## Ce que fait l'appli

- Deux équipes (🦁 vert / 🦅 rouge), noms d'équipes et de joueurs personnalisables.
- **Saisie d'une manche en 4 gestes** : qui a pris → contrat (70 à 230, par 10) → points des cartes du preneur (la défense est calculée automatiquement sur 130) → annonces / ghna (+20, +40 à l'atout).
- Aperçu en direct : « Contrat réussi 🎉 » ou « Chuté 💥 », avec les points marqués par chaque équipe.
- Onglet **Saisie libre** pour une manche spéciale ou une correction.
- Scores animés, barre de progression vers l'objectif, 👑 sur l'équipe qui mène.
- Rotation automatique du **donneur** (« Karim distribue »).
- Historique des manches, annulation de la dernière manche, suppression d'une manche par appui long.
- Victoire : confettis, « Mabrouk ! مبروك », bouton **Revanche** et compteur de parties gagnées 🏆.
- Partie sauvegardée en continu (rien n'est perdu si l'appli est fermée) ; l'écran reste allumé pendant la partie.

## Règles de score (réglables)

- 130 points par donne (120 en cartes + 10 pour le dernier pli), plus les annonces.
- Le contrat est réussi si **cartes + annonces du preneur ≥ contrat**.
- Partie en 300 / 500 / **600** / 1000 points.
- Contrat réussi, le preneur marque : **ses points** (par défaut) ou le montant du contrat.
- Contrat chuté, la défense marque : **tout, soit 130 + toutes les annonces** (par défaut), ou seulement ses propres points.

## Build

Construit par le workflow `.github/workflows/touti-apk.yml` (tests, lint, APK). L'APK est publié dans la release GitHub `touti-latest`.

En local (JDK 17+ et SDK Android) :

```
./gradlew -p touti assembleDebug
```
