package com.example.engine

import com.example.data.model.ContrastCheckResult
import com.example.data.model.DesignToken
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

object ContrastEngine {
    private val HEX_REGEX = Regex("^#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$")

    fun parseHex(value: String): Triple<Int, Int, Int>? {
        val trimmed = value.trim()
        val match = HEX_REGEX.matchEntire(trimmed) ?: return null
        var h = match.groupValues[1]
        if (h.length == 3) {
            h = "${h[0]}${h[0]}${h[1]}${h[1]}${h[2]}${h[2]}"
        }
        val r = h.substring(0, 2).toIntOrNull(16) ?: return null
        val g = h.substring(2, 4).toIntOrNull(16) ?: return null
        val b = h.substring(4, 6).toIntOrNull(16) ?: return null
        return Triple(r, g, b)
    }

    fun luminance(rgb: Triple<Int, Int, Int>): Double {
        fun channel(c: Int): Double {
            val normalized = c / 255.0
            return if (normalized <= 0.04045) {
                normalized / 12.92
            } else {
                ((normalized + 0.055) / 1.055).pow(2.4)
            }
        }
        val r = channel(rgb.first)
        val g = channel(rgb.second)
        val b = channel(rgb.third)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    fun ratio(fgHex: String, bgHex: String): Double {
        val fg = parseHex(fgHex) ?: return 1.0
        val bg = parseHex(bgHex) ?: return 1.0
        val l1 = luminance(fg)
        val l2 = luminance(bg)
        val hi = max(l1, l2)
        val lo = min(l1, l2)
        val r = (hi + 0.05) / (lo + 0.05)
        return (r * 100.0).roundToInt() / 100.0
    }

    fun checkPair(
        fgName: String,
        bgName: String,
        fgHex: String,
        bgHex: String,
        size: String = "normal"
    ): ContrastCheckResult {
        val fg = parseHex(fgHex)
        val bg = parseHex(bgHex)
        if (fg == null || bg == null) {
            return ContrastCheckResult(
                fgName = fgName,
                bgName = bgName,
                fgHex = fgHex,
                bgHex = bgHex,
                ratio = 1.0,
                size = size,
                passAa = false,
                passAaa = false,
                error = "Invalid hex color format"
            )
        }

        val r = ratio(fgHex, bgHex)
        val (neededAa, neededAaa) = when (size) {
            "large" -> Pair(3.0, 4.5)
            "ui" -> Pair(3.0, 3.0)
            else -> Pair(4.5, 7.0) // normal text
        }

        return ContrastCheckResult(
            fgName = fgName,
            bgName = bgName,
            fgHex = fgHex,
            bgHex = bgHex,
            ratio = r,
            size = size,
            passAa = r >= neededAa,
            passAaa = r >= neededAaa
        )
    }

    fun evaluateAllTokens(tokens: List<DesignToken>): List<ContrastCheckResult> {
        val fgTokens = tokens.filter {
            it.name.contains("text", ignoreCase = true) ||
            it.name.contains("fg", ignoreCase = true) ||
            it.name.contains("on-", ignoreCase = true) ||
            it.role == "text"
        }
        val bgTokens = tokens.filter {
            it.name.contains("bg", ignoreCase = true) ||
            it.name.contains("surface", ignoreCase = true) ||
            it.name.contains("canvas", ignoreCase = true) ||
            it.role == "bg" || it.role == "surface"
        }

        val results = mutableListOf<ContrastCheckResult>()
        for (fg in fgTokens) {
            for (bg in bgTokens) {
                results.add(checkPair(fg.name, bg.name, fg.hex, bg.hex, "normal"))
            }
        }
        return results
    }
}
