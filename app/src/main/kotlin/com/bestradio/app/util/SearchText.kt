package com.bestradio.app.util

import java.text.Normalizer

/** Strips accents/diacritics so search matches regardless of them
 * (typing "cherie" finds "Chérie FM"). */
fun String.foldForSearch(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return DIACRITIC_MARKS.replace(normalized, "").lowercase()
}

private val DIACRITIC_MARKS = Regex("\\p{Mn}+")
