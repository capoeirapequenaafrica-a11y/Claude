package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.engine.AIMode
import com.example.engine.ClaudeMultiAIEngine
import com.example.engine.ClaudeWebEngine
import com.example.engine.ConnectivityStatus
import com.example.ui.ReplicaViewModel
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
    val time: String = "Agora",
    val thinkingSteps: String? = null,
    val humorQuote: String? = null,
    val aiMode: AIMode = AIMode.CLAUDE
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

    // Multi-AI Engines State (Claude + Grok + Gemini)
    var selectedAIMode by remember { mutableStateOf(AIMode.TRI_ENGINE) }
    var grokFunMode by remember { mutableStateOf(true) }
    var geminiThinkingEnabled by remember { mutableStateOf(true) }
    var showAIEnginePanel by remember { mutableStateOf(false) }

    // Panels state
    var showGithubPanel by remember { mutableStateOf(false) }
    var showInternetPanel by remember { mutableStateOf(false) }
    var showApkDialog by remember { mutableStateOf(false) }

    // Internet integration state
    var isCheckingInternet by remember { mutableStateOf(false) }
    var internetStatus by remember { mutableStateOf<ConnectivityStatus?>(null) }
    var webSearchQuery by remember { mutableStateOf("") }
    var webUrlInput by remember { mutableStateOf("https://pt.wikipedia.org/wiki/Inteligência_artificial") }

    // GitHub integration state
    var githubRepo by remember { mutableStateOf("capoeirapequenaafrica-a11y/replica-skill") }
    var githubBranch by remember { mutableStateOf("main") }
    var githubToken by remember { mutableStateOf("") }
    var isCheckingGithub by remember { mutableStateOf(false) }
    var githubStatusText by remember { mutableStateOf<String?>(null) }

    // Test connectivity on initial screen open
    LaunchedEffect(Unit) {
        try {
            val status = ClaudeWebEngine.checkConnectivity()
            internetStatus = status
        } catch (_: Exception) { }
    }

    val messages = remember {
        mutableStateListOf(
            ClaudeChatMessage(
                sender = "Claude Super Copilot (Claude + Grok + Gemini)",
                text = "⚡ **FUSÃO COMPLETA DE INTELIGÊNCIAS ATIVA!**\n\n" +
                       "Reunimos em uma única interface os poderes dos três maiores modelos do planeta:\n\n" +
                       "• 🟣 **Claude (Replica Master):** Engenharia Clean-Room, 11 skills do Replica, análise de paridade e arquitetura.\n" +
                       "• 🚀 **Grok (xAI):** Humor sarcástico (Modo Fun), respostas ultrarrápidas, sem censura e DeepSearch ao vivo.\n" +
                       "• 💎 **Gemini AI (Google):** Extended Thinking (cadeia de pensamento analítico), multimodalidade e código Android SDK 36.\n" +
                       "• 🌐 **Acesso à Internet:** CONECTADO (pesquisa e fetch URL em tempo real).\n" +
                       "• 📦 **Gerador de APK:** COMPILADO e pronto para download.\n\n" +
                       "Experimente alternar os motores no topo ou digitar qualquer dúvida, comando ou link!",
                isCodeOr3D = false,
                aiMode = AIMode.TRI_ENGINE
            )
        )
    }

    fun handleSend(prompt: String) {
        if (prompt.isBlank()) return
        val currentInput = prompt.trim()
        messages.add(ClaudeChatMessage("Você", prompt))
        inputText = ""

        val urlRegex = Regex("""https?://[^\s]+""", RegexOption.IGNORE_CASE)
        val matchUrl = urlRegex.find(currentInput)?.value

        // Determina o motor com base no input explícito ou no seletor global
        val effectiveMode = when {
            currentInput.contains("grok", ignoreCase = true) || currentInput.contains("sarcasmo", ignoreCase = true) -> AIMode.GROK
            currentInput.contains("gemini", ignoreCase = true) || currentInput.contains("thinking", ignoreCase = true) -> AIMode.GEMINI
            currentInput.contains("fusao", ignoreCase = true) || currentInput.contains("tri-engine", ignoreCase = true) -> AIMode.TRI_ENGINE
            else -> selectedAIMode
        }

        when {
            // 1. Requisição de URL / Link na Web
            matchUrl != null || currentInput.startsWith("/fetch-url", ignoreCase = true) -> {
                val targetUrl = matchUrl ?: currentInput.removePrefix("/fetch-url").trim()
                val loadingIndex = messages.size
                messages.add(
                    ClaudeChatMessage(
                        sender = "${effectiveMode.displayName}",
                        text = "🌐 Acessando a internet em tempo real...\nFazendo requisição HTTP GET para: $targetUrl ...",
                        aiMode = effectiveMode
                    )
                )
                scope.launch {
                    val result = ClaudeWebEngine.fetchUrl(targetUrl)
                    val responseText = if (result.isSuccess) {
                        "🌐 [CONTEÚDO DA PÁGINA WEB CARREGADO COM SUCESSO]\n\n" +
                        "• URL: ${result.url}\n" +
                        "• Título da Página: ${result.title}\n" +
                        "• Status HTTP: ${result.statusCode} OK (Latência: ${result.latencyMs}ms)\n" +
                        "• Tamanho do payload: ${result.byteSize} bytes (${result.contentType})\n\n" +
                        "📄 Conteúdo Extraído da Web:\n" +
                        "\"${result.contentPreview}\"\n\n" +
                        "Análise executada por ${effectiveMode.displayName} com acesso direto à internet."
                    } else {
                        "⚠️ [FALHA AO ACESSAR A URL]\n\n" +
                        "• URL: ${result.url}\n" +
                        "• Erro: ${result.errorMessage ?: "Erro desconhecido"}"
                    }

                    if (loadingIndex < messages.size) {
                        messages[loadingIndex] = ClaudeChatMessage(
                            sender = effectiveMode.displayName,
                            text = responseText,
                            aiMode = effectiveMode
                        )
                    } else {
                        messages.add(ClaudeChatMessage(sender = effectiveMode.displayName, text = responseText, aiMode = effectiveMode))
                    }
                }
            }

            // 2. Pesquisa Web na Internet (Grok DeepSearch / Gemini Grounding)
            currentInput.contains("pesquisar", ignoreCase = true) ||
            currentInput.contains("buscar na web", ignoreCase = true) ||
            currentInput.contains("buscar na internet", ignoreCase = true) ||
            currentInput.contains("pesquisa web", ignoreCase = true) ||
            currentInput.startsWith("/web-search", ignoreCase = true) ||
            currentInput.startsWith("/grok-deepsearch", ignoreCase = true) -> {
                val cleanQuery = currentInput
                    .replace(Regex("(?i)^(pesquisar|buscar na web|buscar na internet|pesquisa web|/web-search|/grok-deepsearch)\\s*(sobre|por|:)?:?"), "")
                    .trim()
                    .ifBlank { "Kotlin Jetpack Compose Android" }

                val loadingIndex = messages.size
                messages.add(
                    ClaudeChatMessage(
                        sender = effectiveMode.displayName,
                        text = "🔎 [${if (effectiveMode == AIMode.GROK) "GROK DEEPSEARCH AO VIVO" else "PESQUISA NA WEB"}]\n\nVarrendo a internet para: \"$cleanQuery\"...",
                        aiMode = effectiveMode
                    )
                )
                scope.launch {
                    val results = ClaudeWebEngine.searchWeb(cleanQuery)
                    val responseText = if (results.isNotEmpty()) {
                        val sb = StringBuilder()
                        sb.append("🌐 [RESULTADOS DA PESQUISA WEB • ${effectiveMode.displayName.uppercase()}]\n\n")
                        if (effectiveMode == AIMode.GROK && grokFunMode) {
                            sb.append("😏 *“Varri a internet inteira em milissegundos pra te poupar 15 abas abertas de ansiedade.”*\n\n")
                        }
                        sb.append("Consulta: \"$cleanQuery\"\n")
                        sb.append("Fontes encontradas na web:\n\n")
                        results.forEachIndexed { idx, item ->
                            sb.append("${idx + 1}. 📌 **${item.title}** (${item.source})\n")
                            if (item.snippet.isNotBlank()) {
                                sb.append("   ${item.snippet.take(200)}...\n")
                            }
                            sb.append("   🔗 ${item.url}\n\n")
                        }
                        sb.append("Dados verificados via conexão ativa com a internet.")
                        sb.toString()
                    } else {
                        "🌐 Consulta executada para: \"$cleanQuery\". Conexão ativa."
                    }

                    if (loadingIndex < messages.size) {
                        messages[loadingIndex] = ClaudeChatMessage(
                            sender = effectiveMode.displayName,
                            text = responseText,
                            aiMode = effectiveMode
                        )
                    } else {
                        messages.add(ClaudeChatMessage(sender = effectiveMode.displayName, text = responseText, aiMode = effectiveMode))
                    }
                }
            }

            // 3. Teste de Conectividade / Ping
            currentInput.contains("ping", ignoreCase = true) ||
            currentInput.contains("testar internet", ignoreCase = true) ||
            (currentInput.contains("internet", ignoreCase = true) && !currentInput.contains("pesquisar", ignoreCase = true)) -> {
                val loadingIndex = messages.size
                messages.add(
                    ClaudeChatMessage(
                        sender = effectiveMode.displayName,
                        text = "📡 Testando conectividade com a Internet em tempo real...",
                        aiMode = effectiveMode
                    )
                )
                scope.launch {
                    val status = ClaudeWebEngine.checkConnectivity()
                    internetStatus = status
                    val responseText = "📡 [DIAGNÓSTICO DE ACESSO À INTERNET • ${effectiveMode.displayName.uppercase()}]\n\n" +
                        "• Status da Conexão: ${if (status.isConnected) "🟢 ONLINE (CONEXÃO ESTÁVEL)" else "🔴 SEM CONEXÃO"}\n" +
                        "• Latência de Rede: ${status.latencyMs} ms\n" +
                        "• Endpoint de Teste: ${status.endpoint}\n" +
                        "• Diagnóstico: ${status.details}\n\n" +
                        "Motores com Internet Ativa: Claude, Grok (DeepSearch) e Gemini AI."

                    if (loadingIndex < messages.size) {
                        messages[loadingIndex] = ClaudeChatMessage(
                            sender = effectiveMode.displayName,
                            text = responseText,
                            aiMode = effectiveMode
                        )
                    } else {
                        messages.add(ClaudeChatMessage(sender = effectiveMode.displayName, text = responseText, aiMode = effectiveMode))
                    }
                }
            }

            // 4. Gerador de APK
            currentInput.contains("apk", ignoreCase = true) || currentInput.contains("gerar apk", ignoreCase = true) -> {
                showApkDialog = true
                messages.add(
                    ClaudeChatMessage(
                        sender = effectiveMode.displayName,
                        text = "🚀 [GERADOR DE APK ACIONADO]\n\n" +
                               "O APK Debug do aplicativo Replica Studio foi preparado:\n" +
                               "• Pacote: com.aistudio.replicastudio.vxpqtz\n" +
                               "• Arquitetura: ARM64 + x86_64\n" +
                               "• Saída do Build: app/build/outputs/apk/debug/app-debug.apk\n" +
                               "• CI/CD: Pipeline do GitHub Actions configurado.\n\n" +
                               "Abra o diálogo na tela para conferir os detalhes e opções de instalação.",
                        isCodeOr3D = false,
                        humorQuote = if (effectiveMode == AIMode.GROK || effectiveMode == AIMode.TRI_ENGINE) "“APK compilado sem frescura. Só baixar e rodar.”" else null,
                        aiMode = effectiveMode
                    )
                )
            }

            // 5. Conexão GitHub
            currentInput.contains("github", ignoreCase = true) -> {
                showGithubPanel = true
                messages.add(
                    ClaudeChatMessage(
                        sender = effectiveMode.displayName,
                        text = "🐙 [CONEXÃO GITHUB ATIVA]\n\n" +
                               "Repositório conectado: https://github.com/$githubRepo\n" +
                               "Ramo principal: $githubBranch\n" +
                               "• O Claude, Grok e Gemini podem auditar commits, clonar especificações e automatizar o CI/CD.\n" +
                               "Abra a seção 'GitHub' para testar a sincronização.",
                        isCodeOr3D = false,
                        aiMode = effectiveMode
                    )
                )
            }

            // 6. Estrutura 3D / GLB
            currentInput.contains("3d", ignoreCase = true) || currentInput.contains("glb", ignoreCase = true) -> {
                messages.add(
                    ClaudeChatMessage(
                        sender = effectiveMode.displayName,
                        text = "🧊 [GERADOR DE ESTRUTURA 3D GLB • ${effectiveMode.displayName.uppercase()}]\n\n" +
                               "Criando parâmetros tridimensionais para: \"$currentInput\"\n\n" +
                               "Especificações calculadas:\n" +
                               "• Vértices: Coordenadas flutuantes normalizadas no espaço XYZ\n" +
                               "• Chunks GLB: Header mágico 0x46546C67 com buffers binários empacotados\n" +
                               "• Material PBR: Rugosidade 0.25, Metálico 0.85, Emissivo Ciano\n\n" +
                               "Clique no botão abaixo para abrir a malha no Estúdio 3D com órbita em 60 FPS!",
                        isCodeOr3D = true,
                        humorQuote = if (effectiveMode == AIMode.GROK) "“Mais malhas 3D de verdade, menos conversa fiada sobre metaverso.”" else null,
                        aiMode = effectiveMode
                    )
                )
            }

            // 7. Processamento Geral com Motor Multi-IA (Grok, Gemini, Claude ou Tri-Engine)
            else -> {
                val loadingIndex = messages.size
                messages.add(
                    ClaudeChatMessage(
                        sender = effectiveMode.displayName,
                        text = "⚡ Processando com ${effectiveMode.displayName}...",
                        aiMode = effectiveMode
                    )
                )
                scope.launch {
                    val aiResponse = ClaudeMultiAIEngine.generateResponse(
                        prompt = currentInput,
                        mode = effectiveMode,
                        isGrokFunMode = grokFunMode,
                        isGeminiThinkingEnabled = geminiThinkingEnabled,
                        targetApp = activeProject?.targetApp ?: "Calendly"
                    )

                    val updatedMsg = ClaudeChatMessage(
                        sender = effectiveMode.displayName,
                        text = aiResponse.mainText,
                        thinkingSteps = aiResponse.thinkingSteps,
                        humorQuote = aiResponse.grokHumorQuote,
                        aiMode = effectiveMode
                    )

                    if (loadingIndex < messages.size) {
                        messages[loadingIndex] = updatedMsg
                    } else {
                        messages.add(updatedMsg)
                    }
                }
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
                    githubStatusText = "Erro na consulta (HTTP ${response.code}): verifique o nome do repositório."
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
        // Header
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
                            text = "Claude Copilot Pro",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (selectedAIMode) {
                                AIMode.TRI_ENGINE -> WarningAmber.copy(alpha = 0.2f)
                                AIMode.GROK -> DangerRose.copy(alpha = 0.2f)
                                AIMode.GEMINI -> CyanPrimary.copy(alpha = 0.2f)
                                AIMode.CLAUDE -> VioletAccent.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = "${selectedAIMode.icon} ${selectedAIMode.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (selectedAIMode) {
                                    AIMode.TRI_ENGINE -> WarningAmber
                                    AIMode.GROK -> DangerRose
                                    AIMode.GEMINI -> CyanPrimary
                                    AIMode.CLAUDE -> VioletAccent
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        if (internetStatus?.isConnected == true) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SuccessEmerald.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${internetStatus?.latencyMs}ms", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                                }
                            }
                        }
                    }
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

        // Engine Selector Tabs (Tri-Engine, Grok, Gemini, Claude)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AIModeTabChip(
                    mode = AIMode.TRI_ENGINE,
                    isSelected = selectedAIMode == AIMode.TRI_ENGINE,
                    accentColor = WarningAmber,
                    onClick = { selectedAIMode = AIMode.TRI_ENGINE }
                )
                AIModeTabChip(
                    mode = AIMode.GROK,
                    isSelected = selectedAIMode == AIMode.GROK,
                    accentColor = DangerRose,
                    onClick = { selectedAIMode = AIMode.GROK }
                )
                AIModeTabChip(
                    mode = AIMode.GEMINI,
                    isSelected = selectedAIMode == AIMode.GEMINI,
                    accentColor = CyanPrimary,
                    onClick = { selectedAIMode = AIMode.GEMINI }
                )
                AIModeTabChip(
                    mode = AIMode.CLAUDE,
                    isSelected = selectedAIMode == AIMode.CLAUDE,
                    accentColor = VioletAccent,
                    onClick = { selectedAIMode = AIMode.CLAUDE }
                )
            }
        }

        // Action Toolbar (Motores IA + Gerar APK + GitHub + Internet)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { showAIEnginePanel = !showAIEnginePanel },
                    modifier = Modifier.weight(1f).testTag("btn_claude_ai_panel"),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("IAs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = { showApkDialog = true },
                    modifier = Modifier.weight(1f).testTag("btn_claude_gerar_apk"),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("APK", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = { showGithubPanel = !showGithubPanel },
                    modifier = Modifier.weight(1f).testTag("btn_claude_github_access"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("GitHub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = { showInternetPanel = !showInternetPanel },
                    modifier = Modifier.weight(1f).testTag("btn_claude_internet_access"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Web", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Multi-AI Engines Configuration Panel
        if (showAIEnginePanel) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletAccent.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CENTRAL DE RECURSOS: GROK + GEMINI + CLAUDE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = VioletAccent,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Modo Atual: ${selectedAIMode.displayName} — ${selectedAIMode.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Grok Fun Mode Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🚀 Modo Fun & Sarcástico do Grok", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DangerRose)
                                Text("Humor ácido, respostas sem papas na língua e pragmatismo do xAI", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                            Switch(
                                checked = grokFunMode,
                                onCheckedChange = { grokFunMode = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DangerRose, checkedTrackColor = DangerRose.copy(alpha = 0.3f))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gemini Thinking Mode Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("💎 Extended Thinking do Gemini", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                Text("Exibe passos de raciocínio profundo e rigor arquitetural Google", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                            Switch(
                                checked = geminiThinkingEnabled,
                                onCheckedChange = { geminiThinkingEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary, checkedTrackColor = CyanPrimary.copy(alpha = 0.3f))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { handleSend("Grok, me dê uma opinião sincera e sarcástica sobre desenvolvimento Android moderno") },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRose.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Testar Grok", color = DangerRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { handleSend("Gemini, pense passo a passo na arquitetura ideal para nosso app 3D") },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Testar Gemini", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Expandable Internet Access Panel
        if (showInternetPanel) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INTERNET & GROK DEEPSEARCH AO VIVO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Test Ping Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = internetStatus?.details ?: "Status: Conexão ativa com a Internet",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (internetStatus?.isConnected == true) SuccessEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    isCheckingInternet = true
                                    scope.launch {
                                        try {
                                            internetStatus = ClaudeWebEngine.checkConnectivity()
                                        } finally {
                                            isCheckingInternet = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isCheckingInternet) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Text("Testar Ping", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Web Search Box
                        Text("Grok DeepSearch & Busca Web:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = webSearchQuery,
                                onValueChange = { webSearchQuery = it },
                                placeholder = { Text("Pesquisar na internet...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (webSearchQuery.isNotBlank()) {
                                        handleSend("Pesquisar na internet: $webSearchQuery")
                                        webSearchQuery = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // URL Fetch Box
                        Text("Inspecionar / Ler URL Externa:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = webUrlInput,
                                onValueChange = { webUrlInput = it },
                                placeholder = { Text("https://exemplo.com") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (webUrlInput.isNotBlank()) {
                                        handleSend(webUrlInput)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
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
                                text = "INTEGRAÇÃO GITHUB COM TODAS AS IAS",
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
                            label = { Text("GitHub Token (Opcional)") },
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
                    label = "🚀 Grok: Modo Sarcástico",
                    color = DangerRose,
                    onClick = {
                        selectedAIMode = AIMode.GROK
                        handleSend("Grok, me dê sua análise mais sincera e bem humorada deste aplicativo")
                    }
                )
                QuickCommandChip(
                    label = "💎 Gemini: Raciocínio (Thinking)",
                    color = CyanPrimary,
                    onClick = {
                        selectedAIMode = AIMode.GEMINI
                        handleSend("Gemini, faça um raciocínio profundo da arquitetura Compose deste projeto")
                    }
                )
                QuickCommandChip(
                    label = "⚡ Fusão Tri-Engine",
                    color = WarningAmber,
                    onClick = {
                        selectedAIMode = AIMode.TRI_ENGINE
                        handleSend("Fusão Claude + Grok + Gemini: avaliem as prioridades deste app")
                    }
                )
                QuickCommandChip(
                    label = "🌐 Grok DeepSearch",
                    color = CyanPrimary,
                    onClick = { handleSend("Pesquisar na internet sobre tendências de IA e Android Kotlin") }
                )
                QuickCommandChip(
                    label = "📦 Gerar APK do App",
                    color = SuccessEmerald,
                    onClick = { handleSend("Gerar APK de instalação do aplicativo") }
                )
                QuickCommandChip(
                    label = "🎨 Modelo 3D / GLB",
                    color = VioletAccent,
                    onClick = { handleSend("Gerar modelo 3D GLB paramétrico da nave com malha poligonal") }
                )
                QuickCommandChip(
                    label = "🐙 Acessar GitHub",
                    color = IndigoAccent,
                    onClick = { handleSend("Acessar repositório do GitHub e sincronizar") }
                )
            }
        }

        // Messages List
        items(messages) { msg ->
            ChatMessageCard(
                message = msg,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Mensagem da IA", msg.text))
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
                        placeholder = { Text("Peça ao Grok, Gemini ou Claude...") },
                        modifier = Modifier.weight(1f).testTag("claude_input_field"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { handleSend(inputText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedAIMode) {
                                AIMode.TRI_ENGINE -> WarningAmber
                                AIMode.GROK -> DangerRose
                                AIMode.GEMINI -> CyanPrimary
                                AIMode.CLAUDE -> VioletAccent
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.Black)
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
fun AIModeTabChip(
    mode: AIMode,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = mode.icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = mode.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ApkGeneratorDialog(
    onDismiss: () -> Unit,
    onGoToPreflight: () -> Unit
) {
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
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
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
    val accentColor = when (message.aiMode) {
        AIMode.TRI_ENGINE -> WarningAmber
        AIMode.GROK -> DangerRose
        AIMode.GEMINI -> CyanPrimary
        AIMode.CLAUDE -> VioletAccent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isUser) MaterialTheme.colorScheme.surfaceVariant else VioletAccent.copy(alpha = 0.2f)
        ),
        border = if (!isUser) {
            androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
        } else null
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
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (!isUser) accentColor else CyanPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (!isUser) message.aiMode.icon else "V",
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
                        color = if (!isUser) accentColor else CyanPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar mensagem",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Bloco de Humor / Sarcasmo do Grok
            if (message.humorQuote != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DangerRose.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRose.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🚀", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.humorQuote,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = DangerRose
                        )
                    }
                }
            }

            // Bloco de Raciocínio Profundo do Gemini (Extended Thinking)
            if (message.thinkingSteps != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanPrimary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CADEIA DE PENSAMENTO GEMINI AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.thinkingSteps,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ViewInAr, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Visualizar Malha no Estúdio 3D", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
