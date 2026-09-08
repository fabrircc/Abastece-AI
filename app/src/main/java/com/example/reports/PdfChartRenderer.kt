package com.example.reports

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.MaintenanceEntry
import com.example.model.VehicleStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity vector chart renderer for Android Canvas / PdfDocument.
 * Renders Colunas (Column), Pizza (Pie/Donut), Temporal (Time-series),
 * Barras (Horizontal Bar), and Dispersão (Scatter Plot) with executive styling.
 */
object PdfChartRenderer {

    private val titlePaint = Paint().apply {
        color = Color.rgb(15, 23, 42) // Slate 900
        textSize = 10f
        isFakeBoldText = true
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = Color.rgb(71, 85, 105) // Slate 600
        textSize = 7.5f
        isAntiAlias = true
    }

    private val boldLabelPaint = Paint().apply {
        color = Color.rgb(15, 23, 42)
        textSize = 7.5f
        isFakeBoldText = true
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.rgb(226, 232, 240) // Slate 200
        strokeWidth = 0.8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val axisPaint = Paint().apply {
        color = Color.rgb(148, 163, 184) // Slate 400
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val containerBgPaint = Paint().apply {
        color = Color.rgb(248, 250, 252) // Slate 50
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val containerStrokePaint = Paint().apply {
        color = Color.rgb(226, 232, 240)
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Palette
    val colorNavy = Color.rgb(0, 74, 119)        // #004A77
    val colorTeal = Color.rgb(0, 128, 128)       // #008080
    val colorEmerald = Color.rgb(16, 185, 129)   // #10B981
    val colorAmber = Color.rgb(245, 158, 11)     // #F59E0B
    val colorRose = Color.rgb(225, 29, 72)       // #E11D48
    val colorPurple = Color.rgb(147, 51, 234)    // #9333EA

    /**
     * 1. GRÁFICO DE COLUNAS (Vertical Column Chart)
     * Compares expenses by category or monthly grouping.
     */
    fun drawColumnChart(
        canvas: Canvas,
        rect: RectF,
        fuels: List<FuelEntry>,
        maintenances: List<MaintenanceEntry>,
        finances: List<FinanceEntry>
    ) {
        // Draw container
        canvas.drawRoundRect(rect, 6f, 6f, containerBgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, containerStrokePaint)

        canvas.drawText("GRÁFICO DE COLUNAS: Comparativo de Gastos", rect.left + 10f, rect.top + 14f, titlePaint)

        val chartLeft = rect.left + 32f
        val chartTop = rect.top + 28f
        val chartRight = rect.right - 14f
        val chartBottom = rect.bottom - 22f

        val totalFuel = fuels.sumOf { it.totalCost }
        val totalMaint = maintenances.sumOf { it.cost }
        val totalOther = finances.filter { it.type == FinanceType.OUTRO_GASTO }.sumOf { it.amount }

        val items = listOf(
            Triple("Combustível", totalFuel, colorNavy),
            Triple("Manutenção", totalMaint, colorAmber),
            Triple("Outros/Taxas", totalOther, colorRose)
        )

        val maxVal = (items.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(100.0)

        // Draw horizontal grid lines
        for (i in 0..4) {
            val y = chartBottom - (chartBottom - chartTop) * (i / 4f)
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
            val valLabel = String.format(Locale("pt", "BR"), "%.0f", maxVal * (i / 4f))
            canvas.drawText(valLabel, rect.left + 6f, y + 3f, labelPaint)
        }
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)

        // Draw columns
        val availableWidth = chartRight - chartLeft
        val slotWidth = availableWidth / items.size
        val colWidth = (slotWidth * 0.48f).coerceAtMost(36f)

        val fillPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        for ((idx, item) in items.withIndex()) {
            val (label, value, colColor) = item
            val colX = chartLeft + (idx * slotWidth) + (slotWidth - colWidth) / 2f
            val barHeight = ((value / maxVal) * (chartBottom - chartTop)).toFloat()
            val colY = chartBottom - barHeight

            fillPaint.color = colColor
            val colRect = RectF(colX, colY, colX + colWidth, chartBottom)
            canvas.drawRoundRect(colRect, 3f, 3f, fillPaint)

            // Value on top
            val formattedVal = String.format(Locale("pt", "BR"), "R$%.0f", value)
            canvas.drawText(formattedVal, colX - 2f, colY - 3f, boldLabelPaint)

            // Category label below
            canvas.drawText(label, colX - 4f, chartBottom + 12f, labelPaint)
        }
    }

    /**
     * 2. GRÁFICO DE PIZZA / DONUT (Pie / Donut Chart)
     * Shows percentage breakdown of expenses.
     */
    fun drawPieChart(
        canvas: Canvas,
        rect: RectF,
        stats: VehicleStats,
        finances: List<FinanceEntry>
    ) {
        // Draw container
        canvas.drawRoundRect(rect, 6f, 6f, containerBgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, containerStrokePaint)

        canvas.drawText("GRÁFICO DE PIZZA: Distribuição Percentual de Custos", rect.left + 10f, rect.top + 14f, titlePaint)

        val totalFuel = stats.totalFuelCost
        val totalMaint = stats.totalMaintenanceCost
        val totalOther = stats.totalOtherExpenses
        val totalExpenses = (totalFuel + totalMaint + totalOther).coerceAtLeast(1.0)

        val slices = listOf(
            Triple("Combustível", totalFuel, colorNavy),
            Triple("Manutenção", totalMaint, colorAmber),
            Triple("Outros Gastos", totalOther, colorEmerald)
        ).filter { it.second > 0.0 }

        val cx = rect.left + (rect.width() * 0.32f)
        val cy = rect.top + (rect.height() * 0.56f)
        val radius = (rect.height() * 0.34f).coerceAtMost(50f)
        val pieRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        val slicePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        if (slices.isEmpty()) {
            slicePaint.color = Color.rgb(203, 213, 225)
            canvas.drawCircle(cx, cy, radius, slicePaint)
            canvas.drawText("Sem dados no período", cx - 35f, cy, labelPaint)
        } else {
            var currentAngle = -90f
            for (slice in slices) {
                val sweepAngle = ((slice.second / totalExpenses) * 360.0).toFloat()
                slicePaint.color = slice.third
                canvas.drawArc(pieRect, currentAngle, sweepAngle, true, slicePaint)

                // Optional percentage label along arc if angle > 25 deg
                if (sweepAngle > 25f) {
                    val midAngleRad = Math.toRadians((currentAngle + sweepAngle / 2f).toDouble())
                    val textDist = radius * 0.65f
                    val lx = cx + (textDist * cos(midAngleRad)).toFloat()
                    val ly = cy + (textDist * sin(midAngleRad)).toFloat()

                    val pctPaint = Paint().apply {
                        color = Color.WHITE
                        textSize = 7.5f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val pct = (slice.second / totalExpenses * 100).toInt()
                    canvas.drawText("$pct%", lx - 6f, ly + 3f, pctPaint)
                }

                currentAngle += sweepAngle
            }

            // Donut hole in center for modern aesthetic
            val holePaint = Paint().apply {
                color = Color.rgb(248, 250, 252)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(cx, cy, radius * 0.42f, holePaint)
        }

        // Legend on right side
        val legendLeft = rect.left + (rect.width() * 0.60f)
        var legendY = rect.top + 34f

        for (slice in (if (slices.isEmpty()) listOf(Triple("Sem dados", 0.0, Color.GRAY)) else slices)) {
            val legendBox = RectF(legendLeft, legendY - 6f, legendLeft + 9f, legendY + 3f)
            slicePaint.color = slice.third
            canvas.drawRoundRect(legendBox, 2f, 2f, slicePaint)

            canvas.drawText(slice.first, legendLeft + 14f, legendY + 1f, boldLabelPaint)
            val pct = (slice.second / totalExpenses * 100)
            val subLabel = String.format(Locale("pt", "BR"), "R$ %.2f (%.1f%%)", slice.second, pct)
            canvas.drawText(subLabel, legendLeft + 14f, legendY + 10f, labelPaint)

            legendY += 22f
        }
    }

    /**
     * 3. GRÁFICO TEMPORAL (Time Series Line Chart)
     * Chronological evolution of fuel consumption (km/L) and price per liter over time.
     */
    fun drawTemporalChart(
        canvas: Canvas,
        rect: RectF,
        fuels: List<FuelEntry>
    ) {
        // Draw container
        canvas.drawRoundRect(rect, 6f, 6f, containerBgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, containerStrokePaint)

        canvas.drawText("GRÁFICO TEMPORAL: Histórico de Rendimento (km/L)", rect.left + 10f, rect.top + 14f, titlePaint)

        val chartLeft = rect.left + 30f
        val chartTop = rect.top + 28f
        val chartRight = rect.right - 14f
        val chartBottom = rect.bottom - 20f

        val sorted = fuels.sortedBy { it.dateMillis }
        val dateFmt = SimpleDateFormat("dd/MM", Locale("pt", "BR"))

        // Compute km/L points
        val points = mutableListOf<Pair<Long, Double>>()
        for (i in 1 until sorted.size) {
            val dist = sorted[i].odometerKm - sorted[i - 1].odometerKm
            val liters = sorted[i].liters
            if (dist > 0 && liters > 0) {
                val kml = (dist / liters).coerceIn(3.0, 35.0)
                points.add(Pair(sorted[i].dateMillis, kml))
            }
        }

        // If not enough points for km/L, plot pricePerLiter
        val usePrice = points.size < 2
        val activePoints = if (usePrice) {
            sorted.map { Pair(it.dateMillis, it.pricePerLiter.coerceIn(1.0, 15.0)) }
        } else points

        val yAxisTitle = if (usePrice) "Preço R$/L" else "km/L"
        canvas.drawText("($yAxisTitle)", rect.left + 10f, chartTop - 3f, labelPaint)

        val minVal = (activePoints.minOfOrNull { it.second } ?: 5.0) * 0.85
        val maxVal = ((activePoints.maxOfOrNull { it.second } ?: 15.0) * 1.15).coerceAtLeast(minVal + 1.0)

        // Draw horizontal grid lines
        for (i in 0..3) {
            val y = chartBottom - (chartBottom - chartTop) * (i / 3f)
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
            val valLabel = String.format(Locale("pt", "BR"), "%.1f", minVal + (maxVal - minVal) * (i / 3f))
            canvas.drawText(valLabel, rect.left + 6f, y + 3f, labelPaint)
        }
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)

        if (activePoints.size < 2) {
            canvas.drawText("Requer no mínimo 2 abastecimentos cadastrados para traçar curva.", chartLeft + 15f, (chartTop + chartBottom) / 2f, labelPaint)
            return
        }

        val stepX = (chartRight - chartLeft) / (activePoints.size - 1)
        val path = Path()
        val areaPath = Path()

        val linePaint = Paint().apply {
            color = colorTeal
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val dotPaint = Paint().apply {
            color = colorNavy
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val areaPaint = Paint().apply {
            color = Color.argb(35, 0, 128, 128)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        var startX = chartLeft
        var startY = chartBottom

        for ((idx, pt) in activePoints.withIndex()) {
            val px = chartLeft + idx * stepX
            val ratio = ((pt.second - minVal) / (maxVal - minVal)).toFloat().coerceIn(0f, 1f)
            val py = chartBottom - ratio * (chartBottom - chartTop)

            if (idx == 0) {
                path.moveTo(px, py)
                areaPath.moveTo(px, chartBottom)
                areaPath.lineTo(px, py)
                startX = px
                startY = py
            } else {
                path.lineTo(px, py)
                areaPath.lineTo(px, py)
            }

            // Draw point circle
            canvas.drawCircle(px, py, 2.5f, dotPaint)

            // Date label below
            if (activePoints.size <= 8 || idx % (activePoints.size / 5 + 1) == 0) {
                canvas.drawText(dateFmt.format(Date(pt.first)), px - 10f, chartBottom + 11f, labelPaint)
            }
        }

        areaPath.lineTo(chartLeft + (activePoints.size - 1) * stepX, chartBottom)
        areaPath.close()

        canvas.drawPath(areaPath, areaPaint)
        canvas.drawPath(path, linePaint)
    }

    /**
     * 4. GRÁFICO DE BARRAS (Horizontal Bar Chart)
     * Compares spending by Fuel Type.
     */
    fun drawBarChart(
        canvas: Canvas,
        rect: RectF,
        fuels: List<FuelEntry>
    ) {
        // Draw container
        canvas.drawRoundRect(rect, 6f, 6f, containerBgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, containerStrokePaint)

        canvas.drawText("GRÁFICO DE BARRAS: Custos por Tipo de Combustível", rect.left + 10f, rect.top + 14f, titlePaint)

        val chartLeft = rect.left + 80f
        val chartTop = rect.top + 28f
        val chartRight = rect.right - 45f
        val chartBottom = rect.bottom - 12f

        val grouped = fuels.groupBy { it.fuelType }
            .map { Pair(it.key.displayName, it.value.sumOf { f -> f.totalCost }) }
            .sortedByDescending { it.second }

        val maxVal = (grouped.maxOfOrNull { it.second } ?: 100.0).coerceAtLeast(10.0)

        val barPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val colors = listOf(colorNavy, colorEmerald, colorAmber, colorPurple, colorRose)
        val availableHeight = chartBottom - chartTop
        val rowHeight = (availableHeight / (grouped.size.coerceAtLeast(1))).coerceAtMost(22f)

        for ((idx, item) in grouped.withIndex()) {
            val (name, total) = item
            val y = chartTop + idx * rowHeight
            val barW = ((total / maxVal) * (chartRight - chartLeft)).toFloat()

            // Fuel name label
            val truncName = if (name.length > 14) name.take(12) + ".." else name
            canvas.drawText(truncName, rect.left + 8f, y + 11f, boldLabelPaint)

            // Bar
            barPaint.color = colors[idx % colors.size]
            val barRect = RectF(chartLeft, y + 3f, chartLeft + barW, y + rowHeight - 3f)
            canvas.drawRoundRect(barRect, 3f, 3f, barPaint)

            // Value at end
            val valText = String.format(Locale("pt", "BR"), "R$ %.0f", total)
            canvas.drawText(valText, chartLeft + barW + 4f, y + 11f, labelPaint)
        }
    }

    /**
     * 5. GRÁFICO DE DISPERSÃO (Scatter Plot)
     * Correlates Liters vs Total Cost with Regression Line.
     */
    fun drawScatterChart(
        canvas: Canvas,
        rect: RectF,
        fuels: List<FuelEntry>
    ) {
        // Draw container
        canvas.drawRoundRect(rect, 6f, 6f, containerBgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, containerStrokePaint)

        canvas.drawText("GRÁFICO DE DISPERSÃO: Litros vs. Custo Total (R$)", rect.left + 10f, rect.top + 14f, titlePaint)

        val chartLeft = rect.left + 32f
        val chartTop = rect.top + 28f
        val chartRight = rect.right - 14f
        val chartBottom = rect.bottom - 22f

        val maxLiters = (fuels.maxOfOrNull { it.liters } ?: 50.0).coerceAtLeast(20.0) * 1.15
        val maxCost = (fuels.maxOfOrNull { it.totalCost } ?: 250.0).coerceAtLeast(100.0) * 1.15

        // Gridlines
        for (i in 0..3) {
            val y = chartBottom - (chartBottom - chartTop) * (i / 3f)
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
            canvas.drawText(String.format(Locale("pt", "BR"), "%.0f", maxCost * (i / 3f)), rect.left + 6f, y + 3f, labelPaint)

            val x = chartLeft + (chartRight - chartLeft) * (i / 3f)
            canvas.drawLine(x, chartTop, x, chartBottom, gridPaint)
            canvas.drawText(String.format(Locale("pt", "BR"), "%.0fL", maxLiters * (i / 3f)), x - 6f, chartBottom + 12f, labelPaint)
        }
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint)

        val dotPaint = Paint().apply {
            color = colorRose
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val dotStrokePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }

        // Plot scatter points
        for (f in fuels) {
            val px = chartLeft + ((f.liters / maxLiters) * (chartRight - chartLeft)).toFloat()
            val py = chartBottom - ((f.totalCost / maxCost) * (chartBottom - chartTop)).toFloat()

            canvas.drawCircle(px, py, 3.5f, dotPaint)
            canvas.drawCircle(px, py, 3.5f, dotStrokePaint)
        }

        // Trend line (Linha de Regressão / Tendência Média)
        if (fuels.size >= 2) {
            val avgPrice = if (fuels.sumOf { it.liters } > 0) fuels.sumOf { it.totalCost } / fuels.sumOf { it.liters } else 5.8
            val trendStartX = chartLeft
            val trendStartY = chartBottom
            val trendEndX = chartRight
            val trendEndCost = (maxLiters * avgPrice).coerceAtMost(maxCost)
            val trendEndY = chartBottom - ((trendEndCost / maxCost) * (chartBottom - chartTop)).toFloat()

            val trendPaint = Paint().apply {
                color = colorNavy
                strokeWidth = 1.2f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawLine(trendStartX, trendStartY, trendEndX, trendEndY, trendPaint)
            canvas.drawText("Tendência Média: R$ ${String.format(Locale("pt", "BR"), "%.2f/L", avgPrice)}", chartRight - 90f, trendEndY - 4f, boldLabelPaint)
        }
    }
}
