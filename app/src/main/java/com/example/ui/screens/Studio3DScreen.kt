package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.Engine3D
import com.example.engine.Mesh3D
import com.example.ui.components.RenderMode3D
import com.example.ui.components.SectionHeader
import com.example.ui.components.Viewport3D
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRose
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun Studio3DScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val models = remember {
        mutableStateListOf(
            Engine3D.createCrystalCube(),
            Engine3D.createCyberDrone(),
            Engine3D.createMechAndroid(),
            Engine3D.createArchitectureTower(),
            Engine3D.createTorus()
        )
    }

    var selectedModelIndex by remember { mutableIntStateOf(0) }
    var renderMode by remember { mutableStateOf(RenderMode3D.HYBRID) }
    var autoRotate by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var inspectorTab by remember { mutableIntStateOf(0) } // 0: GLB Structure, 1: OBJ Format, 2: Renders Gallery

    var showUrlDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun getFilenameFromUri(uri: Uri): String {
        var result = "custom_model.glb"
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        return result
    }

    // Zero-permission Android file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val filename = getFilenameFromUri(uri)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val customMesh = Engine3D.loadModelFromBytes(bytes, filename)
                    models.add(customMesh)
                    selectedModelIndex = models.size - 1
                    statusMessage = "Carregado com sucesso: ${customMesh.name} (${customMesh.vertexCount} vértices, ${customMesh.polygonCount} faces)"
                    Toast.makeText(context, "Modelo 3D importado!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                statusMessage = "Erro ao ler arquivo: ${e.message}"
                Toast.makeText(context, "Erro no arquivo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val activeMesh = models.getOrElse(selectedModelIndex) { models[0] }
    val glbSummary = remember(activeMesh) { Engine3D.getGlbStructureSummary(activeMesh) }
    val objCode = remember(activeMesh) { Engine3D.exportToObj(activeMesh) }
    val gltfJson = remember(activeMesh) { Engine3D.exportGltfJson(activeMesh) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
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
                        text = "3D Studio & GLB Engine",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Estrutura 3D Completa • Importador de Arquivos & URLs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanPrimary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "60 FPS 3D",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Custom GLB Load Actions (File Picker & URL)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "IMPORTAR MODELO 3D CUSTOMIZADO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.weight(1f).testTag("btn_pick_glb_file"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Arquivo", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showUrlDialog = true },
                            modifier = Modifier.weight(1f).testTag("btn_load_glb_url"),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Carregar URL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (statusMessage!!.contains("Erro")) DangerRose else SuccessEmerald,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (isDownloading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CyanPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Baixando e parseando modelo 3D...", style = MaterialTheme.typography.bodySmall, color = CyanPrimary)
                        }
                    }
                }
            }
        }

        // Real-Time 3D Viewport
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary.copy(alpha = 0.4f))
            ) {
                Viewport3D(
                    mesh = activeMesh,
                    renderMode = renderMode,
                    autoRotate = autoRotate,
                    scaleFactor = zoomScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 3D Controls Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rotação 360°:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(checked = autoRotate, onCheckedChange = { autoRotate = it })
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Zoom: ${(zoomScale * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = zoomScale,
                        onValueChange = { zoomScale = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Modo de Renderização 3D:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RenderMode3D.values().forEach { mode ->
                            val isSel = renderMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyanPrimary else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { renderMode = mode }
                            ) {
                                Text(
                                    text = mode.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    fontSize = 10.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Model Switcher Selector
        item {
            SectionHeader(
                title = "Modelos & Estruturas Carregadas (${models.size})",
                subtitle = "Selecione uma malha tridimensional ou use o botão 'Abrir Arquivo' acima"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                models.forEachIndexed { index, m ->
                    val isSelected = selectedModelIndex == index
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) IndigoAccent.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) IndigoAccent else Color.Transparent),
                        modifier = Modifier.clickable { selectedModelIndex = index }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = m.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) IndigoAccent else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${m.vertexCount}v • ${m.polygonCount}p • ${m.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Structure Inspector Tabs
        item {
            TabRow(
                selectedTabIndex = inspectorTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = CyanPrimary
            ) {
                Tab(
                    selected = inspectorTab == 0,
                    onClick = { inspectorTab = 0 },
                    text = { Text("Estrutura .GLB", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = inspectorTab == 1,
                    onClick = { inspectorTab = 1 },
                    text = { Text("Malha .OBJ", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = inspectorTab == 2,
                    onClick = { inspectorTab = 2 },
                    text = { Text("Galeria 3D", fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (inspectorTab) {
            0 -> {
                // GLB Structure Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ESPECIFICAÇÃO BINÁRIA GLB / GLTF 2.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary
                                )

                                Button(
                                    onClick = { copyToClipboard(gltfJson, "glTF JSON") },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copiar JSON", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Magic Header: ${glbSummary.magic}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Versão: ${glbSummary.version}.0", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Tamanho Total: ${glbSummary.totalByteLength} bytes", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Chunk 0 (JSON): ${glbSummary.jsonChunkLength} bytes", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Chunk 1 (BIN): ${glbSummary.binaryChunkLength} bytes", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Material PBR: BaseColor + Metallic 0.85 + Roughness 0.25", style = MaterialTheme.typography.bodySmall, color = CyanPrimary)

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = gltfJson.take(600) + "\n...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Wavefront OBJ
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WAVEFRONT .OBJ 3D GEOMETRY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoAccent
                                )

                                Button(
                                    onClick = { copyToClipboard(objCode, "Modelo OBJ") },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copiar .OBJ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = objCode.take(800) + "\n# ... ${objCode.lines().size} linhas no total",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // 3D Renders Showcase Gallery
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = R.drawable.replica_3d_studio_1791546678820),
                                    contentDescription = "3D Holographic Robot Render",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Holographic Robot & Wireframe GLB", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Render fotorrealista 3D com iluminação volumétrica e malha vetorial.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = R.drawable.replica_3d_cube_1791546693829),
                                    contentDescription = "3D Cyber Crystal Cube Render",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Cyber Crystal Cube V1.0", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Estrutura poliédrica isométrica com vértices e facetas facetadas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showUrlDialog) {
        LoadModelUrlDialog(
            onDismiss = { showUrlDialog = false },
            onLoadUrl = { url ->
                showUrlDialog = false
                isDownloading = true
                statusMessage = "Conectando e baixando modelo de: $url"
                scope.launch {
                    try {
                        val (bytes, filename) = Engine3D.fetchModelFromUrl(url)
                        val customMesh = Engine3D.loadModelFromBytes(bytes, filename)
                        models.add(customMesh)
                        selectedModelIndex = models.size - 1
                        statusMessage = "Modelo carregado com sucesso: ${customMesh.name} (${customMesh.vertexCount} vértices)"
                        Toast.makeText(context, "Modelo GLB baixado com sucesso!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        statusMessage = "Falha ao baixar da URL: ${e.message}"
                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isDownloading = false
                    }
                }
            }
        )
    }
}

@Composable
fun LoadModelUrlDialog(
    onDismiss: () -> Unit,
    onLoadUrl: (String) -> Unit
) {
    var urlText by remember { mutableStateOf("https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Carregar GLB de URL Externa", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Insira o link direto para um arquivo .glb ou .obj para carregar e renderizar no visualizador 3D:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("URL do arquivo 3D (.glb ou .obj)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_glb_url"),
                    singleLine = true
                )

                Text("Sugestões de teste rápido:", style = MaterialTheme.typography.labelSmall)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "• Khronos Box.glb (Sample Oficial)",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanPrimary,
                        modifier = Modifier.clickable {
                            urlText = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
                        }
                    )
                    Text(
                        text = "• Khronos Triangle.glb",
                        style = MaterialTheme.typography.bodySmall,
                        color = IndigoAccent,
                        modifier = Modifier.clickable {
                            urlText = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Triangle/glTF-Binary/Triangle.glb"
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (urlText.isNotBlank()) {
                        onLoadUrl(urlText.trim())
                    }
                },
                enabled = urlText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Baixar e Renderizar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
