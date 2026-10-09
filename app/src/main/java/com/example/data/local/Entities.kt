package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetApp: String,
    val targetDomain: String,
    val targetColors: String, // comma separated hex
    val category: String,
    val description: String,
    val competitorAngle: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "features")
data class FeatureEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val feature: String,
    val area: String,
    val priority: String, // must, should, could
    val original: Boolean,
    val cloneStatus: String, // yes, partial, no, skip
    val notes: String
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val source: String,
    val url: String,
    val rating: Int?,
    val text: String,
    val date: String
)

@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val hex: String,
    val role: String
)

@Entity(tableName = "bugs")
data class BugEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val flowArea: String,
    val severity: String,
    val isResolved: Boolean,
    val notes: String
)

@Entity(tableName = "listings")
data class StoreListingEntity(
    @PrimaryKey val projectId: String,
    val appStoreName: String,
    val appStoreSubtitle: String,
    val appStoreKeywords: String,
    val appStoreDescription: String,
    val playStoreTitle: String,
    val playStoreShortDesc: String,
    val playStoreFullDesc: String,
    val avoidNames: String
)
