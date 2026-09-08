package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.MaintenanceEntry
import com.example.model.MaintenanceWearStatus
import com.example.model.WearAlertLevel
import com.example.ui.components.AddEntryDialog
import com.example.ui.components.RecordWearChangeDialog
import com.example.ui.theme.SleekErrorBg
import com.example.ui.theme.SleekErrorText
import com.example.ui.theme.SleekInfoBg
import com.example.ui.theme.SleekInfoText
import com.example.ui.theme.SleekNavyPrimary
import com.example.ui.theme.SleekSuccessBg
import com.example.ui.theme.SleekSuccessText
import com.example.ui.theme.SleekWarningBg
import com.example.ui.theme.SleekWarningText
import com.example.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val fuels by viewModel.allFuels.collectAsState()
    val maintenances by viewModel.allMaintenance.collectAsState()
    val finances by viewModel.allFinances.collectAsState()

    val wearStatuses by viewModel.maintenanceWearStatuses.collectAsState()
    val activeAlerts by viewModel.activeMaintenanceAlerts.collectAsState()
    val vehicleOdometer by viewModel.currentVehicleOdometer.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todos", "Combustível", "Manutenção", "Finanças")

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemForChange by remember { mutableStateOf<MaintenanceWearStatus?>(null) }

    // Deletion states
    var fuelToDelete by remember { mutableStateOf<FuelEntry?>(null) }
    var maintToDelete by remember { mutableStateOf<MaintenanceEntry?>(null) }
    var financeToDelete by remember { mutableStateOf<FinanceEntry?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }

    val headerBorderColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_entry_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Registro")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sleek Header & Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = headerBorderColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
                    Text(
                        text = "HISTÓRICO FINANCEIRO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Lançamentos e Registros",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sleek Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Pesquisar por posto, oficina, categoria...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // Sleek Filter Chips (Tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        shape = RoundedCornerShape(20.dp),
                        label = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedLabelColor = Color.White
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

            val query = searchQuery.trim().lowercase(Locale.ROOT)

            // Filtered lists
            val filteredFuels = fuels.filter {
                query.isBlank() ||
                        it.stationName.lowercase(Locale.ROOT).contains(query) ||
                        it.fuelType.displayName.lowercase(Locale.ROOT).contains(query) ||
                        it.notes.lowercase(Locale.ROOT).contains(query)
            }

            val filteredMaint = maintenances.filter {
                query.isBlank() ||
                        it.serviceType.lowercase(Locale.ROOT).contains(query) ||
                        it.workshopName.lowercase(Locale.ROOT).contains(query) ||
                        it.notes.lowercase(Locale.ROOT).contains(query)
            }

            val filteredFinances = finances.filter {
                query.isBlank() ||
                        it.category.lowercase(Locale.ROOT).contains(query) ||
                        it.notes.lowercase(Locale.ROOT).contains(query)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedTab) {
                    0 -> { // ALL
                        if (filteredFuels.isEmpty() && filteredMaint.isEmpty() && filteredFinances.isEmpty()) {
                            item { EmptyStateView() }
                        }
                        items(filteredFuels, key = { "f_${it.id}" }) { item ->
                            FuelRecordCard(item, dateFormat, onDelete = { fuelToDelete = item })
                        }
                        items(filteredMaint, key = { "m_${it.id}" }) { item ->
                            MaintenanceRecordCard(item, dateFormat, onDelete = { maintToDelete = item })
                        }
                        items(filteredFinances, key = { "fn_${it.id}" }) { item ->
                            FinanceRecordCard(item, dateFormat, onDelete = { financeToDelete = item })
                        }
                    }
                    1 -> { // FUELS
                        if (filteredFuels.isEmpty()) {
                            item { EmptyStateView("Nenhum abastecimento encontrado.") }
                        }
                        items(filteredFuels, key = { "f_${it.id}" }) { item ->
                            FuelRecordCard(item, dateFormat, onDelete = { fuelToDelete = item })
                        }
                    }
                    2 -> { // MAINTENANCE
                        if (wearStatuses.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Desgaste de Filtros & Óleo",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (activeAlerts.isNotEmpty()) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = SleekErrorBg
                                                ) {
                                                    Text(
                                                        text = "${activeAlerts.size} ALERTA(S)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = SleekErrorText,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            wearStatuses.forEach { status ->
                                                val isCrit = status.isCritical
                                                val isWarn = status.status == WearAlertLevel.WARNING
                                                val isHalf = status.status == WearAlertLevel.HALF_LIFE
                                                val tagBg = when {
                                                    isCrit -> SleekErrorBg
                                                    isWarn -> SleekWarningBg
                                                    isHalf -> SleekInfoBg
                                                    else -> SleekSuccessBg
                                                }
                                                val tagText = when {
                                                    isCrit -> SleekErrorText
                                                    isWarn -> SleekWarningText
                                                    isHalf -> SleekInfoText
                                                    else -> SleekSuccessText
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = tagBg,
                                                    border = BorderStroke(0.5.dp, tagText.copy(alpha = 0.3f)),
                                                    modifier = Modifier.clickable { itemForChange = status }
                                                ) {
                                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                                        Text(
                                                            text = status.item.name,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = tagText
                                                        )
                                                        Text(
                                                            text = when {
                                                                status.remainingKm <= 0 -> "${String.format(Locale("pt", "BR"), "%.0f", status.depreciationPercentage)}% • Vencido"
                                                                isWarn -> "${String.format(Locale("pt", "BR"), "%.0f", status.depreciationPercentage)}% • Faltam ${status.remainingKm.toInt()} km"
                                                                isHalf -> "${String.format(Locale("pt", "BR"), "%.0f", status.depreciationPercentage)}% • 50% Atingido"
                                                                else -> "${String.format(Locale("pt", "BR"), "%.0f", status.depreciationPercentage)}% • Em dia"
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 10.sp,
                                                            color = tagText.copy(alpha = 0.85f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredMaint.isEmpty()) {
                            item { EmptyStateView("Nenhuma manutenção encontrada.") }
                        }
                        items(filteredMaint, key = { "m_${it.id}" }) { item ->
                            MaintenanceRecordCard(item, dateFormat, onDelete = { maintToDelete = item })
                        }
                    }
                    3 -> { // FINANCES
                        if (filteredFinances.isEmpty()) {
                            item { EmptyStateView("Nenhuma receita ou despesa encontrada.") }
                        }
                        items(filteredFinances, key = { "fn_${it.id}" }) { item ->
                            FinanceRecordCard(item, dateFormat, onDelete = { financeToDelete = item })
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for adding entries
    if (showAddDialog) {
        AddEntryDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    // Delete Fuel confirmation
    fuelToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { fuelToDelete = null },
            title = { Text("Excluir Abastecimento?") },
            text = { Text("Deseja realmente apagar o abastecimento de ${entry.fuelType.displayName} no valor de R$ ${String.format(Locale("pt", "BR"), "%.2f", entry.totalCost)}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFuel(entry)
                        fuelToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { fuelToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Maintenance confirmation
    maintToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { maintToDelete = null },
            title = { Text("Excluir Manutenção?") },
            text = { Text("Deseja realmente apagar o serviço '${entry.serviceType}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMaintenance(entry)
                        maintToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { maintToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Finance confirmation
    financeToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { financeToDelete = null },
            title = { Text("Excluir Registro Financeiro?") },
            text = { Text("Deseja apagar o registro '${entry.category}' de R$ ${String.format(Locale("pt", "BR"), "%.2f", entry.amount)}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFinance(entry)
                        financeToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { financeToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal dialog for wear replacement
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
}

@Composable
fun FuelRecordCard(
    entry: FuelEntry,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = entry.fuelType.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(Date(entry.dateMillis)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f", entry.totalCost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format(Locale("pt", "BR"), "%.2f", entry.liters)} L  •  R$ ${String.format(Locale("pt", "BR"), "%.3f", entry.pricePerLiter)}/L",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.odometerKm > 0) {
                    Text(
                        text = "${entry.odometerKm.toInt()} km",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (entry.stationName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${entry.stationName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MaintenanceRecordCard(
    entry: MaintenanceEntry,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = entry.serviceType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(Date(entry.dateMillis)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f", entry.cost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (entry.workshopName.isNotBlank()) {
                    Text(
                        text = "🔧 ${entry.workshopName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (entry.odometerKm > 0) {
                    Text(
                        text = "${entry.odometerKm.toInt()} km",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun FinanceRecordCard(
    entry: FinanceEntry,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    val isRevenue = entry.type == FinanceType.RECEITA
    val color = if (isRevenue) SleekSuccessText else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Paid,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = entry.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(Date(entry.dateMillis)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (isRevenue) "+" else "-"} R$ ${String.format(Locale("pt", "BR"), "%.2f", entry.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (entry.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String = "Nenhum registro encontrado.") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
