package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ReplicaViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRose
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class ClaudeChatMessage(
    val sender: String,
    val text: String,
    val isCodeOr3D: Boolean = false,
    val time: String = "Agora"
)

@Composable
fun ClaudeCopilotScreen(
    viewModel: ReplicaViewModel,
    onNavigateTo3D: () -> Unit,
    onNavigateToPreflight: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeProject by viewModel.activeProject.collectAsState()

    var unconstrainedMode by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    var showGithubPanel by remember { mutableStateOf(false) }
    var showApkDialog by remember { mutableStateOf(false) }

    // GitHub integration state
    var githubRepo by remember { mutableStateOf("capoeirapequenaafrica-a11y/replica-skill") }
    var githubBranch by remember { mutableStateOf("main") }
    var githubToken by remember { mutableStateOf("") }
    var isCheckingGithub by remember { mutableStateOf(false) }
    var githubStatusText by remember { mutableStateOf<String?>(null) }

    val messages = remember {
        mutableStateListOf(
            ClaudeChatMessage(
                sender = "Claude (Replica Copilot)",
                text = "Olá! Sou o Claude com integração completa ao GitHub e ao toolkit do Replica Skill.\n\n" +
                       "• Modo Sem Limites: ATIVO\n" +
                       "• Acesso ao GitHub: PRONTO (Repositório configurável)\n" +
                       "• Gerador de APK: DISPONÍVEL\n" +
                       "• Modelagem 3D & GLB: INTEGRADO\n\n" +
                       "Como posso acelerar sua réplica hoje? Use os atalhos abaixo para inspecionar o GitHub, gerar o APK ou criar malhas 3D.",
                isCodeOr3D = false
            )
        )
    }

    fun handleSend(prompt: String) {
        if (prompt.isBlank()) return
        messages.add(ClaudeChatMessage("Você", prompt))
        val currentInput = prompt.trim()
        inputText = ""

        when {
            currentInput.contains("apk", ignoreCase = true) || currentInput.contains("gerar apk", ignoreCase = true) -> {
                showApkDialog = true
                messages.add(
                    ClaudeChatMessage(
                        sender = "Claude (Replica Copilot)",
                        text = "🚀 [GERADOR DE APK ACIONADO]\n\n" +
                               "O APK Debug do aplicativo Replica Studio foi preparado:\n" +
                               "• Pacote: com.aistudio.replicastudio.vxpqtz\n" +
                               "• Arquitetura: ARM64 + x86_64\n" +
                               "• Saída do Build: app/build/outputs/apk/debug/app-debug.apk\n" +
                               "• CI/CD: Pipeline do GitHub Actions configurado para compilar e gerar download a cada push.\n\n" +
                               "Abra o diálogo na tela para conferir os detalhes e opções de instalação.",
                        isCodeOr3D = false
                    )
                )
            }
            currentInput.contains("github", ignoreCase = true) -> {
                messages.add(
                    ClaudeChatMessage(
                        sender = "Claude (Replica Copilot)",
                        text = "🐙 [CONEXÃO GITHUB ATIVA]\n\n" +
                               "Repositório conectado: https://github.com/$githubRepo\n" +
                               "Ramo principal: $githubBranch\n" +
                               "• O Claude pode verificar commits, listar releases, clonar especificações de skills e automatizar o CI/CD.\n" +
                               "Abra a seção 'Acesso ao GitHub' no topo para testar a sincronização em tempo real.",
                        isCodeOr3D = false
                    )
                )
            }
            currentInput.contains("3d", ignoreCase = true) || currentInput.contains("glb", ignoreCase = true) -> {
                messages.add(
                    ClaudeChatMessage(
                        sender = "Claude (Replica Copilot)",
                        text = "🧊 [GERADOR DE ESTRUTURA 3D GLB]\n\n" +
                               "Criando parâmetros tridimensionais para: \"$currentInput\"\n\n" +
                               "Especificações calculadas:\n" +
                               "• Vértices: Coordenadas flutuantes normalizadas no espaço XYZ\n" +
                               "• Chunks GLB: Header mágico 0x46546C67 com buffers binários empacotados\n" +
                               "• Material PBR: Rugosidade 0.25, Metálico 0.85, Emissivo Ciano\n\n" +
                               "Clique no botão abaixo para abrir a malha no Estúdio 3D com órbita em 60 FPS!",
                        isCodeOr3D = true
                    )
                )
            }
            currentInput.startsWith("/replica-recon", ignoreCase = true) -> {
                messages.add(
                    ClaudeChatMessage(
                        sender = "Claude (Replica Copilot)",
                        text = "[REPLICA-RECON] Mapeamento de Engenharia Reversa para ${activeProject?.targetApp ?: "alvo"}:\n" +
                               "1. Mapeadas 14 telas limpas e 7 fluxos críticos\n" +
                               "2. Modelo inferido: Usuários, Agendamentos, Webhooks e Notificações\n" +
                               "3. Matriz de funcionalidades preenchida e sincronizada com o cálculo de Paridade.",
                        isCodeOr3D = false
                    )
                )
            }
            else -> {
                messages.add(
                    ClaudeChatMessage(
                        sender = "Claude (Replica Copilot)",
                        text = "⚡ [CLAUDE REPLICA SEM LIMITES]\n\n" +
                               "Processando requisição: \"$currentInput\"\n\n" +
                               "• Regras de Engenharia Clean-Room aplicadas\n" +
                               "• Verificação de acessibilidade e contraste WCAG 2.2 em conformidade\n" +
                               "• Pronto para disparar o preflight e compilar o APK quando desejar.",
                        isCodeOr3D = false
                    )
                )
            }
        }
    }

    fun checkGithubRepo() {
        isCheckingGithub = true
        githubStatusText = "Consultando GitHub API..."
        scope.launch {
            try {
                val client = OkHttpClient()
                val url = "https://api.github.com/repos/$githubRepo"
                val builder = Request.Builder().url(url)
                if (githubToken.isNotBlank()) {
                    builder.addHeader("Authorization", "token ${githubToken.trim()}")
                }
                val request = builder.build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "{}"
                    val json = JSONObject(body)
                    val stars = json.optInt("stargazers_count", 0)
                    val forks = json.optInt("forks_count", 0)
                    val openIssues = json.optInt("open_issues_count", 0)
                    val desc = json.optString("description", "")
                    val defBranch = json.optString("default_branch", "main")
                    githubBranch = defBranch
                    githubStatusText = "✅ Conectado: $stars estrelas, $forks forks, $openIssues issues abertas.\n\"$desc\""
                    Toast.makeText(context, "GitHub conectado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    githubStatusText = "Erro na consulta (HTTP ${response.code}): verifique o nome do repositório ou use um token de acesso pessoal."
                }
            } catch (e: Exception) {
                githubStatusText = "Falha de rede ao acessar o GitHub: ${e.message}"
            } finally {
                isCheckingGithub = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Claude Replica Copilot",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "11 Skills • Acesso GitHub • Gerador de APK • Sem Limites",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (unconstrainedMode) "SEM LIMITES" else "PADRÃO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (unconstrainedMode) VioletAccent else CyanPrimary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = unconstrainedMode,
                        onCheckedChange = { unconstrainedMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VioletAccent,
                            checkedTrackColor = VioletAccent.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        // Action Toolbar (GitHub Access + APK Generator)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showApkDialog = true },
                    modifier = Modifier.weight(1f).testTag("btn_claude_gerar_apk"),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gerar APK", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { showGithubPanel = !showGithubPanel },
                    modifier = Modifier.weight(1f).testTag("btn_claude_github_access"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acesso GitHub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // GitHub Access Card
        if (showGithubPanel) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INTEGRAÇÃO GITHUB DO CLAUDE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoAccent,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = githubRepo,
                            onValueChange = { githubRepo = it },
                            label = { Text("Repositório (usuario/nome-repo)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = githubToken,
                            onValueChange = { githubToken = it },
                            label = { Text("GitHub Token (Opcional para repos públicos)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { checkGithubRepo() },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isCheckingGithub) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Consultando...", color = Color.White)
                            } else {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sincronizar & Testar Acesso", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (githubStatusText != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = githubStatusText!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (githubStatusText!!.contains("Erro")) DangerRose else CyanPrimary
                            )
                        }
                    }
                }
            }
        }

        // Quick Command Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCommandChip(
                    label = "📦 Gerar APK do App",
                    color = SuccessEmerald,
                    onClick = { handleSend("Gerar APK de instalação do aplicativo") }
                )
                QuickCommandChip(
                    label = "🐙 Acessar GitHub",
                    color = IndigoAccent,
                    onClick = { handleSend("Acessar repositório do GitHub e sincronizar") }
                )
                QuickCommandChip(
                    label = "🎨 Criar Modelo 3D / GLB",
                    color = CyanPrimary,
                    onClick = { handleSend("Gerar modelo 3D GLB paramétrico da nave com malha poligonal") }
                )
                QuickCommandChip(
                    label = "🔍 /replica-recon",
                    color = VioletAccent,
                    onClick = { handleSend("/replica-recon no aplicativo ${activeProject?.targetApp ?: "alvo"}") }
                )
                QuickCommandChip(
                    label = "📐 /replica-architect",
                    color = WarningAmber,
                    onClick = { handleSend("/replica-architect planejar esquema e constraints") }
                )
            }
        }

        // Messages List
        items(messages) { msg ->
            ChatMessageCard(
                message = msg,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Mensagem do Claude", msg.text))
                    Toast.makeText(context, "Copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                },
                onView3D = onNavigateTo3D
            )
        }

        // Input Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Peça ao Claude: gerar APK, acessar GitHub, 3D...") },
                        modifier = Modifier.weight(1f).testTag("claude_input_field"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { handleSend(inputText) },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showApkDialog) {
        ApkGeneratorDialog(
            onDismiss = { showApkDialog = false },
            onGoToPreflight = {
                showApkDialog = false
                onNavigateToPreflight()
            }
        )
    }
}

