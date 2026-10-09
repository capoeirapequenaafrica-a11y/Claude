package com.example.engine

import com.example.data.model.FeatureItem
import kotlin.math.roundToInt

data class ParityResult(
    val featureScore: Double,
    val counted: Int,
    val mustHaveDone: Int,
    val mustHaveTotal: Int,
    val isShippable: Boolean,
    val byArea: List<AreaScore>,
    val missing: List<FeatureItem>,
    val skipped: List<FeatureItem>,
    val extras: List<FeatureItem>,
    val problems: List<String>
)

data class AreaScore(
    val area: String,
    val score: Double,
    val featuresCount: Int
)

object ParityEngine {
    private val WEIGHT = mapOf(
        "must" to 3, "should" to 2, "could" to 1,
        "p0" to 3, "p1" to 2, "p2" to 1
    )

    private val CREDIT = mapOf(
        "yes" to 1.0, "done" to 1.0,
        "partial" to 0.5,
        "no" to 0.0, "" to 0.0, "todo" to 0.0
    )

    fun calculateParity(features: List<FeatureItem>): ParityResult {
        val counted = mutableListOf<Triple<FeatureItem, Int, Double>>() // item, weight, credit
        val skipped = mutableListOf<FeatureItem>()
        val extras = mutableListOf<FeatureItem>()
        val problems = mutableListOf<String>()

        for (item in features) {
            if (item.feature.isBlank()) continue

            // If feature wasn't in original, it's an extra (doesn't count towards parity score)
            if (!item.original) {
                extras.add(item)
                continue
            }

            val clone = item.cloneStatus.lowercase().trim()
            if (clone == "skip") {
                skipped.add(item)
                continue
            }

            val prio = item.priority.lowercase().trim()
            val w = WEIGHT[prio] ?: run {
                problems.add("Priority '${item.priority}' for '${item.feature}' counted as 'could'")
                1
            }

            val c = CREDIT[clone] ?: run {
                problems.add("Clone status '${item.cloneStatus}' for '${item.feature}' counted as 0.0")
                0.0
            }

            counted.add(Triple(item, w, c))
        }

        val totalWeight = counted.sumOf { it.second }
        val earnedWeight = counted.sumOf { it.second * it.third }

        val rawScore = if (totalWeight > 0) (100.0 * earnedWeight / totalWeight) else 0.0
        val featureScore = (rawScore * 10.0).roundToInt() / 10.0

        val areaMap = mutableMapOf<String, Pair<Double, Double>>() // earned, total
        val areaCounts = mutableMapOf<String, Int>()

        for ((item, w, c) in counted) {
            val a = if (item.area.isBlank()) "(Uncategorized)" else item.area
            val current = areaMap.getOrDefault(a, Pair(0.0, 0.0))
            areaMap[a] = Pair(current.first + w * c, current.second + w)
            areaCounts[a] = areaCounts.getOrDefault(a, 0) + 1
        }

        val byArea = areaMap.map { (area, pair) ->
            val areaScore = if (pair.second > 0) ((100.0 * pair.first / pair.second) * 10.0).roundToInt() / 10.0 else 0.0
            AreaScore(area, areaScore, areaCounts[area] ?: 0)
        }.sortedBy { it.score } // Weakest area first, as per replica-diff spec

        val musts = counted.filter { it.second == 3 }
        val mustHaveTotal = musts.size
        val mustHaveDone = musts.count { it.third == 1.0 }

        val missing = counted.filter { it.third < 1.0 }
            .sortedWith(compareByDescending<Triple<FeatureItem, Int, Double>> { it.second }
                .thenBy { it.third }
                .thenBy { it.first.area }
                .thenBy { it.first.feature })
            .map { it.first }

        val isShippable = mustHaveDone >= mustHaveTotal && featureScore >= 80.0

        return ParityResult(
            featureScore = featureScore,
            counted = counted.size,
            mustHaveDone = mustHaveDone,
            mustHaveTotal = mustHaveTotal,
            isShippable = isShippable,
            byArea = byArea,
            missing = missing,
            skipped = skipped,
            extras = extras,
            problems = problems
        )
    }
}
