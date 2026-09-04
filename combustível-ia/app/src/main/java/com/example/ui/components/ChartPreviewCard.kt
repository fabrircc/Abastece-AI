package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ScatterPlot
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChartExportType
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.MaintenanceEntry
import com.example.model.VehicleStats
import com.example.ui.theme.SleekNavyPrimary
import com.example.ui.theme.SleekSuccessText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChartPreviewCard(
    chartType: ChartExportType,
    stats: VehicleStats,
    fuels: List<FuelEntry>,
    maintenances: List<MaintenanceEntry>,
    finances: List<FinanceEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chart_preview_card_${chartType.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val icon = when (chartType) {
                                ChartExportType.COLUNAS -> Icons.Default.BarChart
                                ChartExportType.PIZZA -> Icons.Default.PieChart
                                ChartExportType.TEMPORAL -> Icons.Default.ShowChart
                                ChartExportType.BARRAS -> Icons.Default.BarChart
                                ChartExportType.DISPERSAO -> Icons.Default.ScatterPlot
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Prévia: Gráfico de ${chartType.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = chartType.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "PDF & XLS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas Body
            when (chartType) {
                ChartExportType.COLUNAS -> ColumnChartPreview(fuels, maintenances, finances)
                ChartExportType.PIZZA -> PieChartPreview(stats, finances)
                ChartExportType.TEMPORAL -> TemporalChartPreview(fuels)
                ChartExportType.BARRAS -> BarChartPreview(fuels)
                ChartExportType.DISPERSAO -> ScatterChartPreview(fuels)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = chartType.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
private fun ColumnChartPreview(
    fuels: List<FuelEntry>,
    maintenances: List<MaintenanceEntry>,
    finances: List<FinanceEntry>
) {
    val totalFuel = fuels.sumOf { it.totalCost }
    val totalMaint = maintenances.sumOf { it.cost }
    val totalOther = finances.filter { it.type == FinanceType.OUTRO_GASTO }.sumOf { it.amount }

    val items = listOf(
        Triple("Combustível", totalFuel, Color(0xFF004A77)),
        Triple("Manutenção", totalMaint, Color(0xFFF59E0B)),
        Triple("Outros/Taxas", totalOther, Color(0xFFE11D48))
    )

    val maxVal = (items.maxOfOrNull { it.second } ?: 100.0).coerceAtLeast(50.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                val gridColor = Color(0xFFE2E8F0)
                val lines = 3
                for (i in 0..lines) {
                    val y = size.height * (i / lines.toFloat())
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEach { item ->
                    val ratio = (item.second / maxVal).toFloat().coerceIn(0.08f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$%.0f", item.second),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = item.third,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height((100 * ratio).dp)
                                .background(item.third, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                Text(
                    text = item.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF475569),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PieChartPreview(
    stats: VehicleStats,
    finances: List<FinanceEntry>
) {
    val totalFuel = stats.totalFuelCost
    val totalMaint = stats.totalMaintenanceCost
    val totalOther = stats.totalOtherExpenses
    val totalExp = (totalFuel + totalMaint + totalOther).coerceAtLeast(1.0)

    val slices = listOf(
        Triple("Combustível", totalFuel, Color(0xFF004A77)),
        Triple("Manutenção", totalMaint, Color(0xFFF59E0B)),
        Triple("Outros", totalOther, Color(0xFF10B981))
    ).filter { it.second > 0 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(110.dp)) {
                if (slices.isEmpty()) {
                    drawCircle(color = Color(0xFFCBD5E1))
                } else {
                    var startAngle = -90f
                    for (slice in slices) {
                        val sweep = ((slice.second / totalExp) * 360f).toFloat()
                        drawArc(
                            color = slice.third,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 32f)
                        )
                        startAngle += sweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Text(
                    String.format(Locale("pt", "BR"), "R$%.0f", stats.totalExpenses),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val displayList = if (slices.isEmpty()) listOf(Triple("Sem despesas", 0.0, Color.Gray)) else slices
            for (slice in displayList) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(slice.third, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(slice.first, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        val pct = if (totalExp > 0) (slice.second / totalExp * 100) else 0.0
                        Text(
                            String.format(Locale("pt", "BR"), "R$ %.2f (%.0f%%)", slice.second, pct),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemporalChartPreview(fuels: List<FuelEntry>) {
    val sorted = remember(fuels) { fuels.sortedBy { it.dateMillis } }
    val points = remember(sorted) {
        val pts = mutableListOf<Double>()
        for (i in 1 until sorted.size) {
            val dist = sorted[i].odometerKm - sorted[i - 1].odometerKm
            val lit = sorted[i].liters
            if (dist > 0 && lit > 0) {
                pts.add((dist / lit).coerceIn(3.0, 25.0))
            }
        }
        if (pts.isEmpty()) sorted.map { it.pricePerLiter } else pts
    }

    val minVal = (points.minOfOrNull { it } ?: 8.0) * 0.9
    val maxVal = ((points.maxOfOrNull { it } ?: 14.0) * 1.1).coerceAtLeast(minVal + 1.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Evolução Histórica", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF004A77))
            Text(
                if (points.size >= 2) "Rendimento (km/L)" else "Preço por Litro (R$/L)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color(0xFFE2E8F0)
                drawLine(gridColor, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1f)

                if (points.size < 2) {
                    return@Canvas
                }

                val stepX = size.width / (points.size - 1)
                val linePath = Path()
                val areaPath = Path()

                points.forEachIndexed { idx, valY ->
                    val x = idx * stepX
                    val normY = ((valY - minVal) / (maxVal - minVal)).toFloat().coerceIn(0f, 1f)
                    val y = size.height - (normY * size.height)

                    if (idx == 0) {
                        linePath.moveTo(x, y)
                        areaPath.moveTo(x, size.height)
                        areaPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        areaPath.lineTo(x, y)
                    }
                    drawCircle(color = Color(0xFF004A77), radius = 4f, center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 2f, center = Offset(x, y))
                }

                areaPath.lineTo(size.width, size.height)
                areaPath.close()

                drawPath(
                    areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF008080).copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                drawPath(
                    linePath,
                    color = Color(0xFF008080),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Início", fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text("Recente", fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun BarChartPreview(fuels: List<FuelEntry>) {
    val grouped = remember(fuels) {
        fuels.groupBy { it.fuelType }
            .map { Pair(it.key.displayName, it.value.sumOf { f -> f.totalCost }) }
            .sortedByDescending { it.second }
    }

    val maxVal = (grouped.maxOfOrNull { it.second } ?: 100.0).coerceAtLeast(10.0)
    val colors = listOf(Color(0xFF004A77), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF8B5CF6))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.take(4).forEachIndexed { idx, item ->
            val ratio = (item.second / maxVal).toFloat().coerceIn(0.05f, 1f)
            val barColor = colors[idx % colors.size]

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.first, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    Text(
                        String.format(Locale("pt", "BR"), "R$ %.2f", item.second),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(10.dp)
                            .background(barColor, RoundedCornerShape(5.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun ScatterChartPreview(fuels: List<FuelEntry>) {
    val maxLiters = (fuels.maxOfOrNull { it.liters } ?: 50.0).coerceAtLeast(20.0) * 1.15
    val maxCost = (fuels.maxOfOrNull { it.totalCost } ?: 250.0).coerceAtLeast(100.0) * 1.15

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Litros (Eixo X) vs. Custo R$ (Eixo Y)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004A77))
            Text("Tendência", fontSize = 10.sp, color = Color(0xFFE11D48), fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val grid = Color(0xFFE2E8F0)
                // Grid
                for (i in 0..3) {
                    val y = size.height * (i / 3f)
                    drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    val x = size.width * (i / 3f)
                    drawLine(grid, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                }

                // Scatter Points
                for (f in fuels) {
                    val px = ((f.liters / maxLiters) * size.width).toFloat().coerceIn(4f, size.width - 4f)
                    val py = (size.height - ((f.totalCost / maxCost) * size.height)).toFloat().coerceIn(4f, size.height - 4f)

                    drawCircle(color = Color(0xFFE11D48), radius = 6f, center = Offset(px, py))
                    drawCircle(color = Color.White, radius = 2.5f, center = Offset(px, py))
                }

                // Regression / Trend line
                if (fuels.size >= 2) {
                    drawLine(
                        color = Color(0xFF004A77),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height * 0.15f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0 Litros", fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(String.format(Locale("pt", "BR"), "%.0f Litros", maxLiters), fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}
