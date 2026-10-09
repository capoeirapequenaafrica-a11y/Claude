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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BugItem
import com.example.data.model.PreflightItem
import com.example.ui.ReplicaViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRose
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun DeployPreflightScreen(viewModel: ReplicaViewModel) {
    val activeProject by viewModel.activeProject.collectAsState()
    val bugs by viewModel.bugs.collectAsState()
    val preflightStatus = viewModel.calculatePreflightStatus()

    var showAddBugDialog by remember { mutableStateOf(false) }

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
                        text = "Portão de Pré-Voo de Deploy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "/replica-deploy • Checklist de Certificação para Lançamento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddBugDialog = true },
                    modifier = Modifier.testTag("add_bug_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRose),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reportar Bug", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Master Go / No-Go Decision Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (preflightStatus.passed) SuccessEmerald.copy(alpha = 0.15f) else DangerRose.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (preflightStatus.passed) SuccessEmerald.copy(alpha = 0.5f) else DangerRose.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (preflightStatus.passed) SuccessEmerald else DangerRose,
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (preflightStatus.passed) Icons.Default.RocketLaunch else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (preflightStatus.passed) Color.Black else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "DECISÃO DE PRONTIDÃO DE RELEASE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (preflightStatus.passed) SuccessEmerald else DangerRose,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (preflightStatus.passed) "LANÇAMENTO AUTORIZADO (GO)" else "LANÇAMENTO BLOQUEADO (NO-GO)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (preflightStatus.passed) {
                            "Todas as 6 regras do checklist passaram. Paridade >= 80%, zero bugs bloqueadores S1/S2, contraste WCAG AA aprovado, varredura de marca limpa e listagem da loja em conformidade."
                        } else {
                            "O release está travado por portões não cumpridos abaixo. Resolva todos os bloqueadores antes de gerar o pacote final de produção."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 6-Gate Preflight Checklist items
        item {
            SectionHeader(
                title = "Portões Obrigatórios de Certificação (6/6)",
                subtitle = "Conforme especificação formal replica-deploy/preflight.md"
            )
        }

        items(preflightStatus.checklist) { item ->
            PreflightGateCard(item = item)
        }

        // Bugs & Blockers Tracker section
        item {
            SectionHeader(
                title = "Rastreamento de Bugs & Bloqueadores (${bugs.size})",
                subtitle = "Bugs S1 e S2 devem estar 100% resolvidos antes do lançamento"
            )
        }

        items(bugs) { bug ->
            BugItemCard(
                bug = bug,
                onToggleResolved = { viewModel.toggleBugResolved(bug) },
                onDelete = { viewModel.deleteBug(bug) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddBugDialog) {
        AddBugDialog(
            onDismiss = { showAddBugDialog = false },
            onAdd = { title, flow, sev, notes ->
                viewModel.addBug(title, flow, sev, notes)
                showAddBugDialog = false
            }
        )
    }
}

@Composable
fun PreflightGateCard(item: PreflightItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .size(32.dp)
                    .background(
                        (if (item.passed) SuccessEmerald else DangerRose).copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.passed) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (item.passed) SuccessEmerald else DangerRose,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = (if (item.passed) SuccessEmerald else DangerRose).copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (item.passed) "OK" else "PENDENTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.passed) SuccessEmerald else DangerRose,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun BugItemCard(
    bug: BugItem,
    onToggleResolved: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = bug.isResolved,
                    onCheckedChange = { onToggleResolved() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (bug.severity) {
                                "S1" -> DangerRose
                                "S2" -> DangerRose.copy(alpha = 0.8f)
                                "S3" -> WarningAmber
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ) {
                            Text(
                                text = bug.severity,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (bug.severity == "S3") Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = bug.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (bug.isResolved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Área: ${bug.flowArea} • ${if (bug.isResolved) "Resolvido" else "Aberto"} ${if (bug.notes.isNotBlank()) "(${bug.notes})" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(onClick = onDelete) {
                Text("Excluir", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AddBugDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, flow: String, sev: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var flowArea by remember { mutableStateOf("Fluxo de Reserva") }
    var severity by remember { mutableStateOf("S2") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Defeito / Bug", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Bug") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = flowArea,
                    onValueChange = { flowArea = it },
                    label = { Text("Área do Fluxo (ex: Autenticação, Reservas)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = severity,
                    onValueChange = { severity = it },
                    label = { Text("Gravidade (S1: Bloqueador, S2: Crítico, S3, S4)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações / Passos") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), flowArea.trim(), severity.trim().uppercase(), notes.trim())
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRose)
            ) {
                Text("Adicionar Bug", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
