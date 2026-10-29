package com.touti.score.model

/**
 * Numbers spelled the Moroccan way ("سبعين", "مية و عشرين", "ميتين").
 * Left as digits, the Arabic voice would read them in classical Arabic ("سبعون").
 */
object Darija {
    private val units = listOf("", "واحد", "جوج", "تلاتة", "ربعة", "خمسة", "ستة", "سبعة", "تمنية", "تسعود")
    private val teens = listOf(
        "عشرة", "حضاش", "طناش", "تلطاش", "ربعطاش", "خمسطاش", "سطاش", "سبعطاش", "تمنطاش", "تسعطاش",
    )
    private val tens = listOf("", "", "عشرين", "تلاتين", "ربعين", "خمسين", "ستين", "سبعين", "تمانين", "تسعين")

    /** Short forms used before "مية" and "آلاف": تلت مية, ربع آلاف. */
    private val prefixes = listOf("", "", "", "تلت", "ربع", "خمس", "ست", "سبع", "تمن", "تسع")

    fun number(n: Int): String {
        if (n == 0) return "صفر"
        if (n < 0) return "ناقص " + number(-n)
        val parts = mutableListOf<String>()
        val thousands = n / 1000
        val hundreds = n / 100 % 10
        val rest = n % 100
        if (thousands > 0) {
            parts += when (thousands) {
                1 -> "ألف"
                2 -> "ألفين"
                in 3..9 -> "${prefixes[thousands]} آلاف"
                else -> "${number(thousands)} ألف"
            }
        }
        if (hundreds > 0) {
            parts += when (hundreds) {
                1 -> "مية"
                2 -> "ميتين"
                else -> "${prefixes[hundreds]} مية"
            }
        }
        if (rest > 0) parts += belowHundred(rest)
        return parts.joinToString(" و ")
    }

    private fun belowHundred(n: Int): String = when {
        n < 10 -> units[n]
        n < 20 -> teens[n - 10]
        n % 10 == 0 -> tens[n / 10]
        else -> "${units[n % 10]} و ${tens[n / 10]}"
    }
}
