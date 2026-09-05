package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MetricsScreen
import com.example.ui.screens.RecordsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.viewmodel.VehicleViewModel

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val tag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val viewModel: VehicleViewModel = viewModel()
                val currentUser = viewModel.currentUser

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Trava de segurança do Firebase: se não estiver logado, exibe a tela de login
                    if (currentUser == null) {
                        LoginScreen(viewModel = viewModel)
                    } else {
                        MainAppScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: VehicleViewModel) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val navItems = listOf(
        NavItem("Chat IA", Icons.Default.AutoAwesome, "nav_chat"),
        NavItem("Métricas", Icons.Default.BarChart, "nav_metrics"),
        NavItem("Registros", Icons.Default.ListAlt, "nav_records"),
        NavItem("Relatórios", Icons.Default.PictureAsPdf, "nav_reports")
    )

    val configuration = LocalConfiguration.current
    val isTabletOrLandscape = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isTabletOrLandscape) {
                val borderColor = MaterialTheme.colorScheme.outlineVariant
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedIndex = index },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(text = item.title) },
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isTabletOrLandscape) {
            val borderColor = MaterialTheme.colorScheme.outlineVariant
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .drawBehind {
                            drawLine(
                                color = borderColor,
                                start = Offset(size.width, 0f),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { selectedIndex = index },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(text = item.title) },
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    ScreenContent(selectedIndex = selectedIndex, viewModel = viewModel)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ScreenContent(selectedIndex = selectedIndex, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ScreenContent(
    selectedIndex: Int,
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    when (selectedIndex) {
        0 -> ChatScreen(viewModel = viewModel, modifier = modifier)
        1 -> MetricsScreen(viewModel = viewModel, modifier = modifier)
        2 -> RecordsScreen(viewModel = viewModel, modifier = modifier)
        3 -> ReportsScreen(viewModel = viewModel, modifier = modifier)
    }
}
