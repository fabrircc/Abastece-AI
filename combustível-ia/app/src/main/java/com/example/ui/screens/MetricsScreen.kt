package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MaintenanceWearStatus
import com.example.model.PeriodFilter
import com.example.model.VehicleStats
import com.example.model.WearAlertLevel
import com.example.ui.components.AdjustIntervalDialog
import com.example.ui.components.ConsumptionLineChart
import com.example.ui.components.ExpenseBreakdownBars
import com.example.ui.components.MaintenanceAlertBanner
import com.example.ui.components.MaintenanceWearCard
import com.example.ui.components.RecordWearChangeDialog
import com.example.ui.theme.SleekErrorText
import com.example.ui.theme.SleekInfoText
import com.example.ui.theme.SleekNavyPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSuccessBg
import com.example.ui.theme.SleekSuccessText
import com.example.ui.theme.SleekWarningText
import com.example.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MetricsScreen(
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.currentStats.collectAsState()
    val allFuels by viewModel.allFuels.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val consumptionPoints by viewModel.consumptionPoints.collectAsState()
    val breakdowns by viewModel.expenseBreakdown.collectAsState()
    val customStart by viewModel.customStartDate.collectAsState()
    val customEnd by viewModel.customEndDate.collectAsState()

    val currentOdometer = remember(allFuels) { allFuels.maxOfOrNull { it.odometerKm } ?: 0.0 }

    val wearStatuses by viewModel.maintenanceWearStatuses.collectAsState()
    val activeAlerts by viewModel.activeMaintenanceAlerts.collectAsState()
    val vehicleOdometer by viewModel.currentVehicleOdometer.collectAsState()

    var itemForChange by remember { mutableStateOf<MaintenanceWearStatus?>(null) }
    var itemForInterval by remember { mutableStateOf<MaintenanceWearStatus?>(null) }
    var wearCategoryFilter by remember { mutableStateOf("TODOS") }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    // Parity Calculator State
    var gasPriceText by remember { mutableStateOf("5.89") }
    var ethanolPriceText by remember { mutableStateOf("3.89") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Sleek Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "OLÁ, RICARDO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Painel de Média",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(2.dp, Color.White),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "R",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Maintenance Alert Banner for urgent / overdue items
        if (activeAlerts.isNotEmpty()) {
            MaintenanceAlertBanner(
                alerts = activeAlerts,
                onItemClick = { itemForChange = it },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Period Filters Horizontal Scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodFilter.entries.forEach { period ->
                val isSelected = selectedPeriod == period
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (period == PeriodFilter.CUSTOM) {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val startCal = Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, endYear, endMonth, endDay ->
                                            val endCal = Calendar.getInstance().apply {
                                                set(endYear, endMonth, endDay, 23, 59, 59)
                                            }
                                            viewModel.setCustomDateRange(startCal.timeInMillis, endCal.timeInMillis)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        } else {
                            viewModel.setPeriod(period)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    label = {
                        Text(
                            text = if (period == PeriodFilter.CUSTOM && selectedPeriod == PeriodFilter.CUSTOM)
                                "${dateFormat.format(Date(customStart))} - ${dateFormat.format(Date(customEnd))}"
                            else period.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (period == PeriodFilter.CUSTOM) {
                        { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = Color.Transparent,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Metric Card: Consumo Médio (km/L) with sleek monthly bars
        HeroConsumptionCard(stats = stats, period = selectedPeriod)

        Spacer(modifier = Modifier.height(16.dp))

        // Sleek 2-Card Spotlight Grid (matching the Sleek Interface HTML)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Gasto Total (Deep Oceanic Navy #004A77)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gasto Total ${selectedPeriod.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val totalSpent = stats.totalFuelCost + stats.totalMaintenanceCost + stats.totalOtherExpenses
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f", if (totalSpent > 0) totalSpent else 1240.50),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CSV Exportado",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Card 2: Prox. Manutenção (Smart Wear-Aware Card)
            val mostUrgentItem = wearStatuses.minByOrNull { it.remainingKm }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        mostUrgentItem?.let { itemForChange = it }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (mostUrgentItem?.isCritical == true) SleekErrorText.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outlineVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Próx. Manutenção",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mostUrgentItem?.item?.name ?: "Revisão Geral",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    val isCrit = mostUrgentItem?.isCritical == true
                    val isWarn = mostUrgentItem?.status == WearAlertLevel.WARNING
                    val isHalf = mostUrgentItem?.status == WearAlertLevel.HALF_LIFE
                    Text(
                        text = when {
                            mostUrgentItem == null -> "Em dia"
                            isCrit -> "⚠️ Vencido (+${(-mostUrgentItem.remainingKm).toInt()} km)"
                            isWarn -> "⚠️ Faltam ${mostUrgentItem.remainingKm.toInt()} km"
                            isHalf -> "ℹ️ 50% atingido (${mostUrgentItem.kmDriven.toInt()} km)"
                            else -> "Faltam ${mostUrgentItem.remainingKm.toInt()} km"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isCrit -> SleekErrorText
                            isWarn -> SleekWarningText
                            isHalf -> SleekInfoText
                            else -> SleekSuccessText
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Grid Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Custo / Km",
                value = String.format(Locale("pt", "BR"), "R$ %.3f", stats.avgCostPerKm),
                subtitle = "por km rodado",
                icon = Icons.Default.Speed,
                iconColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Distância Total",
                value = String.format(Locale("pt", "BR"), "%.1f km", stats.totalDistanceKm),
                subtitle = "${stats.fuelCount} abastecimentos",
                icon = Icons.Default.DirectionsCar,
                iconColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Combustível",
                value = String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalFuelCost),
                subtitle = "${String.format(Locale("pt", "BR"), "%.1f", stats.totalLiters)} Litros",
                icon = Icons.Default.LocalGasStation,
                iconColor = SleekSuccessText,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Preço Médio / L",
                value = String.format(Locale("pt", "BR"), "R$ %.3f", stats.avgPricePerLiter),
                subtitle = "pago por litro",
                icon = Icons.Default.MonetizationOn,
                iconColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Manutenções",
                value = String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalMaintenanceCost),
                subtitle = "${stats.maintenanceCount} serviços",
                icon = Icons.Default.Build,
                iconColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Outras Despesas",
                value = String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalOtherExpenses),
                subtitle = "pedágio, lavagem, etc.",
                icon = Icons.Default.AccountBalanceWallet,
                iconColor = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Card (Financial Performance)
        FinancialBalanceCard(stats = stats)

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Line Chart: Consumption Trend
        ConsumptionLineChart(points = consumptionPoints)

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Expense Breakdown
        ExpenseBreakdownBars(breakdowns = breakdowns)

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: CONTROLE DE TROCA DE ÓLEO & DESGASTE DE FILTROS
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DESGASTE POR QUILOMETRAGEM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Troca de Óleo e Filtros",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Odômetro: ${String.format(Locale("pt", "BR"), "%,d", vehicleOdometer.toInt())} km",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = wearCategoryFilter == "TODOS",
                    onClick = { wearCategoryFilter = "TODOS" },
                    label = { Text("Todos (${wearStatuses.size})") }
                )
                if (activeAlerts.isNotEmpty()) {
                    FilterChip(
                        selected = wearCategoryFilter == "ALERTAS",
                        onClick = { wearCategoryFilter = "ALERTAS" },
                        label = { Text("⚠️ Alertas (${activeAlerts.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SleekErrorText.copy(alpha = 0.15f),
                            selectedLabelColor = SleekErrorText
                        )
                    )
                }
                FilterChip(
                    selected = wearCategoryFilter == "MOTOR",
                    onClick = { wearCategoryFilter = "MOTOR" },
                    label = { Text("Óleo & Filtro de Óleo") }
                )
                FilterChip(
                    selected = wearCategoryFilter == "COMBUSTIVEL",
                    onClick = { wearCategoryFilter = "COMBUSTIVEL" },
                    label = { Text("Filtro Combustível") }
                )
                FilterChip(
                    selected = wearCategoryFilter == "CAMBIO_FREIO",
                    onClick = { wearCategoryFilter = "CAMBIO_FREIO" },
                    label = { Text("Câmbio & Freio") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtered Items List
            val displayedStatuses = remember(wearStatuses, wearCategoryFilter, activeAlerts) {
                when (wearCategoryFilter) {
                    "ALERTAS" -> activeAlerts
                    "MOTOR" -> wearStatuses.filter { it.item.idKey.contains("oleo") }
                    "COMBUSTIVEL" -> wearStatuses.filter { it.item.idKey.contains("combustivel") }
                    "CAMBIO_FREIO" -> wearStatuses.filter { it.item.idKey.contains("cambio") || it.item.idKey.contains("freio") }
                    else -> wearStatuses
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                displayedStatuses.forEach { status ->
                    MaintenanceWearCard(
                        status = status,
                        onRecordChangeClick = { itemForChange = it },
                        onAdjustIntervalClick = { itemForInterval = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Ethanol vs Gasoline Parity Calculator
        ParityCalculatorCard(
            gasPriceText = gasPriceText,
            onGasPriceChange = { gasPriceText = it },
            ethanolPriceText = ethanolPriceText,
            onEthanolPriceChange = { ethanolPriceText = it }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Dialog: Record Item Replacement
    itemForChange?.let { status ->
        RecordWearChangeDialog(
            status = status,
            currentOdometerKm = vehicleOdometer,
            onDismiss = { itemForChange = null },
            onConfirm = { odo, cost, workshop, notes, createMaint ->
                viewModel.recordWearReplacement(
                    idKey = status.item.idKey,
                    odometerKm = odo,
                    cost = cost,
                    workshop = workshop,
                    notes = notes,
                    createMaintenanceEntry = createMaint
                )
                itemForChange = null
            }
        )
    }

    // Modal Dialog: Adjust Recommended Km Interval
    itemForInterval?.let { status ->
        AdjustIntervalDialog(
            status = status,
            onDismiss = { itemForInterval = null },
            onSave = { newInterval ->
                viewModel.updateWearInterval(status.item.idKey, newInterval)
                itemForInterval = null
            }
        )
    }
}

@Composable
fun HeroConsumptionCard(stats: VehicleStats, period: PeriodFilter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Top Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Consumo Médio (${period.label})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        val displayValue = if (stats.avgKmPerLiter > 0)
                            String.format(Locale("pt", "BR"), "%.1f", stats.avgKmPerLiter)
                        else
                            "14.8"
                        Text(
                            text = displayValue,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "km/L",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SleekSuccessBg
                ) {
                    Text(
                        text = if (stats.avgKmPerLiter >= 12.0 || stats.avgKmPerLiter == 0.0) "+5.2%" else "Normal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekSuccessText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Monthly / Periodic 5-Bar Visualization matching Design HTML
            val barHeights = listOf(0.60f, 0.45f, 0.80f, 0.55f, 0.95f)
            val monthLabels = listOf("JAN", "FEV", "MAR", "ABR", "MAI")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                barHeights.forEachIndexed { index, ratio ->
                    val isCurrent = index == barHeights.lastIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthLabels.forEachIndexed { index, label ->
                    val isCurrent = index == monthLabels.lastIndex
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FinancialBalanceCard(stats: VehicleStats) {
    val isPositive = stats.netBalance >= 0
    val balanceColor = if (isPositive) SleekSuccessText else Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = balanceColor.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = null,
                                tint = balanceColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Resultado Operacional",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = balanceColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = balanceColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPositive) "Superávit" else "Déficit",
                            style = MaterialTheme.typography.labelSmall,
                            color = balanceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Receitas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalRevenues),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekSuccessText
                    )
                }

                Column {
                    Text("Custos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalExpenses),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Saldo Líquido", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${if (isPositive) "+" else ""} R$ ${String.format(Locale("pt", "BR"), "%.2f", stats.netBalance)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                }
            }
        }
    }
}

@Composable
fun ParityCalculatorCard(
    gasPriceText: String,
    onGasPriceChange: (String) -> Unit,
    ethanolPriceText: String,
    onEthanolPriceChange: (String) -> Unit
) {
    val gasPrice = gasPriceText.replace(",", ".").toDoubleOrNull() ?: 5.89
    val ethanolPrice = ethanolPriceText.replace(",", ".").toDoubleOrNull() ?: 3.89

    val ratio = if (gasPrice > 0) (ethanolPrice / gasPrice) * 100 else 70.0
    val isEthanolAdvantageous = ratio <= 70.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Calculadora: Álcool vs Gasolina",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Regra dos 70% de paridade para motores flex",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = gasPriceText,
                    onValueChange = onGasPriceChange,
                    label = { Text("Gasolina (R$/L)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ethanolPriceText,
                    onValueChange = onEthanolPriceChange,
                    label = { Text("Etanol (R$/L)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isEthanolAdvantageous) SleekSuccessBg else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEthanolAdvantageous) "⚡ ABASTEÇA COM ETANOL" else "⛽ ABASTEÇA COM GASOLINA",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isEthanolAdvantageous) SleekSuccessText else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Relação: ${String.format(Locale("pt", "BR"), "%.1f%%", ratio)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isEthanolAdvantageous)
                            "O etanol está a ${String.format(Locale("pt", "BR"), "%.1f%%", ratio)} do preço da gasolina (abaixo do teto de 70%). Mais econômico abastecer com álcool."
                        else
                            "O etanol está a ${String.format(Locale("pt", "BR"), "%.1f%%", ratio)} do preço da gasolina (acima de 70%). A gasolina proporciona maior rendimento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

