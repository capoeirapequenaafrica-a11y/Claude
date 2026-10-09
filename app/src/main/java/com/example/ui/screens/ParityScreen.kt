package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeatureItem
import com.example.ui.ReplicaViewModel
import com.example.ui.components.ParityDialCard
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRose
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun ParityScreen(viewModel: ReplicaViewModel) {
    val parityResult by viewModel.parityResult.collectAsState()
    val features by viewModel.features.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilterArea by remember { mutableStateOf<String?>(null) }
    var selectedFilterPriority by remember { mutableStateOf<String?>(null) }

    val areas = remember(features) { features.map { it.area }.distinct().sorted() }

    val filteredFeatures = features.filter { item ->
        (selectedFilterArea == null || item.area == selectedFilterArea) &&
        (selectedFilterPriority == null || item.priority.equals(selectedFilterPriority, ignoreCase = true))
    }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Motor de Paridade (/replica-diff)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Matriz ponderada para ${activeProject?.name ?: "Aplicativo"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_feature_btn"),
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
                    Text("Adicionar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            ParityDialCard(
                score = parityResult.featureScore,
                mustHaveDone = parityResult.mustHaveDone,
                mustHaveTotal = parityResult.mustHaveTotal,
                isShippable = parityResult.isShippable
            )
        }

        // Breakdown by area (weakest first)
        if (parityResult.byArea.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PARIDADE POR ÁREA (MAIS FRACAS PRIMEIRO)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${parityResult.byArea.size} áreas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        parityResult.byArea.forEach { areaScore ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = areaScore.area,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${areaScore.score}% (${areaScore.featuresCount} recursos)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (areaScore.score >= 80.0) SuccessEmerald else if (areaScore.score >= 50.0) WarningAmber else DangerRose
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (areaScore.score / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (areaScore.score >= 80.0) SuccessEmerald else if (areaScore.score >= 50.0) WarningAmber else DangerRose,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter chips row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedFilterArea == null && selectedFilterPriority == null) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        selectedFilterArea = null
                        selectedFilterPriority = null
                    }
                ) {
                    Text(
                        text = "Todos (${features.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedFilterArea == null && selectedFilterPriority == null) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                areas.forEach { area ->
                    val isSelected = selectedFilterArea == area
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            selectedFilterArea = if (isSelected) null else area
                        }
                    ) {
                        Text(
                            text = area,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Features list
        items(filteredFeatures, key = { it.id }) { feat ->
            FeatureRowCard(
                feature = feat,
                onStatusChange = { newStatus ->
                    viewModel.updateFeatureStatus(feat.id, newStatus)
                },
                onDelete = {
                    viewModel.deleteFeature(feat)
                }
            )
        }

        if (filteredFeatures.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum recurso encontrado neste filtro", fontWeight = FontWeight.Bold)
                        Text("Adicione linhas para calcular a pontuação de paridade", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddDialog) {
        AddFeatureDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { feat, area, prio, orig, notes ->
                viewModel.addFeature(feat, area, prio, orig, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FeatureRowCard(
    feature: FeatureItem,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feature.feature,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = feature.area,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        PriorityBadge(priority = feature.priority)

                        if (!feature.original) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SuccessEmerald.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "SUA MELHORIA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Box {
                    StatusPill(
                        status = feature.cloneStatus,
                        onClick = { showStatusMenu = true }
                    )

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Concluído (1.0 crédito)", color = SuccessEmerald) },
                            onClick = {
                                onStatusChange("yes")
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Parcial (0.5 crédito)", color = WarningAmber) },
                            onClick = {
                                onStatusChange("partial")
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("A Fazer (0.0 crédito)", color = DangerRose) },
                            onClick = {
                                onStatusChange("no")
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ignorar (Fora de Escopo)", color = MaterialTheme.colorScheme.outline) },
                            onClick = {
                                onStatusChange("skip")
                                showStatusMenu = false
                            }
                        )
                    }
                }
            }

            if (feature.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notas: ${feature.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir recurso",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddFeatureDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Boolean, String) -> Unit
) {
    var featureName by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("Fluxo Principal") }
    var priority by remember { mutableStateOf("must") }
    var original by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Recurso à Matriz", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = featureName,
                    onValueChange = { featureName = it },
                    label = { Text("Descrição da Funcionalidade") },
                    modifier = Modifier.fillMaxWidth().testTag("add_feature_input")
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Área / Tela (ex: Fluxo de Agendamento)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Prioridade / Peso:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("must", "should", "could").forEach { p ->
                        val isSel = priority == p
                        val labelPt = when(p) {
                            "must" -> "OBRIGATÓRIO (Must)"
                            "should" -> "IMPORTANTE (Should)"
                            else -> "DESEJÁVEL (Could)"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyanPrimary else Color.Transparent),
                            modifier = Modifier.clickable { priority = p }
                        ) {
                            Text(
                                text = p.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Existia no app original?", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = original, onCheckedChange = { original = it })
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas técnicas (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (featureName.isNotBlank()) {
                        onAdd(featureName, area, priority, original, notes)
                    }
                },
                enabled = featureName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Adicionar Recurso", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
