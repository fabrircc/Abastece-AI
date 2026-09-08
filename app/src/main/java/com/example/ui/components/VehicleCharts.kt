package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConsumptionPoint
import com.example.model.ExpenseCategoryBreakdown
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConsumptionLineChart(
    points: List<ConsumptionPoint>,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Registre ao menos 2 abastecimentos com odômetro para visualizar o gráfico de evolução de km/L.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    val maxKmPerL = (points.maxOfOrNull { it.kmPerLiter } ?: 15.0).coerceAtLeast(10.0)
    val minKmPerL = (points.minOfOrNull { it.kmPerLiter } ?: 5.0).coerceAtMost(maxKmPerL - 2.0).coerceAtLeast(0.0)
    val avgKmPerL = points.map { it.kmPerLiter }.average()

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val avgLineColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Evolução do Consumo (km/L)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Média: ${String.format(Locale("pt", "BR"), "%.2f", avgKmPerL)} km/L",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height
                val padLeft = 40.dp.toPx()
                val padBottom = 24.dp.toPx()
                val padTop = 16.dp.toPx()
                val padRight = 16.dp.toPx()

                val chartW = w - padLeft - padRight
                val chartH = h - padTop - padBottom

                val valRange = (maxKmPerL - minKmPerL).coerceAtLeast(1.0)

                // Grid horizontal lines (3 levels)
                val gridLevels = listOf(minKmPerL, (minKmPerL + maxKmPerL) / 2, maxKmPerL)
                for (lvl in gridLevels) {
                    val y = padTop + chartH - ((lvl - minKmPerL) / valRange * chartH).toFloat()
                    drawLine(
                        color = gridColor,
                        start = Offset(padLeft, y),
                        end = Offset(w - padRight, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Average reference dashed line
                val avgY = padTop + chartH - ((avgKmPerL - minKmPerL) / valRange * chartH).toFloat()
                drawLine(
                    color = avgLineColor,
                    start = Offset(padLeft, avgY),
                    end = Offset(w - padRight, avgY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Map points to canvas coordinates
                val stepX = if (points.size > 1) chartW / (points.size - 1) else chartW
                val coords = points.mapIndexed { idx, pt ->
                    val x = padLeft + idx * stepX
                    val y = padTop + chartH - ((pt.kmPerLiter - minKmPerL) / valRange * chartH).toFloat()
                    Offset(x, y)
                }

                // Draw line path
                val path = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        val p0 = coords[i - 1]
                        val p1 = coords[i]
                        // smooth curve
                        val cX = (p0.x + p1.x) / 2f
                        cubicTo(cX, p0.y, cX, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw dot points with glow
                coords.forEachIndexed { i, offset ->
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.3f),
                        radius = 8.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = offset
                    )
                }
            }

            // X-axis label indicators (first and last dates)
            val dateFormat = SimpleDateFormat("dd/MM", Locale("pt", "BR"))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(Date(points.first().dateMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor
                )
                Text(
                    text = dateFormat.format(Date(points.last().dateMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ExpenseBreakdownBars(
    breakdowns: List<ExpenseCategoryBreakdown>,
    modifier: Modifier = Modifier
) {
    if (breakdowns.isEmpty()) return

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFEC4899),
        Color(0xFF8B5CF6)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Distribuição de Gastos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            ) {
                breakdowns.forEachIndexed { index, item ->
                    val color = colors[index % colors.size]
                    val weight = item.percentage.coerceAtLeast(0.01f)
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(12.dp)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend rows
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                breakdowns.forEachIndexed { index, item ->
                    val color = colors[index % colors.size]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %.2f", item.totalAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${String.format(Locale("pt", "BR"), "%.0f%%", item.percentage * 100)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
