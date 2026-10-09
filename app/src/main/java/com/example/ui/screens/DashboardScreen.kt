package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ReplicaViewModel
import com.example.ui.components.ParityDialCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRose
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    viewModel: ReplicaViewModel,
    onNavigateTo3D: () -> Unit,
    onNavigateToClaude: () -> Unit,
    onNavigateToSkills: () -> Unit,
    onNavigateToParity: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToContrast: () -> Unit,
    onNavigateToSweep: () -> Unit,
    onNavigateToListing: () -> Unit,
    onNavigateToPreflight: () -> Unit
) {
    val projects by viewModel.allProjects.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()
    val parityResult by viewModel.parityResult.collectAsState()
    val reviewsResult by viewModel.reviewsAnalysis.collectAsState()
    val contrastResults by viewModel.contrastResults.collectAsState()
    val bugs by viewModel.bugs.collectAsState()

    var showCreateProjectDialog by remember { mutableStateOf(false) }

    val openBlockerCount = bugs.count { !it.isResolved && (it.severity == "S1" || it.severity == "S2") }
    val contrastFails = contrastResults.count { !it.passAa }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    CyanPrimary.copy(alpha = 0.08f),
                                    IndigoAccent.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CyanPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ESPAÇO DE TRABALHO REPLICA",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                    Text(
                                        text = activeProject?.name ?: "Nenhum Projeto Selecionado",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Button(
                                onClick = { showCreateProjectDialog = true },
                                modifier = Modifier.testTag("new_project_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Novo", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeProject != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Aplicativo Alvo: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${activeProject!!.targetApp} (${activeProject!!.targetDomain})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (activeProject?.competitorAngle?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ângulo Matador: \"${activeProject!!.competitorAngle}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Project Switcher
        if (projects.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    projects.forEach { proj ->
                        val isSelected = proj.id == activeProject?.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.clickable { viewModel.selectProject(proj.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = proj.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Parity Dial
        item {
            ParityDialCard(
                score = parityResult.featureScore,
                mustHaveDone = parityResult.mustHaveDone,
                mustHaveTotal = parityResult.mustHaveTotal,
                isShippable = parityResult.isShippable,
                modifier = Modifier.clickable { onNavigateToParity() }
            )
        }

        // 4 Key Stats in Portuguese
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Temas de Dor dos Clientes",
                    value = "${reviewsResult.themes.size}",
                    subtitle = "${reviewsResult.totalReviews} avaliações analisadas",
                    icon = Icons.Default.Psychology,
                    statusColor = IndigoAccent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToReviews() }
                )
                StatCard(
                    title = "Bugs Críticos",
                    value = "$openBlockerCount",
                    subtitle = if (openBlockerCount == 0) "0 S1/S2 abertos" else "Bloqueia lançamento",
                    icon = Icons.Default.BugReport,
                    statusColor = if (openBlockerCount == 0) SuccessEmerald else DangerRose,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPreflight() }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Contraste WCAG 2.2",
                    value = if (contrastFails == 0) "100%" else "$contrastFails Falhas",
                    subtitle = "${contrastResults.size} pares de cores",
                    icon = Icons.Default.ColorLens,
                    statusColor = if (contrastFails == 0) SuccessEmerald else WarningAmber,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToContrast() }
                )
                StatCard(
                    title = "Portão Preflight",
                    value = if (parityResult.isShippable && openBlockerCount == 0 && contrastFails == 0) "APROVADO" else "AUDITAR",
                    subtitle = "6 critérios de liberação",
                    icon = Icons.Default.RocketLaunch,
                    statusColor = if (parityResult.isShippable) SuccessEmerald else CyanPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPreflight() }
                )
            }
        }

        // The 11 Replica Skills in Portuguese
        item {
            SectionHeader(
                title = "As 11 Habilidades do Replica",
                subtitle = "Metodologia completa de engenharia reversa, rebrand e lançamento",
                badgeText = "Metodologia"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSkills() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Recon → 2. Architect → 3. Design → 4. Build → 5. Backend → 6. Test → 7. Diff → 8. Entrepreneur → 9. Brand → 10. Launch → 11. Deploy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explorar habilidades, regras e ferramentas",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Interactive Tool Suite in Portuguese
        item {
            SectionHeader(
                title = "Suíte de Ferramentas Interativas",
                subtitle = "Implementações em tempo real com acesso GitHub, 3D e gerador de APK"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolRowButton(
                    title = "Estúdio 3D & Motor GLB",
                    description = "Visualizador 3D em tempo real, importação de arquivos GLB/OBJ e URL",
                    icon = Icons.Default.ViewInAr,
                    accentColor = CyanPrimary,
                    onClick = onNavigateTo3D
                )
                ToolRowButton(
                    title = "Claude Copilot (Sem Limites & GitHub)",
                    description = "Agente Claude com acesso GitHub, geração de APK e comandos 3D",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = VioletAccent,
                    onClick = onNavigateToClaude
                )
                ToolRowButton(
                    title = "Matriz de Paridade de Recursos (/replica-diff)",
                    description = "Cálculo de paridade ponderada (Must/Should/Could) e áreas mais fracas",
                    icon = Icons.Default.Compare,
                    accentColor = CyanPrimary,
                    onClick = onNavigateToParity
                )
                ToolRowButton(
                    title = "Inteligência de Avaliações (/replica-entrepreneur)",
                    description = "Análise de dores dos usuários e extração do ângulo matador",
                    icon = Icons.Default.Psychology,
                    accentColor = IndigoAccent,
                    onClick = onNavigateToReviews
                )
                ToolRowButton(
                    title = "Verificador de Contraste WCAG 2.2 (/replica-design)",
                    description = "Luminância relativa e conformidade de acessibilidade AA/AAA",
                    icon = Icons.Default.ColorLens,
                    accentColor = VioletAccent,
                    onClick = onNavigateToContrast
                )
                ToolRowButton(
                    title = "Varredura de Marca Clean-Room (/replica-brand)",
                    description = "Escanear código contra nomes, domínios e cores do concorrente",
                    icon = Icons.Default.Security,
                    accentColor = WarningAmber,
                    onClick = onNavigateToSweep
                )
                ToolRowButton(
                    title = "Linter de Listagem de Loja (/replica-launch)",
                    description = "Validação de limites de caracteres e regras anti-rejeição de loja",
                    icon = Icons.Default.RocketLaunch,
                    accentColor = SuccessEmerald,
                    onClick = onNavigateToListing
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCreateProjectDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateProjectDialog = false },
            onCreate = { name, targetApp, domain, colors, cat, desc, angle ->
                viewModel.createProject(name, targetApp, domain, colors, cat, desc, angle)
                showCreateProjectDialog = false
            }
        )
    }
}

@Composable
fun ToolRowButton(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, List<String>, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetApp by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Produtividade") }
    var description by remember { mutableStateOf("") }
    var angle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar Projeto no Replica Studio", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da sua Réplica (ex: MeetFlow)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_proj_name_input")
                )
                OutlinedTextField(
                    value = targetApp,
                    onValueChange = { targetApp = it },
                    label = { Text("Aplicativo Concorrente (ex: Calendly)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domínio do Concorrente (ex: calendly.com)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = angle,
                    onValueChange = { angle = it },
                    label = { Text("Ângulo Matador / Proposta de Valor") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && targetApp.isNotBlank()) {
                        onCreate(name, targetApp, domain, listOf("#006BFF"), category, description, angle)
                    }
                },
                enabled = name.isNotBlank() && targetApp.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Criar Projeto", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
