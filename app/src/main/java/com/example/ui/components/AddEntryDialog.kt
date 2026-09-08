package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.FinanceType
import com.example.model.FuelType
import com.example.ui.viewmodel.VehicleViewModel

enum class EntryType(val label: String) {
    FUEL("Abastecimento"),
    MAINTENANCE("Manutenção"),
    FINANCE("Receita / Outros")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryDialog(
    viewModel: VehicleViewModel,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(EntryType.FUEL) }

    // Fuel State
    var odometerText by remember { mutableStateOf("") }
    var litersText by remember { mutableStateOf("") }
    var pricePerLiterText by remember { mutableStateOf("") }
    var totalCostText by remember { mutableStateOf("") }
    var selectedFuelType by remember { mutableStateOf(FuelType.GASOLINA_COMUM) }
    var fuelDropdownExpanded by remember { mutableStateOf(false) }
    var stationText by remember { mutableStateOf("") }
    var isFullTank by remember { mutableStateOf(true) }
    var fuelNotes by remember { mutableStateOf("") }

    // Maintenance State
    var maintOdoText by remember { mutableStateOf("") }
    var serviceTypeText by remember { mutableStateOf("") }
    var workshopText by remember { mutableStateOf("") }
    var maintCostText by remember { mutableStateOf("") }
    var maintNotes by remember { mutableStateOf("") }

    // Finance State
    var isRevenue by remember { mutableStateOf(true) }
    var categoryText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var financeNotes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Novo Registro Manual",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Entry Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EntryType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            shape = RoundedCornerShape(20.dp),
                            label = { Text(type.label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedType) {
                    EntryType.FUEL -> {
                        OutlinedTextField(
                            value = odometerText,
                            onValueChange = { odometerText = it },
                            label = { Text("Odômetro / Km Atual") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Fuel Type Dropdown
                        ExposedDropdownMenuBox(
                            expanded = fuelDropdownExpanded,
                            onExpandedChange = { fuelDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedFuelType.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Combustível") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = fuelDropdownExpanded,
                                onDismissRequest = { fuelDropdownExpanded = false }
                            ) {
                                FuelType.entries.forEach { ft ->
                                    DropdownMenuItem(
                                        text = { Text(ft.displayName) },
                                        onClick = {
                                            selectedFuelType = ft
                                            fuelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = litersText,
                                onValueChange = {
                                    litersText = it
                                    val l = it.replace(",", ".").toDoubleOrNull()
                                    val p = pricePerLiterText.replace(",", ".").toDoubleOrNull()
                                    if (l != null && p != null) {
                                        totalCostText = String.format("%.2f", l * p).replace(".", ",")
                                    }
                                },
                                label = { Text("Litros (L)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = pricePerLiterText,
                                onValueChange = {
                                    pricePerLiterText = it
                                    val p = it.replace(",", ".").toDoubleOrNull()
                                    val l = litersText.replace(",", ".").toDoubleOrNull()
                                    if (p != null && l != null) {
                                        totalCostText = String.format("%.2f", l * p).replace(".", ",")
                                    }
                                },
                                label = { Text("Preço/L (R$)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = totalCostText,
                            onValueChange = {
                                totalCostText = it
                                val t = it.replace(",", ".").toDoubleOrNull()
                                val l = litersText.replace(",", ".").toDoubleOrNull()
                                if (t != null && l != null && l > 0) {
                                    pricePerLiterText = String.format("%.3f", t / l).replace(".", ",")
                                }
                            },
                            label = { Text("Valor Total (R$)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = stationText,
                            onValueChange = { stationText = it },
                            label = { Text("Posto / Local") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tanque Cheio?", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = isFullTank, onCheckedChange = { isFullTank = it })
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = fuelNotes,
                            onValueChange = { fuelNotes = it },
                            label = { Text("Observações (opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    EntryType.MAINTENANCE -> {
                        OutlinedTextField(
                            value = serviceTypeText,
                            onValueChange = { serviceTypeText = it },
                            label = { Text("Serviço (ex: Troca de óleo, Freios)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = maintCostText,
                            onValueChange = { maintCostText = it },
                            label = { Text("Valor Total (R$)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = maintOdoText,
                            onValueChange = { maintOdoText = it },
                            label = { Text("Odômetro / Km Atual") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workshopText,
                            onValueChange = { workshopText = it },
                            label = { Text("Oficina / Estabelecimento") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = maintNotes,
                            onValueChange = { maintNotes = it },
                            label = { Text("Observações") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    EntryType.FINANCE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = isRevenue,
                                onClick = { isRevenue = true },
                                label = { Text("Receita (Ganho)") }
                            )
                            FilterChip(
                                selected = !isRevenue,
                                onClick = { isRevenue = false },
                                label = { Text("Outra Despesa") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = categoryText,
                            onValueChange = { categoryText = it },
                            label = { Text(if (isRevenue) "Categoria (ex: Uber, 99, Frete)" else "Categoria (ex: Pedágio, Lavagem)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Valor (R$)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = financeNotes,
                            onValueChange = { financeNotes = it },
                            label = { Text("Observações") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            when (selectedType) {
                                EntryType.FUEL -> {
                                    val odo = odometerText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    val liters = litersText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    val price = pricePerLiterText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    val total = totalCostText.replace(",", ".").toDoubleOrNull() ?: (liters * price)
                                    viewModel.addFuel(
                                        odometer = odo,
                                        liters = liters,
                                        pricePerLiter = price,
                                        totalCost = total,
                                        fuelType = selectedFuelType,
                                        station = stationText,
                                        isFullTank = isFullTank,
                                        dateMillis = now,
                                        notes = fuelNotes
                                    )
                                }
                                EntryType.MAINTENANCE -> {
                                    val odo = maintOdoText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    val cost = maintCostText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    viewModel.addMaintenance(
                                        odometer = odo,
                                        serviceType = serviceTypeText.ifBlank { "Manutenção" },
                                        workshop = workshopText,
                                        cost = cost,
                                        dateMillis = now,
                                        notes = maintNotes
                                    )
                                }
                                EntryType.FINANCE -> {
                                    val amt = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    val type = if (isRevenue) FinanceType.RECEITA else FinanceType.OUTRO_GASTO
                                    viewModel.addFinance(
                                        type = type,
                                        category = categoryText.ifBlank { if (isRevenue) "Receita" else "Despesa" },
                                        amount = amt,
                                        dateMillis = now,
                                        notes = financeNotes
                                    )
                                }
                            }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Salvar Registro", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
