package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRose
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.WarningAmber

data class SkillItem(
    val command: String,
    val name: String,
    val stage: String,
    val description: String,
    val inputs: String,
    val outputs: String,
    val rules: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun SkillsHubScreen(
    onNavigateToParity: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToContrast: () -> Unit,
    onNavigateToSweep: () -> Unit,
    onNavigateToListing: () -> Unit,
    onNavigateToPreflight: () -> Unit
) {
    val skills = remember {
        listOf(
            SkillItem(
                command = "/replica-recon",
                name = "Reconhecimento",
                stage = "1 / 11",
                description = "Engenharia reversa de qualquer aplicativo-alvo: telas, fluxos, componentes e modelo de dados inferido. A partir de páginas públicas, lojas e contas de teste.",
                inputs = "URL do app, central de ajuda, página da loja, capturas de tela",
                outputs = "replica/recon.md, replica/features.csv (a matriz de recursos)",
                rules = "Clean-room estrito: leia apenas páginas públicas e contas pessoais; nunca colete dados atrás de autenticações não autorizadas.",
                icon = Icons.Default.Explore,
                accentColor = CyanPrimary
            ),
            SkillItem(
                command = "/replica-architect",
                name = "Arquiteto",
                stage = "2 / 11",
                description = "Planeja a arquitetura técnica, esquema do banco de dados, restrições e ordem de desenvolvimento do clone. Mapeia jornadas críticas primeiro.",
                inputs = "replica/recon.md",
                outputs = "replica/architecture.md (esquema, APIs, sequência de build)",
                rules = "Restrições primeiro: projete travas estruturais no banco para que conflitos (ex: agendamentos duplicados) sejam fisicamente impossíveis.",
                icon = Icons.Default.Architecture,
                accentColor = IndigoAccent
            ),
            SkillItem(
                command = "/replica-design",
                name = "Design & Tokens",
                stage = "3 / 11",
                description = "Recria o design system: paleta de cores, escala tipográfica, espaçamento de 8px e tokens. Executa validador matemático de contraste WCAG 2.2.",
                inputs = "Capturas de tela, observações visuais da marca",
                outputs = "replica/design/tokens.json, auditorias contrast.py",
                rules = "Ícones e fontes de licença aberta, 100% de conformidade de contraste WCAG AA matematicamente validada.",
                icon = Icons.Default.ColorLens,
                accentColor = VioletAccent
            ),
            SkillItem(
                command = "/replica-build",
                name = "Construção de UI",
                stage = "4 / 11",
                description = "Reconstrói o aplicativo tela por tela a partir do mapa de reconhecimento. Implementa todos os estados: vazio, carregando, erro e sucesso.",
                inputs = "replica/recon.md, tokens.json",
                outputs = "Componentes Jetpack Compose, telas interativas, estados locais",
                rules = "Construa a carcaça primeiro, depois o fluxo principal ponta a ponta, marcando o progresso na matriz de recursos.",
                icon = Icons.Default.Build,
                accentColor = CyanPrimary
            ),
            SkillItem(
                command = "/replica-backend",
                name = "Backend & Integrações",
                stage = "5 / 11",
                description = "Autenticação, persistência em banco de dados, pagamentos e integrações externas reais (Google Calendar, Stripe, Twilio SMS).",
                inputs = "replica/architecture.md",
                outputs = "APIs, esquemas de dados, manipuladores de webhooks",
                rules = "Apenas integrações reais; sem soluções falsas ou dados simulados para fluxos centrais.",
                icon = Icons.Default.Storage,
                accentColor = IndigoAccent
            ),
            SkillItem(
                command = "/replica-test",
                name = "Testes & Qualidade",
                stage = "6 / 11",
                description = "Cria planos de testes e executa Jornadas Críticas do Usuário. Classifica bugs em S1 (Bloqueador), S2 (Crítico), S3 e S4.",
                inputs = "Fluxos de usuário, casos de borda (fusos horários, concorrência)",
                outputs = "replica/test-plan.md, relatórios de bugs",
                rules = "Nenhum bug S1 ou S2 pode permanecer aberto para o lançamento.",
                icon = Icons.Default.BugReport,
                accentColor = DangerRose
            ),
            SkillItem(
                command = "/replica-diff",
                name = "Diferença & Paridade",
                stage = "7 / 11",
                description = "Calcula a pontuação ponderada de paridade de recursos (Must: 3, Should: 2, Could: 1). Exibe análise de áreas fracas e checklist de pendências.",
                inputs = "replica/features.csv, capturas de tela",
                outputs = "Pontuação parity.py (0-100), relatórios de paridade",
                rules = "Todos os itens obrigatórios (must-have) devem estar concluídos (100%) e paridade total >= 80% para liberação.",
                icon = Icons.Default.Compare,
                accentColor = WarningAmber
            ),
            SkillItem(
                command = "/replica-entrepreneur",
                name = "Empreendedor & Reviews",
                stage = "8 / 11",
                description = "Analisa centenas de avaliações reais da App Store, G2, Capterra e Reddit. Descobre dores dos clientes e as transforma em diferenciais matadores.",
                inputs = "replica/reviews.csv",
                outputs = "Dores ranqueadas, citações reais com links, ângulo de posicionamento",
                rules = "Nunca invente dados: cada ponto liga-se a uma avaliação real com URL e nota.",
                icon = Icons.Default.Psychology,
                accentColor = VioletAccent
            ),
            SkillItem(
                command = "/replica-brand",
                name = "Marca & Varredura Limpa",
                stage = "9 / 11",
                description = "Cria novo nome e identidade visual clean-room. Executa scanner de varredura no código para garantir zero marcas registradas do concorrente.",
                inputs = "Lista de marcas, nomes e domínios do alvo",
                outputs = "Relatório de aprovação/rejeição do sweep.py, guia da marca",
                rules = "Deploy bloqueado se a varredura encontrar nomes, domínios ou cores do concorrente no código.",
                icon = Icons.Default.BrandingWatermark,
                accentColor = IndigoAccent
            ),
            SkillItem(
                command = "/replica-launch",
                name = "Lançamento & Listagem",
                stage = "10 / 11",
                description = "Elabora textos de landing page, precificação justa e valida limites de caracteres e regras da Google Play e App Store.",
                inputs = "replica/launch/listing.json",
                outputs = "Relatório de conformidade da loja, validador de alegações de ranking",
                rules = "Aplica título de até 30 caracteres na Play, descrição curta de até 80, proíbe emojis e termos promocionais (#1).",
                icon = Icons.Default.RocketLaunch,
                accentColor = SuccessEmerald
            ),
            SkillItem(
                command = "/replica-deploy",
                name = "Preflight de Lançamento",
                stage = "11 / 11",
                description = "Executa a auditoria mestra de pré-voo (testes, paridade, varredura, listagem da loja), valida prontidão para produção e assina o release.",
                inputs = "Saídas de todas as etapas anteriores",
                outputs = "replica/preflight.md, Decisão Go/No-Go",
                rules = "Todos os 6 portões de pré-voo devem passar antes do lançamento.",
                icon = Icons.Default.RocketLaunch,
                accentColor = CyanPrimary
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Motor de Clonagem em 11 Etapas",
                subtitle = "Pipeline completo: da engenharia reversa até a publicação nas lojas de aplicativos"
            )
        }

        items(skills) { skill ->
            SkillCard(
                skill = skill,
                onOpenTool = {
                    when (skill.command) {
                        "/replica-diff" -> onNavigateToParity()
                        "/replica-entrepreneur" -> onNavigateToReviews()
                        "/replica-design" -> onNavigateToContrast()
                        "/replica-brand" -> onNavigateToSweep()
                        "/replica-launch" -> onNavigateToListing()
                        "/replica-deploy" -> onNavigateToPreflight()
                        else -> onNavigateToParity()
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SkillCard(
    skill: SkillItem,
    onOpenTool: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(skill.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = skill.icon,
                            contentDescription = null,
                            tint = skill.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = skill.command,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = skill.accentColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = skill.stage,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = skill.accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = skill.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Alternar detalhes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Entradas: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = skill.inputs,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Saídas: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = IndigoAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = skill.outputs,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Regra Estrita: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = skill.rules,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenTool,
                        colors = ButtonDefaults.buttonColors(containerColor = skill.accentColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abrir Ferramenta & Bancada", color = Color.Black, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
