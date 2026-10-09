package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContrastCheckResult
import com.example.data.model.DesignToken
import com.example.engine.ContrastEngine
import com.example.ui.ReplicaViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRose
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.WarningAmber

@Composable
fun ContrastScreen(viewModel: ReplicaViewModel) {
    val tokens by viewModel.tokens.collectAsState()
    val contrastResults by viewModel.contrastResults.collectAsState()

    var customFgHex by remember { mutableStateOf("#0F172A") }
    var customBgHex by remember { mutableStateOf("#FFFFFF") }
    var showAddTokenDialog by remember { mutableStateOf(false) }

    val liveRatio = remember(customFgHex, customBgHex) {
        ContrastEngine.ratio(customFgHex, customBgHex)
    }

    val failCount = contrastResults.count { !it.passAa }

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
                        text = "Contraste WCAG 2.2",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "/replica-design • Auditoria de Acessibilidade (A11Y)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddTokenDialog = true },
                    modifier = Modifier.testTag("add_token_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Token", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Contrast Checker Workbench
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, VioletAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TESTADOR DE CONTRASTE EM TEMPO REAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VioletAccent,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customFgHex,
                            onValueChange = { customFgHex = it },
                            label = { Text("Texto / Frente (HEX)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = customBgHex,
                            onValueChange = { customBgHex = it },
                            label = { Text("Fundo (HEX)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live preview box
                    val parsedFg = try {
                        Color(android.graphics.Color.parseColor(customFgHex))
                    } catch (e: Exception) {
                        Color.White
                    }

                    val parsedBg = try {
                        Color(android.graphics.Color.parseColor(customBgHex))
                    } catch (e: Exception) {
                        Color.Black
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(parsedBg)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Texto de Demonstração em Tempo Real",
                                color = parsedFg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Proporção de Contraste Calculada: ${liveRatio}:1",
                                color = parsedFg.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pass / Fail Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ComplianceBadge(label = "Texto Normal AA", required = ">= 4.5:1", passed = liveRatio >= 4.5)
                        ComplianceBadge(label = "Texto Grande AA", required = ">= 3.0:1", passed = liveRatio >= 3.0)
                        ComplianceBadge(label = "Componentes UI", required = ">= 3.0:1", passed = liveRatio >= 3.0)
                        ComplianceBadge(label = "Texto Normal AAA", required = ">= 7.0:1", passed = liveRatio >= 7.0)
                    }
                }
            }
        }

        // Status overview of project tokens
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
                            text = "STATUS DA AUDITORIA WCAG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (failCount == 0) "100% Aprovado (AA)" else "$failCount Reprovado(s)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (failCount == 0) SuccessEmerald else DangerRose
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = (if (failCount == 0) SuccessEmerald else DangerRose).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (failCount == 0) "PRONTO PARA PUBLICAR" else "CORREÇÃO OBRIGATÓRIA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (failCount == 0) SuccessEmerald else DangerRose,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Audit Pairs List
        item {
            SectionHeader(
                title = "Pares de Cores & Auditoria WCAG (${contrastResults.size})",
                subtitle = "Pares verificados automaticamente a partir dos tokens de design cadastrados"
            )
        }

        items(contrastResults) { res ->
            ContrastResultCard(result = res)
        }

        // Design Tokens List
        item {
            SectionHeader(
                title = "Tokens de Cor do Projeto (${tokens.size})",
                subtitle = "Valores de cores mapeados no sistema de design da réplica"
            )
        }

        items(tokens) { token ->
            TokenItemCard(
                token = token,
                onDelete = { viewModel.deleteToken(token) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddTokenDialog) {
        AddTokenDialog(
            onDismiss = { showAddTokenDialog = false },
            onAdd = { name, hex, role ->
                viewModel.addToken(name, hex, role)
                showAddTokenDialog = false
            }
        )
    }
}

@Composable
fun ComplianceBadge(label: String, required: String, passed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (passed) SuccessEmerald.copy(alpha = 0.15f) else DangerRose.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (passed) "APROVADO" else "FALHOU",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (passed) SuccessEmerald else DangerRose,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 9.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(text = required, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ContrastResultCard(result: ContrastCheckResult) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color swatches preview
                val fgColor = try { Color(android.graphics.Color.parseColor(result.fgHex)) } catch (e: Exception) { Color.Black }
                val bgColor = try { Color(android.graphics.Color.parseColor(result.bgHex)) } catch (e: Exception) { Color.White }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Aa", color = fgColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "${result.fgName} sobre ${result.bgName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${result.fgHex} / ${result.bgHex}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${result.ratio}:1",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = if (result.passAa) SuccessEmerald else DangerRose
                )
                Text(
                    text = if (result.passAaa) "WCAG AAA" else if (result.passAa) "WCAG AA" else "FALHOU",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (result.passAa) SuccessEmerald else DangerRose,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun TokenItemCard(token: DesignToken, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val swatchColor = try { Color(android.graphics.Color.parseColor(token.hex)) } catch (e: Exception) { Color.Gray }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = token.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(text = "${token.hex} • papel: ${token.role}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir Token",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddTokenDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, hex: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("color-brand-primary") }
    var hex by remember { mutableStateOf("#4F46E5") }
    var role by remember { mutableStateOf("accent") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Token de Cor", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Token (ex: color-brand-primary)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = hex,
                    onValueChange = { hex = it },
                    label = { Text("Código HEX (#RRGGBB)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Papel (text, bg, surface, accent)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && hex.isNotBlank()) {
                        onAdd(name.trim(), hex.trim(), role.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VioletAccent)
            ) {
                Text("Adicionar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