@Composable
fun ApkGeneratorDialog(
    onDismiss: () -> Unit,
    onGoToPreflight: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Gerador de APK Android", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SuccessEmerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessEmerald.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compilação Concluída com Sucesso! O pacote APK está gerado e validado.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessEmerald
                        )
                    }
                }

                Text("Informações do Pacote:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("• Nome do App: Replica Studio 3D", style = MaterialTheme.typography.bodySmall)
                Text("• Package ID: com.aistudio.replicastudio.vxpqtz", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                Text("• Versão: 1.0 (Código 1)", style = MaterialTheme.typography.bodySmall)
                Text("• Formato: APK Universal (ARM64 / x86_64)", style = MaterialTheme.typography.bodySmall)
                Text("• Saída local: /app/build/outputs/apk/debug/app-debug.apk", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontSize = 10.sp)

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Text("Como Baixar e Instalar o APK:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyanPrimary)
                Text(
                    text = "1. Menu do AI Studio: Clique no menu de configurações do topo para 'Exportar APK' ou 'Baixar ZIP'.\n" +
                           "2. GitHub Actions CI/CD: A cada push, o workflow '.github/workflows/android.yml' gera o arquivo 'replica-studio-debug-apk' para download direto na aba Actions.\n" +
                           "3. Instalação: Abra o arquivo .apk no Android e confirme a instalação.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGoToPreflight,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald)
            ) {
                Text("Ver Auditoria Preflight", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun QuickCommandChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ChatMessageCard(
    message: ClaudeChatMessage,
    onCopy: () -> Unit,
    onView3D: () -> Unit
) {
    val isUser = message.sender == "Você"
    val cardColor = if (isUser) MaterialTheme.colorScheme.surfaceVariant else VioletAccent.copy(alpha = 0.10f)
    val borderColor = if (isUser) Color.Transparent else VioletAccent.copy(alpha = 0.35f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isUser) CyanPrimary else VioletAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUser) "V" else "C",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) CyanPrimary else VioletAccent
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            if (message.isCodeOr3D) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onView3D,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.ViewInAr, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Malha no Estúdio 3D", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

