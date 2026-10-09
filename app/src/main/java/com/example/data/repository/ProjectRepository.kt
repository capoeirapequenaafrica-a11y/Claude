package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.BugEntity
import com.example.data.local.FeatureEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.ReviewEntity
import com.example.data.local.StoreListingEntity
import com.example.data.local.TokenEntity
import com.example.data.model.BugItem
import com.example.data.model.DesignToken
import com.example.data.model.FeatureItem
import com.example.data.model.ReplicaProject
import com.example.data.model.ReviewItem
import com.example.data.model.StoreListingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val database: AppDatabase) {
    private val dao = database.replicaDao()

    val allProjects: Flow<List<ReplicaProject>> = dao.getAllProjects().map { list ->
        list.map { it.toDomain() }
    }

    fun getFeatures(projectId: String): Flow<List<FeatureItem>> =
        dao.getFeaturesForProject(projectId).map { list -> list.map { it.toDomain() } }

    fun getReviews(projectId: String): Flow<List<ReviewItem>> =
        dao.getReviewsForProject(projectId).map { list -> list.map { it.toDomain() } }

    fun getTokens(projectId: String): Flow<List<DesignToken>> =
        dao.getTokensForProject(projectId).map { list -> list.map { it.toDomain() } }

    fun getBugs(projectId: String): Flow<List<BugItem>> =
        dao.getBugsForProject(projectId).map { list -> list.map { it.toDomain() } }

    fun getListing(projectId: String): Flow<StoreListingData?> =
        dao.getListingForProject(projectId).map { it?.toDomain() }

    suspend fun addProject(project: ReplicaProject) {
        dao.insertProject(project.toEntity())
    }

    suspend fun updateProject(project: ReplicaProject) {
        dao.updateProject(project.toEntity())
    }

    suspend fun deleteProject(project: ReplicaProject) {
        dao.deleteProject(project.toEntity())
    }

    suspend fun addFeature(feature: FeatureItem) {
        dao.insertFeature(feature.toEntity())
    }

    suspend fun updateFeature(feature: FeatureItem) {
        dao.updateFeature(feature.toEntity())
    }

    suspend fun deleteFeature(feature: FeatureItem) {
        dao.deleteFeature(feature.toEntity())
    }

    suspend fun addReview(review: ReviewItem) {
        dao.insertReview(review.toEntity())
    }

    suspend fun deleteReview(review: ReviewItem) {
        dao.deleteReview(review.toEntity())
    }

    suspend fun addToken(token: DesignToken) {
        dao.insertToken(token.toEntity())
    }

    suspend fun deleteToken(token: DesignToken) {
        dao.deleteToken(token.toEntity())
    }

    suspend fun addBug(bug: BugItem) {
        dao.insertBug(bug.toEntity())
    }

    suspend fun updateBug(bug: BugItem) {
        dao.updateBug(bug.toEntity())
    }

    suspend fun deleteBug(bug: BugItem) {
        dao.deleteBug(bug.toEntity())
    }

    suspend fun saveListing(listing: StoreListingData) {
        dao.insertListing(listing.toEntity())
    }

    suspend fun seedInitialDataIfEmpty() {
        // Will check if empty and seed initial data
        val sampleProjects = getSampleProjects()
        dao.insertProjects(sampleProjects.map { it.toEntity() })

        val sampleFeatures = getSampleFeatures()
        dao.insertFeatures(sampleFeatures.map { it.toEntity() })

        val sampleReviews = getSampleReviews()
        dao.insertReviews(sampleReviews.map { it.toEntity() })

        val sampleTokens = getSampleTokens()
        dao.insertTokens(sampleTokens.map { it.toEntity() })

        val sampleBugs = getSampleBugs()
        dao.insertBugs(sampleBugs.map { it.toEntity() })

        getSampleListings().forEach {
            dao.insertListing(it.toEntity())
        }
    }

    // Mapping extensions
    private fun ProjectEntity.toDomain() = ReplicaProject(
        id = id,
        name = name,
        targetApp = targetApp,
        targetDomain = targetDomain,
        targetColors = if (targetColors.isBlank()) emptyList() else targetColors.split(","),
        category = category,
        description = description,
        competitorAngle = competitorAngle,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ReplicaProject.toEntity() = ProjectEntity(
        id = id,
        name = name,
        targetApp = targetApp,
        targetDomain = targetDomain,
        targetColors = targetColors.joinToString(","),
        category = category,
        description = description,
        competitorAngle = competitorAngle,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun FeatureEntity.toDomain() = FeatureItem(
        id = id,
        projectId = projectId,
        feature = feature,
        area = area,
        priority = priority,
        original = original,
        cloneStatus = cloneStatus,
        notes = notes
    )

    private fun FeatureItem.toEntity() = FeatureEntity(
        id = id,
        projectId = projectId,
        feature = feature,
        area = area,
        priority = priority,
        original = original,
        cloneStatus = cloneStatus,
        notes = notes
    )

    private fun ReviewEntity.toDomain() = ReviewItem(
        id = id,
        projectId = projectId,
        source = source,
        url = url,
        rating = rating,
        text = text,
        date = date
    )

    private fun ReviewItem.toEntity() = ReviewEntity(
        id = id,
        projectId = projectId,
        source = source,
        url = url,
        rating = rating,
        text = text,
        date = date
    )

    private fun TokenEntity.toDomain() = DesignToken(
        id = id,
        projectId = projectId,
        name = name,
        hex = hex,
        role = role
    )

    private fun DesignToken.toEntity() = TokenEntity(
        id = id,
        projectId = projectId,
        name = name,
        hex = hex,
        role = role
    )

    private fun BugEntity.toDomain() = BugItem(
        id = id,
        projectId = projectId,
        title = title,
        flowArea = flowArea,
        severity = severity,
        isResolved = isResolved,
        notes = notes
    )

    private fun BugItem.toEntity() = BugEntity(
        id = id,
        projectId = projectId,
        title = title,
        flowArea = flowArea,
        severity = severity,
        isResolved = isResolved,
        notes = notes
    )

    private fun StoreListingEntity.toDomain() = StoreListingData(
        projectId = projectId,
        appStoreName = appStoreName,
        appStoreSubtitle = appStoreSubtitle,
        appStoreKeywords = appStoreKeywords,
        appStoreDescription = appStoreDescription,
        playStoreTitle = playStoreTitle,
        playStoreShortDesc = playStoreShortDesc,
        playStoreFullDesc = playStoreFullDesc,
        avoidNames = if (avoidNames.isBlank()) emptyList() else avoidNames.split(",")
    )

    private fun StoreListingData.toEntity() = StoreListingEntity(
        projectId = projectId,
        appStoreName = appStoreName,
        appStoreSubtitle = appStoreSubtitle,
        appStoreKeywords = appStoreKeywords,
        appStoreDescription = appStoreDescription,
        playStoreTitle = playStoreTitle,
        playStoreShortDesc = playStoreShortDesc,
        playStoreFullDesc = playStoreFullDesc,
        avoidNames = avoidNames.joinToString(",")
    )

    companion object {
        fun getSampleProjects(): List<ReplicaProject> = listOf(
            ReplicaProject(
                id = "proj-meetflow",
                name = "MeetFlow",
                targetApp = "Calendly",
                targetDomain = "calendly.com",
                targetColors = listOf("#006BFF", "#0AE8F0"),
                category = "Produtividade & Agendamento",
                description = "Clone clean-room de agendamento de reuniões com preço fixo por equipe e trava nativa de fuso horário.",
                competitorAngle = "Links de agendamento para equipes enxutas que odeiam aumentos drásticos de preço por usuário e convites com fuso horário quebrado."
            ),
            ReplicaProject(
                id = "proj-flownotes",
                name = "FlowNotes",
                targetApp = "Notion",
                targetDomain = "notion.so",
                targetColors = listOf("#000000", "#FFFFFF", "#2F3437"),
                category = "Produtividade / Notas",
                description = "Editor em blocos offline-first com carregamento instantâneo e zero paywalls de nuvem.",
                competitorAngle = "Espaço de trabalho markdown ultrarrápido, local-first, sem lentidão e sem aprisionamento em nuvem."
            )
        )

        fun getSampleFeatures(): List<FeatureItem> = listOf(
            FeatureItem(
                id = "feat-1",
                projectId = "proj-meetflow",
                feature = "Convidado seleciona data e horário disponível",
                area = "Fluxo de Reserva",
                priority = "must",
                original = true,
                cloneStatus = "yes",
                notes = "Calendário interativo principal"
            ),
            FeatureItem(
                id = "feat-2",
                projectId = "proj-meetflow",
                feature = "Anfitrião conecta com Google Agenda",
                area = "Integrações",
                priority = "must",
                original = true,
                cloneStatus = "yes",
                notes = "Sincronização OAuth concluída"
            ),
            FeatureItem(
                id = "feat-3",
                projectId = "proj-meetflow",
                feature = "Anfitrião define janelas semanais de disponibilidade",
                area = "Painel do Anfitrião",
                priority = "must",
                original = true,
                cloneStatus = "yes",
                notes = "Bloqueios recorrentes"
            ),
            FeatureItem(
                id = "feat-4",
                projectId = "proj-meetflow",
                feature = "Trava contra colisão e agendamento duplo",
                area = "Fluxo de Reserva",
                priority = "must",
                original = true,
                cloneStatus = "yes",
                notes = "Bloqueio atômico de slot no banco de dados"
            ),
            FeatureItem(
                id = "feat-5",
                projectId = "proj-meetflow",
                feature = "Lembretes de compromisso por SMS e E-mail",
                area = "Notificações",
                priority = "should",
                original = true,
                cloneStatus = "partial",
                notes = "Webhooks Twilio e Resend em andamento"
            ),
            FeatureItem(
                id = "feat-6",
                projectId = "proj-meetflow",
                feature = "Modal explícito de confirmação de fuso horário do convidado",
                area = "Fluxo de Reserva",
                priority = "must",
                original = false, // Melhoria competitiva!
                cloneStatus = "yes",
                notes = "Melhoria estratégica: elimina confusão de fuso horário"
            ),
            FeatureItem(
                id = "feat-7",
                projectId = "proj-meetflow",
                feature = "Plano fixo para equipe com usuários ilimitados",
                area = "Faturamento",
                priority = "should",
                original = false,
                cloneStatus = "yes",
                notes = "Resolve a principal reclamação encontrada nas avaliações"
            ),
            FeatureItem(
                id = "feat-8",
                projectId = "proj-meetflow",
                feature = "Distribuição de reuniões em rodízio (round-robin)",
                area = "Agendamento em Equipe",
                priority = "could",
                original = true,
                cloneStatus = "no",
                notes = "Planejado para versão v1.1"
            ),
            FeatureItem(
                id = "feat-9",
                projectId = "proj-meetflow",
                feature = "Integração Enterprise SSO SAML",
                area = "Autenticação",
                priority = "could",
                original = true,
                cloneStatus = "skip",
                notes = "Fora do escopo para MVP da equipe independente"
            ),
            // Recursos do FlowNotes
            FeatureItem(
                id = "feat-fn-1",
                projectId = "proj-flownotes",
                feature = "Menu de comandos de bloco por barra (/)",
                area = "Editor",
                priority = "must",
                original = true,
                cloneStatus = "yes",
                notes = "Títulos, listas, código, citações"
            ),
            FeatureItem(
                id = "feat-fn-2",
                projectId = "proj-flownotes",
                feature = "Armazenamento local offline instantâneo",
                area = "Armazenamento",
                priority = "must",
                original = false,
                cloneStatus = "yes",
                notes = "Zero latência de nuvem"
            )
        )

        fun getSampleReviews(): List<ReviewItem> = listOf(
            ReviewItem(
                id = "rev-1",
                projectId = "proj-meetflow",
                source = "G2",
                url = "https://g2.com/products/calendly/reviews/101",
                rating = 2,
                text = "O preço por usuário dispara drasticamente ao adicionar apenas 2 colegas. Ficou inacessível para nossa agência de 5 pessoas.",
                date = "2026-05-14"
            ),
            ReviewItem(
                id = "rev-2",
                projectId = "proj-meetflow",
                source = "App Store",
                url = "https://apps.apple.com/app/calendly/id1234#rev2",
                rating = 1,
                text = "Os clientes sempre se confundem com fusos horários ao agendar reuniões internacionais. Não há confirmação destacada.",
                date = "2026-06-20"
            ),
            ReviewItem(
                id = "rev-3",
                projectId = "proj-meetflow",
                source = "Reddit",
                url = "https://reddit.com/r/productivity/comments/sched1",
                rating = 2,
                text = "Por que os lembretes por SMS estão travados atrás de planos empresariais caros? A taxa de faltas continua alta sem SMS.",
                date = "2026-07-02"
            ),
            ReviewItem(
                id = "rev-4",
                projectId = "proj-meetflow",
                source = "Capterra",
                url = "https://capterra.com/reviews/calendly/303",
                rating = 4,
                text = "Ótima integração de calendário e configuração simples, mas um choque na fatura ao expandir com prestadores de serviços.",
                date = "2026-08-11"
            )
        )

        fun getSampleTokens(): List<DesignToken> = listOf(
            DesignToken("tok-1", "proj-meetflow", "color-text-primary", "#0F172A", "text"),
            DesignToken("tok-2", "proj-meetflow", "color-text-muted", "#64748B", "text"),
            DesignToken("tok-3", "proj-meetflow", "color-bg-canvas", "#FFFFFF", "bg"),
            DesignToken("tok-4", "proj-meetflow", "color-bg-surface", "#F8FAFC", "surface"),
            DesignToken("tok-5", "proj-meetflow", "color-brand-primary", "#4F46E5", "accent"),
            DesignToken("tok-6", "proj-meetflow", "color-text-on-brand", "#FFFFFF", "text")
        )

        fun getSampleBugs(): List<BugItem> = listOf(
            BugItem(
                id = "bug-1",
                projectId = "proj-meetflow",
                title = "Caso de borda com clique duplo simultâneo na reserva",
                flowArea = "Fluxo de Reserva",
                severity = "S2",
                isResolved = true,
                notes = "Resolvido com trava de transação atômica no banco de dados"
            ),
            BugItem(
                id = "bug-2",
                projectId = "proj-meetflow",
                title = "Deslocamento de horário durante horário de verão",
                flowArea = "Integrações",
                severity = "S2",
                isResolved = true,
                notes = "Resolvido com java.time.ZonedDateTime"
            ),
            BugItem(
                id = "bug-3",
                projectId = "proj-meetflow",
                title = "Espaçamento do ícone de avatar em telas compactas",
                flowArea = "Configurações",
                severity = "S4",
                isResolved = false,
                notes = "Ajuste fino de interface"
            )
        )

        fun getSampleListings(): List<StoreListingData> = listOf(
            StoreListingData(
                projectId = "proj-meetflow",
                appStoreName = "MeetFlow",
                appStoreSubtitle = "Agendamentos em Equipe",
                appStoreKeywords = "reuniao,calendario,agendamento,horarios,reserva,sync",
                appStoreDescription = "O MeetFlow simplifica compromissos e reservas em equipe sem cobranças abusivas por usuário. Conecte sua Google Agenda e compartilhe links elegantes.",
                playStoreTitle = "MeetFlow",
                playStoreShortDesc = "Agendamento ágil de reuniões em equipe com trava automática de fuso horário.",
                playStoreFullDesc = "O MeetFlow foi feito para profissionais e equipes que precisam de links práticos de agendamento. Compartilhe sua agenda, deixe seus clientes escolherem no fuso horário deles e evite reservas duplicadas.",
                avoidNames = listOf("Calendly", "Acuity Scheduling")
            )
        )
    }
}
