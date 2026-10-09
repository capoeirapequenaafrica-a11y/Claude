package com.example.engine

import com.example.data.model.LintIssue
import com.example.data.model.StoreListingData

object ListingLinterEngine {
    private val CLAIMS_REGEX = Regex(
        "(#\\s?1\\b|\\bnumber one\\b|\\bno\\.? ?1\\b|\\bbest\\b|\\btop[- ]rated\\b|\\b(app|game) of the year\\b|\\bfree\\b|\\bdiscount\\b|\\bsale\\b|\\b\\d+% off\\b|\\bmelhor\\b|\\bgrátis\\b|\\bgratis\\b|\\bdesconto\\b|\\bnovo!?\$)",
        RegexOption.IGNORE_CASE
    )

    private val STOP_WORDS = setOf("a", "an", "and", "app", "for", "the", "to", "of", "with", "your", "you", "in", "on", "&", "o", "a", "de", "para", "com", "seu", "sua", "em")

    private fun hasEmoji(text: String): Boolean {
        for (i in 0 until text.length) {
            val type = Character.getType(text[i])
            if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                return true
            }
        }
        return false
    }

    private fun extractWords(text: String): Set<String> {
        val wordRegex = Regex("[a-zA-Z0-9À-ÿ']+")
        return wordRegex.findAll(text.lowercase())
            .map { it.value }
            .filter { it !in STOP_WORDS && it.length > 1 }
            .toSet()
    }

    fun lintListing(listing: StoreListingData): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        val avoid = listing.avoidNames.filter { it.isNotBlank() }

        // --- Google Play Checks ---
        val playTitle = listing.playStoreTitle.trim()
        val playShort = listing.playStoreShortDesc.trim()
        val playFull = listing.playStoreFullDesc.trim()

        if (playTitle.isEmpty()) {
            issues.add(LintIssue("error", "google_play", "title", "O título está vazio."))
        } else {
            if (playTitle.length > 30) {
                issues.add(LintIssue("error", "google_play", "title", "${playTitle.length} caracteres (Limite no Play é 30, ${playTitle.length - 30} caracteres a mais)."))
            }
            if (hasEmoji(playTitle)) {
                issues.add(LintIssue("warn", "google_play", "title", "Contém emojis. O Google Play proíbe terminantemente emojis no título do app."))
            }
            CLAIMS_REGEX.find(playTitle)?.let { match ->
                issues.add(LintIssue("warn", "google_play", "title", "'${match.value}' soa como alegação de ranking ou promoção (violação de políticas da Play Store)."))
            }
            val caps = Regex("\\b[A-Z]{4,}\\b").findAll(playTitle).map { it.value }.toList()
            if (caps.isNotEmpty()) {
                issues.add(LintIssue("warn", "google_play", "title", "Contém palavras em caixa alta: ${caps.joinToString(", ")}"))
            }
            for (badName in avoid) {
                if (playTitle.contains(badName, ignoreCase = true)) {
                    issues.add(LintIssue("error", "google_play", "title", "Menciona o concorrente '$badName'. As lojas rejeitam cópias de metadados."))
                }
            }
        }

        if (playShort.isEmpty()) {
            issues.add(LintIssue("warn", "google_play", "short_description", "A descrição curta está vazia."))
        } else {
            if (playShort.length > 80) {
                issues.add(LintIssue("error", "google_play", "short_description", "${playShort.length} caracteres (Limite é 80, ${playShort.length - 80} caracteres a mais)."))
            }
            if (hasEmoji(playShort)) {
                issues.add(LintIssue("warn", "google_play", "short_description", "Contém emojis na descrição curta."))
            }
            CLAIMS_REGEX.find(playShort)?.let { match ->
                issues.add(LintIssue("warn", "google_play", "short_description", "'${match.value}' soa como alegação de ranking ou preço."))
            }
            for (badName in avoid) {
                if (playShort.contains(badName, ignoreCase = true)) {
                    issues.add(LintIssue("error", "google_play", "short_description", "Menciona o concorrente '$badName'."))
                }
            }
        }

        if (playFull.isNotEmpty()) {
            if (playFull.length > 4000) {
                issues.add(LintIssue("error", "google_play", "full_description", "${playFull.length} caracteres (Limite é 4000)."))
            }
            for (badName in avoid) {
                if (playFull.contains(badName, ignoreCase = true)) {
                    issues.add(LintIssue("error", "google_play", "full_description", "Menciona o concorrente '$badName' na descrição completa."))
                }
            }
        }

        // --- App Store Checks ---
        val appName = listing.appStoreName.trim()
        val appSubtitle = listing.appStoreSubtitle.trim()
        val appKeywords = listing.appStoreKeywords.trim()
        val appDesc = listing.appStoreDescription.trim()

        if (appName.isNotEmpty()) {
            if (appName.length > 30) {
                issues.add(LintIssue("error", "app_store", "name", "${appName.length} caracteres (Limite na App Store é 30, ${appName.length - 30} caracteres a mais)."))
            }
            CLAIMS_REGEX.find(appName)?.let { match ->
                issues.add(LintIssue("warn", "app_store", "name", "'${match.value}' soa como alegação não verificável."))
            }
            for (badName in avoid) {
                if (appName.contains(badName, ignoreCase = true)) {
                    issues.add(LintIssue("error", "app_store", "name", "Menciona '$badName'. A Apple rejeitará o aplicativo."))
                }
            }
        }

        if (appSubtitle.isNotEmpty()) {
            if (appSubtitle.length > 30) {
                issues.add(LintIssue("error", "app_store", "subtitle", "${appSubtitle.length} caracteres (Limite é 30)."))
            }
            for (badName in avoid) {
                if (appSubtitle.contains(badName, ignoreCase = true)) {
                    issues.add(LintIssue("error", "app_store", "subtitle", "Menciona o concorrente '$badName'."))
                }
            }
        }

        if (appKeywords.isNotEmpty()) {
            if (appKeywords.length > 100) {
                issues.add(LintIssue("error", "app_store", "keywords", "${appKeywords.length} caracteres (Limite na App Store é 100, ${appKeywords.length - 100} caracteres a mais)."))
            }
            if (appKeywords.contains(", ")) {
                issues.add(LintIssue("warn", "app_store", "keywords", "Espaços após vírgulas desperdiçam caracteres. Use apenas vírgula: 'palavra1,palavra2'."))
            }

            val kwList = appKeywords.split(",").map { it.trim().lowercase() }
            val duplicates = kwList.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            if (duplicates.isNotEmpty()) {
                issues.add(LintIssue("warn", "app_store", "keywords", "Palavras-chave repetidas: ${duplicates.joinToString(", ")}"))
            }

            val titleWords = extractWords(appName + " " + appSubtitle)
            val wasted = kwList.filter { it in titleWords }
            if (wasted.isNotEmpty()) {
                issues.add(LintIssue("warn", "app_store", "keywords", "Palavras já indexadas pelo título/subtítulo: ${wasted.joinToString(", ")}"))
            }
        }

        if (appDesc.isNotEmpty()) {
            if (appDesc.length > 4000) {
                issues.add(LintIssue("error", "app_store", "description", "${appDesc.length} caracteres (Limite é 4000)."))
            }
            for (badName in avoid) {
                if (appDesc.contains(badName, ignoreCase = true)) {
                    issues.add(LintIssue("error", "app_store", "description", "Menciona '$badName' na descrição."))
                }
            }
        }

        return issues
    }
}
