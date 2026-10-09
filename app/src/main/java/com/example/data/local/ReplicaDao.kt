package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplicaDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // Features
    @Query("SELECT * FROM features WHERE projectId = :projectId")
    fun getFeaturesForProject(projectId: String): Flow<List<FeatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeature(feature: FeatureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatures(features: List<FeatureEntity>)

    @Update
    suspend fun updateFeature(feature: FeatureEntity)

    @Delete
    suspend fun deleteFeature(feature: FeatureEntity)

    // Reviews
    @Query("SELECT * FROM reviews WHERE projectId = :projectId")
    fun getReviewsForProject(projectId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Delete
    suspend fun deleteReview(review: ReviewEntity)

    // Tokens
    @Query("SELECT * FROM tokens WHERE projectId = :projectId")
    fun getTokensForProject(projectId: String): Flow<List<TokenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: TokenEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens: List<TokenEntity>)

    @Delete
    suspend fun deleteToken(token: TokenEntity)

    // Bugs
    @Query("SELECT * FROM bugs WHERE projectId = :projectId")
    fun getBugsForProject(projectId: String): Flow<List<BugEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBug(bug: BugEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBugs(bugs: List<BugEntity>)

    @Update
    suspend fun updateBug(bug: BugEntity)

    @Delete
    suspend fun deleteBug(bug: BugEntity)

    // Listings
    @Query("SELECT * FROM listings WHERE projectId = :projectId")
    fun getListingForProject(projectId: String): Flow<StoreListingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: StoreListingEntity)
}
