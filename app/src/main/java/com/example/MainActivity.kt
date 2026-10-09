package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ReplicaViewModel
import com.example.ui.screens.BrandSweepScreen
import com.example.ui.screens.ClaudeCopilotScreen
import com.example.ui.screens.ContrastScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeployPreflightScreen
import com.example.ui.screens.ParityScreen
import com.example.ui.screens.ReviewsScreen
import com.example.ui.screens.SkillsHubScreen
import com.example.ui.screens.StoreListingScreen
import com.example.ui.screens.Studio3DScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ReplicaTheme

sealed class Screen(val title: String, val icon: ImageVector) {
    object Dashboard : Screen("Painel", Icons.Default.Dashboard)
    object Studio3D : Screen("Estúdio 3D", Icons.Default.ViewInAr)
    object Claude : Screen("Claude", Icons.Default.AutoAwesome)
    object Skills : Screen("11 Skills", Icons.Default.Explore)
    object Parity : Screen("Paridade", Icons.Default.Compare)
    object Reviews : Screen("Avaliações", Icons.Default.Psychology)
    object Contrast : Screen("Contraste", Icons.Default.ColorLens)
    object Sweep : Screen("Varredura", Icons.Default.Security)
    object Listing : Screen("Loja", Icons.Default.RocketLaunch)
    object Preflight : Screen("Preflight", Icons.Default.RocketLaunch)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReplicaTheme(darkTheme = true) {
                ReplicaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplicaApp(viewModel: ReplicaViewModel = viewModel()) {
    var selectedScreenIndex by rememberSaveable { mutableIntStateOf(0) }
    val activeProject by viewModel.activeProject.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Replica Studio 3D",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    if (activeProject != null) {
                        Text(
                            text = activeProject!!.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val primaryNavs = listOf(
                    0 to Screen.Dashboard,
                    1 to Screen.Studio3D,
                    2 to Screen.Claude,
                    3 to Screen.Skills,
                    4 to Screen.Parity
                )

                primaryNavs.forEach { (index, screen) ->
                    val isSelected = selectedScreenIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedScreenIndex = index },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.title.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedScreenIndex) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateTo3D = { selectedScreenIndex = 1 },
                    onNavigateToClaude = { selectedScreenIndex = 2 },
                    onNavigateToSkills = { selectedScreenIndex = 3 },
                    onNavigateToParity = { selectedScreenIndex = 4 },
                    onNavigateToReviews = { selectedScreenIndex = 5 },
                    onNavigateToContrast = { selectedScreenIndex = 6 },
                    onNavigateToSweep = { selectedScreenIndex = 7 },
                    onNavigateToListing = { selectedScreenIndex = 8 },
                    onNavigateToPreflight = { selectedScreenIndex = 9 }
                )
                1 -> Studio3DScreen()
                2 -> ClaudeCopilotScreen(
                    viewModel = viewModel,
                    onNavigateTo3D = { selectedScreenIndex = 1 },
                    onNavigateToPreflight = { selectedScreenIndex = 9 }
                )
                3 -> SkillsHubScreen(
                    onNavigateToParity = { selectedScreenIndex = 4 },
                    onNavigateToReviews = { selectedScreenIndex = 5 },
                    onNavigateToContrast = { selectedScreenIndex = 6 },
                    onNavigateToSweep = { selectedScreenIndex = 7 },
                    onNavigateToListing = { selectedScreenIndex = 8 },
                    onNavigateToPreflight = { selectedScreenIndex = 9 }
                )
                4 -> ParityScreen(viewModel = viewModel)
                5 -> ReviewsScreen(viewModel = viewModel)
                6 -> ContrastScreen(viewModel = viewModel)
                7 -> BrandSweepScreen(viewModel = viewModel)
                8 -> StoreListingScreen(viewModel = viewModel)
                9 -> DeployPreflightScreen(viewModel = viewModel)
                else -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateTo3D = { selectedScreenIndex = 1 },
                    onNavigateToClaude = { selectedScreenIndex = 2 },
                    onNavigateToSkills = { selectedScreenIndex = 3 },
                    onNavigateToParity = { selectedScreenIndex = 4 },
                    onNavigateToReviews = { selectedScreenIndex = 5 },
                    onNavigateToContrast = { selectedScreenIndex = 6 },
                    onNavigateToSweep = { selectedScreenIndex = 7 },
                    onNavigateToListing = { selectedScreenIndex = 8 },
                    onNavigateToPreflight = { selectedScreenIndex = 9 }
                )
            }
        }
    }
}
