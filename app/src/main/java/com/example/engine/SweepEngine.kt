package com.example.engine

import com.example.data.model.SweepHit

object SweepEngine {
    private val HEX_REGEX = Regex("#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{3})\\b")

    private fun normalizeHex(hex: String): String {
        var h = hex.trim().lowercase().removePrefix("#")
        if (h.length == 3) {
            h = "${h[0]}${h[0]}${h[1]}${h[1]}${h[2]}${h[2]}"
        }
        return "#$h"
    }

    fun performSweep(
        content: String,
        avoidNames: List<String>,
        domains: List<String>,
        colors: List<String>
    ): List<SweepHit> {
        val hits = mutableListOf<SweepHit>()
        val lines = content.lines()

        val normalizedColors = colors.map { normalizeHex(it) }.toSet()

        lines.forEachIndexed { index, line ->
            val lineNum = index + 1

            // Check avoid names
            for (name in avoidNames) {
                val trimmedName = name.trim()
                if (trimmedName.isBlank()) continue

                val regex = Regex("(?i)\\b${Regex.escape(trimmedName)}\\b")
                val looseRegex = Regex("(?i)${Regex.escape(trimmedName)}")

                if (regex.containsMatchIn(line) || looseRegex.containsMatchIn(line)) {
                    hits.add(
                        SweepHit(
                            kind = "name",
                            match = trimmedName,
                            location = "Line $lineNum",
                            snippet = line.trim().take(120)
                        )
                    )
                }
            }

            // Check domains
            for (domain in domains) {
                val trimmedDomain = domain.trim().lowercase()
                if (trimmedDomain.isBlank()) continue

                if (line.contains(trimmedDomain, ignoreCase = true)) {
                    hits.add(
                        SweepHit(
                            kind = "domain",
                            match = trimmedDomain,
                            location = "Line $lineNum",
                            snippet = line.trim().take(120)
                        )
                    )
                }
            }

            // Check colors
            if (normalizedColors.isNotEmpty()) {
                val foundHexes = HEX_REGEX.findAll(line)
                for (match in foundHexes) {
                    val normalizedFound = normalizeHex(match.value)
                    if (normalizedColors.contains(normalizedFound)) {
                        hits.add(
                            SweepHit(
                                kind = "color",
                                match = normalizedFound,
                                location = "Line $lineNum",
                                snippet = line.trim().take(120)
                            )
                        )
                    }
                }
            }
        }

        return hits
    }
}
