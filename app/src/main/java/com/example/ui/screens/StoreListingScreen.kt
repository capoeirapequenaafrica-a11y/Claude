package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LintIssue
import com.example.data.model.StoreListingData
import com.example.ui.ReplicaViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRose
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun StoreListingScreen(viewModel: ReplicaViewModel) {
    val storeListing by viewModel.storeListing.collectAsState()
    val lintIssues by viewModel.listingIssues.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    var activeStoreTab by remember { mutableIntStateOf(0) } // 0: Google Play, 1: App Store

    val currentData = storeListing ?: StoreListingData(
        projectId = activeProject?.id ?: "proj-meetflow",
        avoidNames = if (activeProject != null) listOf(activeProject!!.targetApp) else listOf("Calendly")
    )

    var playTitle by remember(currentData) { mutableStateOf(currentData.playStoreTitle) }
    var playShortDesc by remember(currentData) { mutableStateOf(currentData.playStoreShortDesc) }
    var playFullDesc by remember(currentData) { mutableStateOf(currentData.playStoreFullDesc) }

    var appStoreName by remember(currentData) { mutableStateOf(currentData.appStoreName) }
    var appStoreSubtitle by remember(currentData) { mutableStateOf(currentData.appStoreSubtitle) }
    var appStoreKeywords by remember(currentData) { mutableStateOf(currentData.appStoreKeywords) }
    var appStoreDesc by remember(currentData) { mutableStateOf(currentData.appStoreDescription) }

    val errorCount = lintIssues.count { it.level == "error" }
    val warnCount = lintIssues.count { it.level == "warn" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Linter de Listagem da Loja",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "/replica-launch • Validador de Metadados da Google Play & App Store",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        val updated = currentData.copy(
                            playStoreTitle = playTitle,
                            playStoreShortDesc = playShortDesc,
                            playStoreFullDesc = playFullDesc,
                            appStoreName = appStoreName,
                            appStoreSubtitle = appSubtitleFormat(appStoreSubtitle),
                            appStoreKeywords = appStoreKeywords,
                            appStoreDescription = appStoreDesc
                        )
                        viewModel.updateListing(updated)
                    },
                    modifier = Modifier.testTag("save_listing_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Compliance status summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STATUS DE VALIDAÇÃO DE METADADOS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (errorCount == 0) "Pronto para Envio às Lojas" else "$errorCount Erro(s) de Política",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (errorCount == 0) SuccessEmerald else DangerRose
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (errorCount > 0) DangerRose.copy(alpha = 0.15f) else SuccessEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$errorCount Erros",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (errorCount > 0) DangerRose else SuccessEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarningAmber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$warnCount Alertas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Store tabs selector
        item {
            TabRow(
                selectedTabIndex = activeStoreTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = CyanPrimary
            ) {
                Tab(
                    selected = activeStoreTab == 0,
                    onClick = { activeStoreTab = 0 },
                    text = { Text("Google Play Store (Android)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeStoreTab == 1,
                    onClick = { activeStoreTab = 1 },
                    text = { Text("Apple App Store (iOS)", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (activeStoreTab == 0) {
            // Google Play fields
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "METADADOS DO GOOGLE PLAY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )

                        Column {
                            OutlinedTextField(
                                value = playTitle,
                                onValueChange = { playTitle = it },
                                label = { Text("Título do App (Máx. 30 caracteres)") },
                                modifier = Modifier.fillMaxWidth().testTag("play_title_input"),
                                singleLine = true
                            )
                            Text(
                                text = "${playTitle.length} / 30 caracteres",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (playTitle.length > 30) DangerRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                            )
                        }

                        Column {
                            OutlinedTextField(
                                value = playShortDesc,
                                onValueChange = { playShortDesc = it },
                                label = { Text("Descrição Curta (Máx. 80 caracteres)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                            Text(
                                text = "${playShortDesc.length} / 80 caracteres",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (playShortDesc.length > 80) DangerRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                            )
                        }

                        Column {
                            OutlinedTextField(
                                value = playFullDesc,
                                onValueChange = { playFullDesc = it },
                                label = { Text("Descrição Completa (Máx. 4000 caracteres)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4
                            )
                            Text(
                                text = "${playFullDesc.length} / 4000 caracteres",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (playFullDesc.length > 4000) DangerRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // App Store fields
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "METADADOS DA APPLE APP STORE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndigoAccent
                        )

                        Column {
                            OutlinedTextField(
                                value = appStoreName,
                                onValueChange = { appStoreName = it },
                                label = { Text("Nome do Aplicativo (Máx. 30 caracteres)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Text(
                                text = "${appStoreName.length} / 30 caracteres",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (appStoreName.length > 30) DangerRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                            )
                        }

                        Column {
                            OutlinedTextField(
                                value = appStoreSubtitle,
                                onValueChange = { appStoreSubtitle = it },
                                label = { Text("Subtítulo (Máx. 30 caracteres)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Text(
                                text = "${appStoreSubtitle.length} / 30 caracteres",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (appStoreSubtitle.length > 30) DangerRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                            )
                        }

                        Column {
                            OutlinedTextField(
                                value = appStoreKeywords,
                                onValueChange = { appStoreKeywords = it },
                                label = { Text("Palavras-Chave (Máx. 100 caracteres, sem espaços após vírgulas)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Text(
                                text = "${appStoreKeywords.length} / 100 caracteres",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (appStoreKeywords.length > 100) DangerRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                            )
                        }

                        Column {
                            OutlinedTextField(
                                value = appStoreDesc,
                                onValueChange = { appStoreDesc = it },
                                label = { Text("Descrição Promocional (Máx. 4000 caracteres)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4
                            )
                            Text(
                                text = "${appStoreDesc.length} / 4000 caracteres",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (appStoreDesc.length > 4000) DangerRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Lint issues section
        item {
            SectionHeader(
                title = "Problemas de Auditoria de Políticas (${lintIssues.size})",
                subtitle = "Verificação em tempo real de limites de caracteres, alegações promocionais e termos de concorrentes"
            )
        }

        if (lintIssues.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessEmerald.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Metadados 100% em conformidade com as diretrizes do Google Play e App Store!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessEmerald
                        )
                    }
                }
            }
        } else {
            items(lintIssues) { issue ->
                LintIssueCard(issue = issue)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun appSubtitleFormat(sub: String): String = sub

@Composable
fun LintIssueCard(issue: LintIssue) {
    val isError = issue.level == "error"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = (if (isError) DangerRose else WarningAmber).copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, (if (isError) DangerRose else WarningAmber).copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Warning else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isError) DangerRose else WarningAmber,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isError) DangerRose else WarningAmber
                    ) {
                        Text(
                            text = if (isError) "ERRO" else "ALERTA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isError) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 8.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${issue.store.replace('_', ' ').uppercase()} • ${issue.field}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = issue.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
