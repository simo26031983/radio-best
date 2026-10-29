# Touti Score 🃏⭐

Appli Android pour compter les points du **Touti** à la marocaine, 2 contre 2.

Projet Gradle indépendant de best-radio (il réutilise juste le même catalogue de versions `gradle/libs.versions.toml`).

## Ce que fait l'appli

- Deux équipes (🦁 vert / 🦅 rouge), noms d'équipes et de joueurs personnalisables.
- **Une manche = 2 gestes** : qui gagne → combien (70 à 230). Le montant est ajouté au score de l'équipe gagnante.
- Scores animés, barre de progression vers l'objectif, 👑 sur l'équipe qui mène.
- **Annonce vocale** après chaque manche, en français et/ou en darija (voix arabe du téléphone, nombres dits à la marocaine : سبعين، مية و عشرين…) : la valeur de la manche puis le score (« Plus 70 pour Les Lions. Score : Les Lions 170, Les Aigles 90. »). Bouton 🔊/🔇 pour couper le son.
- Rotation automatique du **donneur** (« Karim distribue »).
- Historique des manches, annulation de la dernière manche, suppression d'une manche par appui long.
- Victoire : confettis, « Mabrouk ! مبروك », bouton **Revanche** et compteur de parties gagnées 🏆.
- Partie sauvegardée en continu (rien n'est perdu si l'appli est fermée) ; l'écran reste allumé pendant la partie.

## Règle de score

L'équipe qui gagne la manche marque le montant de l'enchère (ex. enchère à 70 gagnée → +70). Partie en 300 / 500 / **600** / 1000 points.

## Build

Construit par le workflow `.github/workflows/touti-apk.yml` (tests, lint, APK). L'APK est publié dans la release GitHub `touti-latest`.

En local (JDK 17+ et SDK Android) :

```
./gradlew -p touti assembleDebug
```
