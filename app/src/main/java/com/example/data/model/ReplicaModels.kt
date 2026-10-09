package com.example.data.model

data class ReplicaProject(
    val id: String,
    val name: String,
    val targetApp: String,
    val targetDomain: String,
    val targetColors: List<String> = emptyList(),
    val category: String,
    val description: String,
    val competitorAngle: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FeatureItem(
    val id: String,
    val projectId: String,
    val feature: String,
    val area: String,
    val priority: String, // "must", "should", "could"
    val original: Boolean = true,
    val cloneStatus: String, // "yes", "partial", "no", "skip"
    val notes: String = ""
)

data class ReviewItem(
    val id: String,
    val projectId: String,
    val source: String, // "App Store", "G2", "Capterra", "Reddit"
    val url: String,
    val rating: Int?, // 1..5 or null
    val text: String,
    val date: String = "" // YYYY-MM-DD
)

data class DesignToken(
    val id: String,
    val projectId: String,
    val name: String,
    val hex: String,
    val role: String // "text", "bg", "accent", "surface"
)

data class ContrastCheckResult(
    val fgName: String,
    val bgName: String,
    val fgHex: String,
    val bgHex: String,
    val ratio: Double,
    val size: String, // "normal", "large", "ui"
    val passAa: Boolean,
    val passAaa: Boolean,
    val error: String? = null
)

data class StoreListingData(
    val projectId: String,
    val appStoreName: String = "",
    val appStoreSubtitle: String = "",
    val appStoreKeywords: String = "",
    val appStoreDescription: String = "",
    val playStoreTitle: String = "",
    val playStoreShortDesc: String = "",
    val playStoreFullDesc: String = "",
    val avoidNames: List<String> = emptyList()
)

data class LintIssue(
    val level: String, // "error", "warn"
    val store: String, // "app_store", "google_play", "-"
    val field: String,
    val message: String
)

data class SweepHit(
    val kind: String, // "name", "domain", "color", "path"
    val match: String,
    val location: String,
    val snippet: String
)

data class BugItem(
    val id: String,
    val projectId: String,
    val title: String,
    val flowArea: String,
    val severity: String, // "S1", "S2", "S3", "S4"
    val isResolved: Boolean = false,
    val notes: String = ""
)

data class PreflightStatus(
    val passed: Boolean,
    val parityDone: Boolean,
    val parityScore: Double,
    val mustHavesDone: Int,
    val mustHavesTotal: Int,
    val openBlockers: Int,
    val contrastPassed: Boolean,
    val sweepClean: Boolean,
    val listingValid: Boolean,
    val checklist: List<PreflightItem>
)

data class PreflightItem(
    val title: String,
    val description: String,
    val passed: Boolean,
    val isRequired: Boolean = true
)
