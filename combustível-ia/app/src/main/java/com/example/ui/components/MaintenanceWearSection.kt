package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MaintenanceComponentType
import com.example.model.MaintenanceTrackerItem
import com.example.model.MaintenanceWearStatus
import com.example.model.WearAlertLevel
import com.example.ui.theme.SleekErrorBg
import com.example.ui.theme.SleekErrorText
import com.example.ui.theme.SleekInfoBg
import com.example.ui.theme.SleekInfoText
import com.example.ui.theme.SleekNavyPrimary
import com.example.ui.theme.SleekSuccessBg
import com.example.ui.theme.SleekSuccessText
import com.example.ui.theme.SleekWarningBg
import com.example.ui.theme.SleekWarningText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MaintenanceAlertBanner(
    alerts: List<MaintenanceWearStatus>,
    onItemClick: (MaintenanceWearStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    val criticalCount = alerts.count { it.isCritical }
    val warningCount = alerts.count { it.status == WearAlertLevel.WARNING }
    val halfLifeCount = alerts.count { it.status == WearAlertLevel.HALF_LIFE }

    val isEmergency = criticalCount > 0
    val isWarning = warningCount > 0

    val containerColor = when {
        isEmergency -> SleekErrorBg
        isWarning -> SleekWarningBg
        else -> SleekInfoBg
    }
    val contentColor = when {
        isEmergency -> SleekErrorText
        isWarning -> SleekWarningText
        else -> SleekInfoText
    }
    val borderColor = when {
        isEmergency -> SleekErrorText.copy(alpha = 0.35f)
        isWarning -> SleekWarningText.copy(alpha = 0.35f)
        else -> SleekInfoText.copy(alpha = 0.35f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("maintenance_alert_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = contentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                isEmergency -> Icons.Default.Error
                                isWarning -> Icons.Default.NotificationsActive
                                else -> Icons.Default.Info
                            },
                            contentDescription = "Alerta de Manutenção",
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            isEmergency -> "⚠️ ITENS VENCIDOS PARA TROCA"
                            isWarning -> "⚠️ REVISÃO PREVENTIVA PRÓXIMA"
                            else -> "ℹ️ ALERTA: METADE DO INTERVALO (50%)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = contentColor
                    )
                    Text(
                        text = when {
                            criticalCount > 0 && warningCount > 0 -> "$criticalCount item(ns) vencido(s) e $warningCount próximo(s) da troca"
                            criticalCount > 0 -> "$criticalCount item(ns) ultrapassaram a quilometragem recomendada!"
                            warningCount > 0 && halfLifeCount > 0 -> "$warningCount item(ns) próximo(s) da troca e $halfLifeCount na metade do intervalo"
                            warningCount > 0 -> "$warningCount item(ns) com mais de 85% de desgaste"
                            halfLifeCount > 0 -> "$halfLifeCount item(ns) atingiram a metade do intervalo padrão (50%). Inspeção recomendada."
                            else -> "${alerts.size} item(ns) em alerta preventivo"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = contentColor.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips of items in alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alerts.take(3).forEach { status ->
                    val chipBg = when {
                        status.isCritical -> SleekErrorText
                        status.status == WearAlertLevel.WARNING -> SleekWarningText
                        else -> SleekInfoText
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = chipBg.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, chipBg.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { onItemClick(status) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = status.item.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = chipBg
                            )
                            Text(
                                text = when {
                                    status.remainingKm <= 0 -> "Vencido"
                                    status.status == WearAlertLevel.WARNING -> "Faltam ${status.remainingKm.toInt()} km"
                                    else -> "50% atingido"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = chipBg.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaintenanceWearCard(
    status: MaintenanceWearStatus,
    onRecordChangeClick: (MaintenanceWearStatus) -> Unit,
    onAdjustIntervalClick: (MaintenanceWearStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val item = status.item
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    val statusColor = when (status.status) {
        WearAlertLevel.OK -> SleekSuccessText
        WearAlertLevel.HALF_LIFE -> SleekInfoText
        WearAlertLevel.WARNING -> SleekWarningText
        WearAlertLevel.CRITICAL -> SleekErrorText
    }

    val statusBgColor = when (status.status) {
        WearAlertLevel.OK -> SleekSuccessBg
        WearAlertLevel.HALF_LIFE -> SleekInfoBg
        WearAlertLevel.WARNING -> SleekWarningBg
        WearAlertLevel.CRITICAL -> SleekErrorBg
    }

    val icon: ImageVector = when (item.idKey) {
        MaintenanceComponentType.OLEO_MOTOR.idKey -> Icons.Default.DirectionsCar
        MaintenanceComponentType.FILTRO_OLEO.idKey -> Icons.Default.FilterAlt
        MaintenanceComponentType.FILTRO_COMBUSTIVEL.idKey -> Icons.Default.LocalGasStation
        MaintenanceComponentType.FILTRO_FREIO.idKey -> Icons.Default.Speed
        MaintenanceComponentType.FILTRO_CAMBIO.idKey -> Icons.Default.Settings
        else -> Icons.Default.Build
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (status.depreciationRatio).coerceIn(0f, 1f),
        label = "wearProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wear_item_${item.idKey}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            when (status.status) {
                WearAlertLevel.CRITICAL -> SleekErrorText.copy(alpha = 0.4f)
                WearAlertLevel.WARNING -> SleekWarningText.copy(alpha = 0.4f)
                WearAlertLevel.HALF_LIFE -> SleekInfoText.copy(alpha = 0.4f)
                WearAlertLevel.OK -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon + Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = statusBgColor,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.name,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.category + " • Intervalo ${String.format(Locale("pt", "BR"), "%,d", item.recommendedIntervalKm.toInt())} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBgColor,
                    border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (status.status) {
                                WearAlertLevel.OK -> Icons.Default.CheckCircle
                                WearAlertLevel.HALF_LIFE -> Icons.Default.Info
                                WearAlertLevel.WARNING -> Icons.Default.Warning
                                WearAlertLevel.CRITICAL -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = status.status.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Depreciation Progress Bar & Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Depreciação / Desgaste:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format(Locale("pt", "BR"), "%.1f", status.depreciationPercentage)}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Milestone Indicator showing Half-Interval
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0 km",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "⚡ Metade: ${String.format(Locale("pt", "BR"), "%,d", status.halfIntervalKm.toInt())} km",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = if (status.reachedHalfInterval) FontWeight.Bold else FontWeight.Medium,
                    color = if (status.reachedHalfInterval) SleekInfoText else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "${String.format(Locale("pt", "BR"), "%,d", item.recommendedIntervalKm.toInt())} km",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Alert Callout Banner for Half Interval, Warning or Critical
            if (status.status != WearAlertLevel.OK) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBgColor,
                    border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (status.status) {
                                WearAlertLevel.HALF_LIFE -> Icons.Default.Info
                                WearAlertLevel.WARNING -> Icons.Default.Warning
                                WearAlertLevel.CRITICAL -> Icons.Default.Error
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (status.status) {
                                WearAlertLevel.HALF_LIFE ->
                                    "Metade do intervalo atingida (${String.format(Locale("pt", "BR"), "%,d", status.kmDriven.toInt())} km). Verifique o nível e estado preventivo."
                                WearAlertLevel.WARNING ->
                                    "Atenção: faltam apenas ${String.format(Locale("pt", "BR"), "%,d", status.remainingKm.toInt())} km para a troca recomendada."
                                WearAlertLevel.CRITICAL ->
                                    "Item vencido em ${String.format(Locale("pt", "BR"), "%,d", (-status.remainingKm).toInt())} km! Realize a troca imediatamente."
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mileage Details Table/Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Km Rodados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${String.format(Locale("pt", "BR"), "%,d", status.kmDriven.toInt())} km",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (status.remainingKm >= 0) "Km Restantes" else "Km Excedidos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (status.remainingKm >= 0) {
                            "${String.format(Locale("pt", "BR"), "%,d", status.remainingKm.toInt())} km"
                        } else {
                            "+${String.format(Locale("pt", "BR"), "%,d", (-status.remainingKm).toInt())} km"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Última Troca",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${String.format(Locale("pt", "BR"), "%,d", item.lastReplacedKm.toInt())} km",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: "Registrar Troca" + "Ajustar Intervalo"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAdjustIntervalClick(status) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Intervalo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onRecordChangeClick(status) },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status.isCritical) SleekErrorText else SleekNavyPrimary
                    )
                ) {
                    Icon(Icons.Default.ChangeCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Registrar Troca", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RecordWearChangeDialog(
    status: MaintenanceWearStatus,
    currentOdometerKm: Double,
    onDismiss: () -> Unit,
    onConfirm: (odometerKm: Double, cost: Double?, workshop: String, notes: String, createMaintenanceEntry: Boolean) -> Unit
) {
    var odoText by remember { mutableStateOf(currentOdometerKm.toInt().toString()) }
    var costText by remember { mutableStateOf("") }
    var workshopText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var createMaintenanceEntry by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = SleekNavyPrimary)
                Text("Registrar Troca: ${status.item.name}")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ao registrar a troca, o desgaste deste item será resetado para 0% e a quilometragem de referência será atualizada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = odoText,
                    onValueChange = { odoText = it },
                    label = { Text("Quilometragem da Troca (KM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Custo Total R$ (opcional)") },
                    placeholder = { Text("ex: 180,00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = workshopText,
                    onValueChange = { workshopText = it },
                    label = { Text("Oficina / Local (opcional)") },
                    placeholder = { Text("ex: AutoCenter Central") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observações (marca do filtro, óleo)") },
                    placeholder = { Text("ex: Óleo 5W30 Sintético") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { createMaintenanceEntry = !createMaintenanceEntry }
                ) {
                    Checkbox(
                        checked = createMaintenanceEntry,
                        onCheckedChange = { createMaintenanceEntry = it }
                    )
                    Text(
                        text = "Salvar também no Histórico de Manutenções",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val odo = odoText.toDoubleOrNull() ?: currentOdometerKm
                    val cost = costText.replace(",", ".").toDoubleOrNull()
                    onConfirm(odo, cost, workshopText.trim(), notesText.trim(), createMaintenanceEntry)
                }
            ) {
                Text("Confirmar Troca")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AdjustIntervalDialog(
    status: MaintenanceWearStatus,
    onDismiss: () -> Unit,
    onSave: (newIntervalKm: Double) -> Unit
) {
    var intervalText by remember { mutableStateOf(status.item.recommendedIntervalKm.toInt().toString()) }

    val presets = listOf(5000, 7500, 10000, 12000, 15000, 20000, 30000, 50000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = SleekNavyPrimary)
                Text("Intervalo de Troca")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ajuste a quilometragem recomendada para ${status.item.name} de acordo com o manual do seu veículo ou uso severo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { intervalText = it },
                    label = { Text("Intervalo Recomendado (KM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Atalhos rápidos:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(4).forEach { km ->
                        FilterChip(
                            selected = intervalText == km.toString(),
                            onClick = { intervalText = km.toString() },
                            label = { Text("${km / 1000}k") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.drop(4).forEach { km ->
                        FilterChip(
                            selected = intervalText == km.toString(),
                            onClick = { intervalText = km.toString() },
                            label = { Text("${km / 1000}k") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = intervalText.toDoubleOrNull() ?: status.item.recommendedIntervalKm
                    onSave(km)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
