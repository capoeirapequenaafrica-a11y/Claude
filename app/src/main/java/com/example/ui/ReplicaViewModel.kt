package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BugItem
import com.example.data.model.ContrastCheckResult
import com.example.data.model.DesignToken
import com.example.data.model.FeatureItem
import com.example.data.model.LintIssue
import com.example.data.model.PreflightStatus
import com.example.data.model.ReplicaProject
import com.example.data.model.ReviewItem
import com.example.data.model.StoreListingData
import com.example.data.model.SweepHit
import com.example.data.repository.ProjectRepository
import com.example.engine.ContrastEngine
import com.example.engine.ListingLinterEngine
import com.example.engine.ParityEngine
import com.example.engine.ParityResult
import com.example.engine.PreflightEngine
import com.example.engine.ReviewsAnalysisResult
import com.example.engine.ReviewsEngine
import com.example.engine.SweepEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ReplicaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProjectRepository(AppDatabase.getInstance(application))

    val allProjects: StateFlow<List<ReplicaProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProjectId = MutableStateFlow<String?>("proj-meetflow")
    val selectedProjectId = _selectedProjectId.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Overview, 1: Skills, 2: Parity, 3: Reviews, 4: Contrast, 5: Sweep, 6: Listing, 7: Preflight
    val activeTab = _activeTab.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    val activeProject: StateFlow<ReplicaProject?> = combine(allProjects, selectedProjectId) { projects, selectedId ->
        projects.firstOrNull { it.id == selectedId } ?: projects.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val features: StateFlow<List<FeatureItem>> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getFeatures(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parityResult: StateFlow<ParityResult> = features.combine(selectedProjectId) { featList, _ ->
        ParityEngine.calculateParity(featList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ParityEngine.calculateParity(emptyList())
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val reviews: StateFlow<List<ReviewItem>> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getReviews(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviewsAnalysis: StateFlow<ReviewsAnalysisResult> = reviews.combine(selectedProjectId) { revList, _ ->
        ReviewsEngine.analyzeReviews(revList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReviewsEngine.analyzeReviews(emptyList())
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tokens: StateFlow<List<DesignToken>> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getTokens(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contrastResults: StateFlow<List<ContrastCheckResult>> = tokens.combine(selectedProjectId) { tokenList, _ ->
        ContrastEngine.evaluateAllTokens(tokenList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val bugs: StateFlow<List<BugItem>> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getBugs(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val storeListing: StateFlow<StoreListingData?> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getListing(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val listingIssues: StateFlow<List<LintIssue>> = storeListing.combine(selectedProjectId) { listing, _ ->
        if (listing != null) ListingLinterEngine.lintListing(listing) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sweep interactive state
    private val _sweepCodeSnippet = MutableStateFlow(
        "// Sample app implementation code\n" +
        "class CalendlySyncService {\n" +
        "    val endpoint = \"https://api.calendly.com/v1/users\"\n" +
        "    val brandPrimary = \"#006BFF\"\n" +
        "    fun schedule() { /* sync */ }\n" +
        "}"
    )
    val sweepCodeSnippet = _sweepCodeSnippet.asStateFlow()

    private val _sweepHits = MutableStateFlow<List<SweepHit>>(emptyList())
    val sweepHits = _sweepHits.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            runSweepOnCurrentSnippet()
        }
    }

    fun selectProject(projectId: String) {
        _selectedProjectId.value = projectId
        viewModelScope.launch {
            runSweepOnCurrentSnippet()
        }
    }

    fun updateSweepCodeSnippet(newSnippet: String) {
        _sweepCodeSnippet.value = newSnippet
        runSweepOnCurrentSnippet()
    }

    fun runSweepOnCurrentSnippet() {
        val proj = activeProject.value
        val avoid = if (proj != null) listOf(proj.targetApp) else listOf("Calendly")
        val domains = if (proj != null && proj.targetDomain.isNotBlank()) listOf(proj.targetDomain) else listOf("calendly.com")
        val colors = proj?.targetColors ?: listOf("#006BFF", "#0AE8F0")

        _sweepHits.value = SweepEngine.performSweep(
            content = _sweepCodeSnippet.value,
            avoidNames = avoid,
            domains = domains,
            colors = colors
        )
    }

    fun calculatePreflightStatus(): PreflightStatus {
        return PreflightEngine.evaluatePreflight(
            parityResult = parityResult.value,
            bugs = bugs.value,
            contrastResults = contrastResults.value,
            sweepHits = sweepHits.value,
            listingIssues = listingIssues.value
        )
    }

    // Actions
    fun updateFeatureStatus(featureId: String, newStatus: String) {
        viewModelScope.launch {
            val item = features.value.firstOrNull { it.id == featureId } ?: return@launch
            repository.updateFeature(item.copy(cloneStatus = newStatus))
        }
    }

    fun addFeature(feature: String, area: String, priority: String, original: Boolean, notes: String) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            val item = FeatureItem(
                id = "feat-${UUID.randomUUID()}",
                projectId = projId,
                feature = feature,
                area = area,
                priority = priority,
                original = original,
                cloneStatus = "no",
                notes = notes
            )
            repository.addFeature(item)
        }
    }

    fun deleteFeature(feature: FeatureItem) {
        viewModelScope.launch {
            repository.deleteFeature(feature)
        }
    }

    fun addReview(source: String, url: String, rating: Int?, text: String) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            val item = ReviewItem(
                id = "rev-${UUID.randomUUID()}",
                projectId = projId,
                source = source,
                url = url,
                rating = rating,
                text = text,
                date = "2026-10-09"
            )
            repository.addReview(item)
        }
    }

    fun deleteReview(review: ReviewItem) {
        viewModelScope.launch {
            repository.deleteReview(review)
        }
    }

    fun addToken(name: String, hex: String, role: String) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            val item = DesignToken(
                id = "tok-${UUID.randomUUID()}",
                projectId = projId,
                name = name,
                hex = hex,
                role = role
            )
            repository.addToken(item)
        }
    }

    fun deleteToken(token: DesignToken) {
        viewModelScope.launch {
            repository.deleteToken(token)
        }
    }

    fun toggleBugResolved(bug: BugItem) {
        viewModelScope.launch {
            repository.updateBug(bug.copy(isResolved = !bug.isResolved))
        }
    }

    fun addBug(title: String, flowArea: String, severity: String, notes: String) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            val item = BugItem(
                id = "bug-${UUID.randomUUID()}",
                projectId = projId,
                title = title,
                flowArea = flowArea,
                severity = severity,
                isResolved = false,
                notes = notes
            )
            repository.addBug(item)
        }
    }

    fun deleteBug(bug: BugItem) {
        viewModelScope.launch {
            repository.deleteBug(bug)
        }
    }

    fun updateListing(data: StoreListingData) {
        viewModelScope.launch {
            repository.saveListing(data)
        }
    }

    fun createProject(
        name: String,
        targetApp: String,
        targetDomain: String,
        targetColors: List<String>,
        category: String,
        description: String,
        angle: String
    ) {
        viewModelScope.launch {
            val newId = "proj-${UUID.randomUUID().toString().take(8)}"
            val project = ReplicaProject(
                id = newId,
                name = name,
                targetApp = targetApp,
                targetDomain = targetDomain,
                targetColors = targetColors,
                category = category,
                description = description,
                competitorAngle = angle
            )
            repository.addProject(project)
            _selectedProjectId.value = newId
        }
    }
}
