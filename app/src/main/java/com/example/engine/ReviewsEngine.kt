package com.example.engine

import com.example.data.model.ReviewItem
import kotlin.math.roundToInt

data class ReviewTheme(
    val id: String,
    val label: String,
    val kind: String, // "complaint" or "request"
    val score: Double,
    val reviewCount: Int,
    val sharePct: Double,
    val avgRating: Double?,
    val sources: List<String>,
    val quotes: List<ThemeQuote>
)

data class ThemeQuote(
    val quote: String,
    val source: String,
    val rating: Int?,
    val url: String
)

data class ReviewsAnalysisResult(
    val totalReviews: Int,
    val sources: List<String>,
    val ratedCount: Int,
    val themes: List<ReviewTheme>,
    val requests: List<ThemeQuote>,
    val competitorAngle: String
)

object ReviewsEngine {
    private data class ThemeDef(
        val id: String,
        val label: String,
        val kind: String,
        val keywords: List<String>
    )

    private val THEME_DEFINITIONS = listOf(
        ThemeDef(
            "pricing_seats",
            "Aumentos de preços por usuário e paywalls",
            "complaint",
            listOf("price", "pricing", "seat", "expensive", "cost", "tier", "subscription", "paywall", "bill", "preço", "custo", "caro", "cobrança")
        ),
        ThemeDef(
            "timezone_confusion",
            "Confusão de fuso horário e erros de agendamento",
            "complaint",
            listOf("timezone", "time zone", "offset", "daylight", "confus", "wrong time", "slot", "fuso", "horário", "hora errada")
        ),
        ThemeDef(
            "missing_sms",
            "Falta de lembretes por SMS / Alto índice de faltas",
            "request",
            listOf("sms", "text", "reminder", "no-show", "missed appointment", "whatsapp", "lembrete", "mensagem", "notificação")
        ),
        ThemeDef(
            "calendar_sync",
            "Atrasos na sincronização e conflito de agendas",
            "complaint",
            listOf("sync", "double book", "google calendar", "outlook", "delay", "conflict", "sincroniz", "conflito", "duplicad")
        ),
        ThemeDef(
            "complex_ui",
            "Interface sobrecarregada e onboarding confuso",
            "complaint",
            listOf("clutter", "complex", "confusing", "hard to", "navigate", "slow", "lag", "bloat", "lento", "difícil", "pesado", "confuso")
        ),
        ThemeDef(
            "team_features",
            "Gestão de equipe e disponibilidade compartilhada",
            "request",
            listOf("team", "shared", "round-robin", "collaborat", "assign", "equipe", "time", "compartilhad", "colabora")
        )
    )

    fun analyzeReviews(reviews: List<ReviewItem>): ReviewsAnalysisResult {
        val total = reviews.size
        if (total == 0) {
            return ReviewsAnalysisResult(
                totalReviews = 0,
                sources = emptyList(),
                ratedCount = 0,
                themes = emptyList(),
                requests = emptyList(),
                competitorAngle = "Adicione avaliações de clientes para identificar vulnerabilidades e posicionamento estratégico do concorrente."
            )
        }

        val sources = reviews.map { it.source }.distinct().sorted()
        val ratedCount = reviews.count { it.rating != null }

        val themeMatches = mutableMapOf<String, MutableList<Pair<ReviewItem, String>>>()
        val themeScores = mutableMapOf<String, Double>()
        THEME_DEFINITIONS.forEach {
            themeMatches[it.id] = mutableListOf()
            themeScores[it.id] = 0.0
        }

        val directRequests = mutableListOf<ThemeQuote>()

        for (rv in reviews) {
            val weight = if (rv.rating != null) (6.0 - rv.rating) / 5.0 else 0.6
            val lowerText = rv.text.lowercase()

            for (theme in THEME_DEFINITIONS) {
                val matchedKeyword = theme.keywords.firstOrNull { lowerText.contains(it) }
                if (matchedKeyword != null) {
                    val currentScore = themeScores[theme.id] ?: 0.0
                    themeScores[theme.id] = currentScore + weight
                    themeMatches[theme.id]?.add(Pair(rv, extractSnippet(rv.text, matchedKeyword)))
                }
            }

            if (lowerText.contains("wish") || lowerText.contains("need") || lowerText.contains("please add") || lowerText.contains("why can't") ||
                lowerText.contains("precisa") || lowerText.contains("gostaria") || lowerText.contains("deveria ter")) {
                directRequests.add(
                    ThemeQuote(
                        quote = rv.text.take(160),
                        source = rv.source,
                        rating = rv.rating,
                        url = rv.url
                    )
                )
            }
        }

        val rankedThemes = mutableListOf<ReviewTheme>()
        for (def in THEME_DEFINITIONS) {
            val items = themeMatches[def.id] ?: continue
            if (items.isEmpty()) continue

            val ratings = items.mapNotNull { it.first.rating }
            val avgRating = if (ratings.isNotEmpty()) {
                (ratings.average() * 10.0).roundToInt() / 10.0
            } else null

            val themeSources = items.map { it.first.source }.distinct()
            val score = ((themeScores[def.id] ?: 0.0) * 10.0).roundToInt() / 10.0
            val sharePct = ((items.size.toDouble() / total * 100.0) * 10.0).roundToInt() / 10.0

            val quotes = items.take(3).map {
                ThemeQuote(
                    quote = it.second,
                    source = it.first.source,
                    rating = it.first.rating,
                    url = it.first.url
                )
            }

            rankedThemes.add(
                ReviewTheme(
                    id = def.id,
                    label = def.label,
                    kind = def.kind,
                    score = score,
                    reviewCount = items.size,
                    sharePct = sharePct,
                    avgRating = avgRating,
                    sources = themeSources,
                    quotes = quotes
                )
            )
        }

        rankedThemes.sortWith(compareByDescending<ReviewTheme> { it.score }.thenByDescending { it.reviewCount })

        val topComplaint = rankedThemes.firstOrNull { it.kind == "complaint" }
        val competitorAngle = if (topComplaint != null) {
            "Aproveite a maior dor dos usuários do concorrente: '${topComplaint.label}'. Posicione sua réplica com preço fixo transparente, alta confiabilidade e sem atritos nessa área."
        } else {
            "Posicione sua réplica como a alternativa ágil, moderna, sem inchaço visual e com planos acessíveis."
        }

        return ReviewsAnalysisResult(
            totalReviews = total,
            sources = sources,
            ratedCount = ratedCount,
            themes = rankedThemes,
            requests = directRequests,
            competitorAngle = competitorAngle
        )
    }

    private fun extractSnippet(text: String, keyword: String): String {
        val sentences = text.split(Regex("[.!?]\\s+"))
        val matchedSentence = sentences.firstOrNull { it.contains(keyword, ignoreCase = true) }
        val target = matchedSentence ?: text
        return if (target.length > 200) target.take(197) + "..." else target.trim()
    }
}
