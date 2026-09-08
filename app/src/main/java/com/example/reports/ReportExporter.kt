package com.example.reports

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.ChartExportType
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.MaintenanceEntry
import com.example.model.MaintenanceWearStatus
import com.example.model.VehicleStats
import com.example.model.WearAlertLevel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("pt", "BR"))

    /**
     * Exports full Microsoft Excel Spreadsheet (.xls).
     * Features multiple worksheets, styled XML, formulas, and structured chart series.
     */
    fun exportXls(
        context: Context,
        periodLabel: String,
        stats: VehicleStats,
        fuels: List<FuelEntry>,
        maintenances: List<MaintenanceEntry>,
        finances: List<FinanceEntry>,
        selectedCharts: Set<ChartExportType> = ChartExportType.entries.toSet(),
        wearStatuses: List<MaintenanceWearStatus> = emptyList()
    ): File {
        val fileName = "Relatorio_Veicular_${fileDateFormat.format(Date())}.xls"
        val file = File(context.cacheDir, fileName)

        val xmlContent = ExcelXmlExporter.generateXls(
            periodLabel = periodLabel,
            stats = stats,
            fuels = fuels,
            maintenances = maintenances,
            finances = finances,
            selectedCharts = selectedCharts,
            wearStatuses = wearStatuses
        )

        file.writeText(xmlContent, Charsets.UTF_8)
        return file
    }

    /**
     * Exports simple CSV format with semicolon delimiter and UTF-8 BOM.
     */
    fun exportCsv(
        context: Context,
        periodLabel: String,
        stats: VehicleStats,
        fuels: List<FuelEntry>,
        maintenances: List<MaintenanceEntry>,
        finances: List<FinanceEntry>,
        wearStatuses: List<MaintenanceWearStatus> = emptyList()
    ): File {
        val fileName = "Relatorio_Veicular_${fileDateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("\uFEFF") // UTF-8 BOM

            writer.write("RELATÓRIO DE CONTROLE VEICULAR & COMBUSTÍVEL\n")
            writer.write("Período;$periodLabel;Gerado em;${dateFormat.format(Date())}\n\n")

            if (wearStatuses.isNotEmpty()) {
                writer.write("CONTROLE DE DESGASTE DE FILTROS & ÓLEO DO MOTOR\n")
                writer.write("Item;Última Troca (km);Intervalo Recomendado (km);Km Restantes;Desgaste (%);Situação / Alerta\n")
                for (w in wearStatuses) {
                    val statusLabel = when (w.status) {
                        WearAlertLevel.CRITICAL -> "CRÍTICO (VENCIDO)"
                        WearAlertLevel.WARNING -> "ATENÇÃO (PRÓXIMO)"
                        WearAlertLevel.HALF_LIFE -> "METADE DO INTERVALO (50%)"
                        WearAlertLevel.OK -> "EM DIA"
                    }
                    writer.write(
                        "\"${w.item.name}\";" +
                                "${w.item.lastReplacedKm.toInt()};" +
                                "${w.item.recommendedIntervalKm.toInt()};" +
                                "${w.remainingKm.toInt()};" +
                                "${String.format(Locale("pt", "BR"), "%.1f", w.depreciationPercentage)}%;" +
                                "\"$statusLabel\"\n"
                    )
                }
                writer.write("\n")
            }

            writer.write("RESUMO GERAL DAS MÉTRICAS\n")
            writer.write("Métrica;Valor\n")
            writer.write("Distância Total Percorrida;${String.format(Locale("pt", "BR"), "%.1f km", stats.totalDistanceKm)}\n")
            writer.write("Total de Combustível Abastecido;${String.format(Locale("pt", "BR"), "%.2f Litros", stats.totalLiters)}\n")
            writer.write("Média de Consumo;${String.format(Locale("pt", "BR"), "%.2f km/L", stats.avgKmPerLiter)}\n")
            writer.write("Custo Médio por Km;${String.format(Locale("pt", "BR"), "R$ %.3f / km", stats.avgCostPerKm)}\n")
            writer.write("Preço Médio por Litro;${String.format(Locale("pt", "BR"), "R$ %.3f", stats.avgPricePerLiter)}\n")
            writer.write("Gasto Total em Combustível;${String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalFuelCost)}\n")
            writer.write("Gasto Total em Manutenção;${String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalMaintenanceCost)}\n")
            writer.write("Outras Despesas do Veículo;${String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalOtherExpenses)}\n")
            writer.write("Total de Receitas (Corridas/Fretes);${String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalRevenues)}\n")
            writer.write("Saldo Líquido Operacional;${String.format(Locale("pt", "BR"), "R$ %.2f", stats.netBalance)}\n\n")

            writer.write("HISTÓRICO DE ABASTECIMENTOS\n")
            writer.write("Data;Odômetro (km);Combustível;Litros;Preço/L (R$);Total (R$);Posto/Local;Tanque Cheio;Observações\n")
            for (f in fuels) {
                writer.write(
                    "${dateFormat.format(Date(f.dateMillis))};" +
                            "${f.odometerKm.toInt()};" +
                            "${f.fuelType.displayName};" +
                            "${String.format(Locale("pt", "BR"), "%.2f", f.liters)};" +
                            "${String.format(Locale("pt", "BR"), "%.3f", f.pricePerLiter)};" +
                            "${String.format(Locale("pt", "BR"), "%.2f", f.totalCost)};" +
                            "\"${f.stationName.replace("\"", "\"\"")}\";" +
                            "${if (f.isFullTank) "Sim" else "Não"};" +
                            "\"${f.notes.replace("\"", "\"\"")}\"\n"
                )
            }
            writer.write("\n")

            writer.write("HISTÓRICO DE MANUTENÇÕES\n")
            writer.write("Data;Odômetro (km);Serviço;Oficina/Local;Custo (R$);Observações\n")
            for (m in maintenances) {
                writer.write(
                    "${dateFormat.format(Date(m.dateMillis))};" +
                            "${m.odometerKm.toInt()};" +
                            "\"${m.serviceType.replace("\"", "\"\"")}\";" +
                            "\"${m.workshopName.replace("\"", "\"\"")}\";" +
                            "${String.format(Locale("pt", "BR"), "%.2f", m.cost)};" +
                            "\"${m.notes.replace("\"", "\"\"")}\"\n"
                )
            }
            writer.write("\n")

            writer.write("HISTÓRICO DE RECEITAS E OUTROS GASTOS\n")
            writer.write("Data;Tipo;Categoria;Valor (R$);Observações\n")
            for (fn in finances) {
                writer.write(
                    "${dateFormat.format(Date(fn.dateMillis))};" +
                            "${if (fn.type == FinanceType.RECEITA) "Receita" else "Despesa"};" +
                            "\"${fn.category.replace("\"", "\"\"")}\";" +
                            "${String.format(Locale("pt", "BR"), "%.2f", fn.amount)};" +
                            "\"${fn.notes.replace("\"", "\"\"")}\"\n"
                )
            }
        }

        return file
    }

    /**
     * Exports multi-page executive PDF report with selected vector charts rendered directly on Canvas.
     */
    fun exportPdf(
        context: Context,
        periodLabel: String,
        stats: VehicleStats,
        fuels: List<FuelEntry>,
        maintenances: List<MaintenanceEntry>,
        finances: List<FinanceEntry>,
        selectedCharts: Set<ChartExportType> = ChartExportType.entries.toSet(),
        wearStatuses: List<MaintenanceWearStatus> = emptyList()
    ): File {
        val fileName = "Relatorio_Veicular_${fileDateFormat.format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)

        val doc = PdfDocument()
        val pageWidth = 595 // A4 width
        val pageHeight = 842 // A4 height
        val margin = 36f
        val contentWidth = pageWidth - (margin * 2)

        val titlePaint = Paint().apply {
            color = Color.rgb(0, 74, 119) // Navy
            textSize = 17f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9.5f
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.rgb(15, 76, 129)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val cardPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardStrokePaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val boldText = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val normalText = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 8.5f
            isAntiAlias = true
        }

        val smallText = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        val totalPages = if (selectedCharts.size > 2) 3 else 2

        // ==========================================
        // PAGE 1: Visão Executiva, KPIs e Gráficos Iniciais
        // ==========================================
        val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = doc.startPage(page1Info)
        val canvas1: Canvas = page1.canvas

        var y1 = 36f

        // Top Accent Bar
        val accentPaint = Paint().apply { color = Color.rgb(0, 74, 119) }
        canvas1.drawRect(margin, y1, margin + contentWidth, y1 + 4, accentPaint)
        y1 += 18f

        // Header Title
        canvas1.drawText("COMBUSTÍVEL IA - RELATÓRIO VEICULAR", margin, y1, titlePaint)
        y1 += 14f
        canvas1.drawText("Período Analisado: $periodLabel  |  Emitido em: ${dateFormat.format(Date())}", margin, y1, subtitlePaint)
        y1 += 20f

        // 6 KPI Summary Cards (2 rows x 3 cols)
        val cardMargin = 8f
        val cardWidth = (contentWidth - (cardMargin * 2)) / 3
        val cardHeight = 44f

        val kpiData = listOf(
            Pair("Média Consumo", String.format(Locale("pt", "BR"), "%.2f km/L", stats.avgKmPerLiter)),
            Pair("Custo por Km", String.format(Locale("pt", "BR"), "R$ %.3f", stats.avgCostPerKm)),
            Pair("Distância Total", String.format(Locale("pt", "BR"), "%.1f km", stats.totalDistanceKm)),
            Pair("Gasto Combustível", String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalFuelCost)),
            Pair("Gasto Manutenção", String.format(Locale("pt", "BR"), "R$ %.2f", stats.totalMaintenanceCost)),
            Pair("Saldo Líquido", String.format(Locale("pt", "BR"), "R$ %.2f", stats.netBalance))
        )

        for (idx in kpiData.indices) {
            val row = idx / 3
            val col = idx % 3
            val cardX = margin + col * (cardWidth + cardMargin)
            val cardY = y1 + row * (cardHeight + cardMargin)

            val rect = RectF(cardX, cardY, cardX + cardWidth, cardY + cardHeight)
            canvas1.drawRoundRect(rect, 6f, 6f, cardPaint)
            canvas1.drawRoundRect(rect, 6f, 6f, cardStrokePaint)

            val (kpiTitle, kpiVal) = kpiData[idx]
            canvas1.drawText(kpiTitle, cardX + 8f, cardY + 15f, smallText)
            val valPaint = if (idx == 5 && stats.netBalance < 0) {
                Paint(boldText).apply { color = Color.rgb(220, 38, 38) }
            } else if (idx == 5 && stats.netBalance > 0) {
                Paint(boldText).apply { color = Color.rgb(16, 185, 129) }
            } else boldText
            canvas1.drawText(kpiVal, cardX + 8f, cardY + 32f, valPaint)
        }

        y1 += (cardHeight * 2) + cardMargin + 20f

        // Charts Section 1 on Page 1
        canvas1.drawText("Análise Visual & Indicadores", margin, y1, sectionPaint)
        y1 += 12f

        val hasColumns = selectedCharts.contains(ChartExportType.COLUNAS)
        val hasPie = selectedCharts.contains(ChartExportType.PIZZA)

        if (hasColumns && hasPie) {
            val chartW = (contentWidth - 10f) / 2f
            val chartH = 180f
            PdfChartRenderer.drawColumnChart(canvas1, RectF(margin, y1, margin + chartW, y1 + chartH), fuels, maintenances, finances)
            PdfChartRenderer.drawPieChart(canvas1, RectF(margin + chartW + 10f, y1, margin + contentWidth, y1 + chartH), stats, finances)
            y1 += chartH + 16f
        } else if (hasColumns) {
            val chartH = 180f
            PdfChartRenderer.drawColumnChart(canvas1, RectF(margin, y1, margin + contentWidth, y1 + chartH), fuels, maintenances, finances)
            y1 += chartH + 16f
        } else if (hasPie) {
            val chartH = 180f
            PdfChartRenderer.drawPieChart(canvas1, RectF(margin, y1, margin + contentWidth, y1 + chartH), stats, finances)
            y1 += chartH + 16f
        } else if (selectedCharts.contains(ChartExportType.TEMPORAL)) {
            val chartH = 180f
            PdfChartRenderer.drawTemporalChart(canvas1, RectF(margin, y1, margin + contentWidth, y1 + chartH), fuels)
            y1 += chartH + 16f
        }

        // Mini Table of Recent Fuelings on Page 1 if space allows
        if (y1 < pageHeight - 170) {
            canvas1.drawText("Abastecimentos Recentes", margin, y1, sectionPaint)
            y1 += 12f

            val thPaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                style = Paint.Style.FILL
            }
            canvas1.drawRect(margin, y1, margin + contentWidth, y1 + 16f, thPaint)
            canvas1.drawText("Data", margin + 4, y1 + 11f, boldText)
            canvas1.drawText("Odômetro", margin + 65, y1 + 11f, boldText)
            canvas1.drawText("Combustível", margin + 140, y1 + 11f, boldText)
            canvas1.drawText("Litros", margin + 245, y1 + 11f, boldText)
            canvas1.drawText("R$/L", margin + 305, y1 + 11f, boldText)
            canvas1.drawText("Total Pago", margin + 365, y1 + 11f, boldText)
            canvas1.drawText("Posto", margin + 440, y1 + 11f, boldText)
            y1 += 18f

            var alt = false
            for (f in fuels.take(7)) {
                if (alt) canvas1.drawRect(margin, y1 - 2, margin + contentWidth, y1 + 13f, cardPaint)
                canvas1.drawText(dateOnlyFormat.format(Date(f.dateMillis)), margin + 4, y1 + 9f, normalText)
                canvas1.drawText("${f.odometerKm.toInt()} km", margin + 65, y1 + 9f, normalText)
                canvas1.drawText(f.fuelType.shortName, margin + 140, y1 + 9f, normalText)
                canvas1.drawText(String.format(Locale("pt", "BR"), "%.1f L", f.liters), margin + 245, y1 + 9f, normalText)
                canvas1.drawText(String.format(Locale("pt", "BR"), "%.2f", f.pricePerLiter), margin + 305, y1 + 9f, normalText)
                canvas1.drawText(String.format(Locale("pt", "BR"), "R$ %.2f", f.totalCost), margin + 365, y1 + 9f, boldText)
                val st = if (f.stationName.length > 14) f.stationName.take(12) + ".." else f.stationName
                canvas1.drawText(st.ifBlank { "-" }, margin + 440, y1 + 9f, normalText)
                y1 += 15f
                alt = !alt
            }
        }

        // Footer Page 1
        canvas1.drawLine(margin, pageHeight - 32f, margin + contentWidth, pageHeight - 32f, linePaint)
        canvas1.drawText("Página 1 de $totalPages • Combustível IA • Relatório Automotivo Inteligente", margin, pageHeight - 18f, smallText)
        doc.finishPage(page1)

        // ==========================================
        // PAGE 2: Gráficos Avançados (Temporal, Barras, Dispersão)
        // ==========================================
        val hasTemporal = selectedCharts.contains(ChartExportType.TEMPORAL)
        val hasBar = selectedCharts.contains(ChartExportType.BARRAS)
        val hasScatter = selectedCharts.contains(ChartExportType.DISPERSAO)

        if (totalPages >= 3 || (hasTemporal || hasBar || hasScatter)) {
            val page2Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = doc.startPage(page2Info)
            val canvas2: Canvas = page2.canvas

            var y2 = 36f
            canvas2.drawRect(margin, y2, margin + contentWidth, y2 + 4, accentPaint)
            y2 += 18f

            canvas2.drawText("ANÁLISE GRÁFICA AVANÇADA & CORRELAÇÕES", margin, y2, titlePaint)
            y2 += 14f
            canvas2.drawText("Histórico temporal de rendimento, consumo por combustível e dispersão de custos", margin, y2, subtitlePaint)
            y2 += 22f

            // Temporal chart if not rendered on page 1 or explicitly requested
            if (hasTemporal) {
                val temporalH = 175f
                PdfChartRenderer.drawTemporalChart(canvas2, RectF(margin, y2, margin + contentWidth, y2 + temporalH), fuels)
                y2 += temporalH + 18f
            }

            if (hasBar && hasScatter) {
                val chartW = (contentWidth - 10f) / 2f
                val chartH = 180f
                PdfChartRenderer.drawBarChart(canvas2, RectF(margin, y2, margin + chartW, y2 + chartH), fuels)
                PdfChartRenderer.drawScatterChart(canvas2, RectF(margin + chartW + 10f, y2, margin + contentWidth, y2 + chartH), fuels)
                y2 += chartH + 18f
            } else if (hasBar) {
                val chartH = 180f
                PdfChartRenderer.drawBarChart(canvas2, RectF(margin, y2, margin + contentWidth, y2 + chartH), fuels)
                y2 += chartH + 18f
            } else if (hasScatter) {
                val chartH = 180f
                PdfChartRenderer.drawScatterChart(canvas2, RectF(margin, y2, margin + contentWidth, y2 + chartH), fuels)
                y2 += chartH + 18f
            }

            // If space allows, render Maintenance section
            if (y2 < pageHeight - 180 && maintenances.isNotEmpty()) {
                canvas2.drawText("Histórico de Manutenções Registradas", margin, y2, sectionPaint)
                y2 += 12f

                val thPaint = Paint().apply {
                    color = Color.rgb(226, 232, 240)
                    style = Paint.Style.FILL
                }
                canvas2.drawRect(margin, y2, margin + contentWidth, y2 + 16f, thPaint)
                canvas2.drawText("Data", margin + 4, y2 + 11f, boldText)
                canvas2.drawText("Km", margin + 70, y2 + 11f, boldText)
                canvas2.drawText("Serviço Realizado", margin + 140, y2 + 11f, boldText)
                canvas2.drawText("Oficina / Local", margin + 330, y2 + 11f, boldText)
                canvas2.drawText("Custo Total", margin + 440, y2 + 11f, boldText)
                y2 += 18f

                var alt = false
                for (m in maintenances.take(8)) {
                    if (alt) canvas2.drawRect(margin, y2 - 2, margin + contentWidth, y2 + 13f, cardPaint)
                    canvas2.drawText(dateOnlyFormat.format(Date(m.dateMillis)), margin + 4, y2 + 9f, normalText)
                    canvas2.drawText("${m.odometerKm.toInt()} km", margin + 70, y2 + 9f, normalText)
                    val srv = if (m.serviceType.length > 27) m.serviceType.take(25) + ".." else m.serviceType
                    canvas2.drawText(srv, margin + 140, y2 + 9f, normalText)
                    val off = if (m.workshopName.length > 18) m.workshopName.take(16) + ".." else m.workshopName
                    canvas2.drawText(off.ifBlank { "-" }, margin + 330, y2 + 9f, normalText)
                    canvas2.drawText(String.format(Locale("pt", "BR"), "R$ %.2f", m.cost), margin + 440, y2 + 9f, boldText)
                    y2 += 15f
                    alt = !alt
                }
            }

            if (wearStatuses.isNotEmpty() && y2 < pageHeight - 120) {
                y2 += 8f
                canvas2.drawText("Controle de Troca de Óleo e Filtros", margin, y2, sectionPaint)
                y2 += 12f
                val thPaint = Paint().apply {
                    color = Color.rgb(226, 232, 240)
                    style = Paint.Style.FILL
                }
                canvas2.drawRect(margin, y2, margin + contentWidth, y2 + 16f, thPaint)
                canvas2.drawText("Item / Componente", margin + 4, y2 + 11f, boldText)
                canvas2.drawText("Últ. Troca", margin + 180, y2 + 11f, boldText)
                canvas2.drawText("Intervalo", margin + 250, y2 + 11f, boldText)
                canvas2.drawText("Restantes", margin + 325, y2 + 11f, boldText)
                canvas2.drawText("Desgaste", margin + 400, y2 + 11f, boldText)
                canvas2.drawText("Situação", margin + 465, y2 + 11f, boldText)
                y2 += 18f

                var altWear = false
                for (w in wearStatuses) {
                    if (y2 >= pageHeight - 40f) break
                    if (altWear) canvas2.drawRect(margin, y2 - 2, margin + contentWidth, y2 + 13f, cardPaint)
                    canvas2.drawText(w.item.name, margin + 4, y2 + 9f, normalText)
                    canvas2.drawText("${w.item.lastReplacedKm.toInt()} km", margin + 180, y2 + 9f, normalText)
                    canvas2.drawText("${w.item.recommendedIntervalKm.toInt()} km", margin + 250, y2 + 9f, normalText)
                    val remText = if (w.remainingKm >= 0) "${w.remainingKm.toInt()} km" else "+${(-w.remainingKm).toInt()} km venc."
                    canvas2.drawText(remText, margin + 325, y2 + 9f, normalText)
                    canvas2.drawText(String.format(Locale("pt", "BR"), "%.0f%%", w.depreciationPercentage), margin + 400, y2 + 9f, boldText)
                    val statusColor = when (w.status) {
                        WearAlertLevel.CRITICAL -> Color.rgb(220, 38, 38)
                        WearAlertLevel.WARNING -> Color.rgb(217, 119, 6)
                        WearAlertLevel.HALF_LIFE -> Color.rgb(2, 132, 199)
                        WearAlertLevel.OK -> Color.rgb(16, 185, 129)
                    }
                    val statusText = when (w.status) {
                        WearAlertLevel.CRITICAL -> "VENCIDO"
                        WearAlertLevel.WARNING -> "ATENÇÃO"
                        WearAlertLevel.HALF_LIFE -> "50% ATING."
                        WearAlertLevel.OK -> "EM DIA"
                    }
                    val stPaint = Paint(boldText).apply { color = statusColor }
                    canvas2.drawText(statusText, margin + 465, y2 + 9f, stPaint)
                    y2 += 15f
                    altWear = !altWear
                }
            }

            // Footer Page 2
            canvas2.drawLine(margin, pageHeight - 32f, margin + contentWidth, pageHeight - 32f, linePaint)
            canvas2.drawText("Página 2 de $totalPages • Combustível IA • Análise Estatística", margin, pageHeight - 18f, smallText)
            doc.finishPage(page2)
        }

        // ==========================================
        // PAGE 3 (If 3 pages): Detalhamento Completo de Tabelas
        // ==========================================
        if (totalPages >= 3) {
            val page3Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
            val page3 = doc.startPage(page3Info)
            val canvas3: Canvas = page3.canvas

            var y3 = 36f
            canvas3.drawRect(margin, y3, margin + contentWidth, y3 + 4, accentPaint)
            y3 += 18f

            canvas3.drawText("DETALHAMENTO DE REGISTROS FINANCEIROS", margin, y3, titlePaint)
            y3 += 14f
            canvas3.drawText("Tabela completa de abastecimentos, despesas e receitas da frota", margin, y3, subtitlePaint)
            y3 += 22f

            // Full Fuels Table
            canvas3.drawText("Abastecimentos Cadastrados", margin, y3, sectionPaint)
            y3 += 12f

            val thPaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                style = Paint.Style.FILL
            }
            canvas3.drawRect(margin, y3, margin + contentWidth, y3 + 16f, thPaint)
            canvas3.drawText("Data", margin + 4, y3 + 11f, boldText)
            canvas3.drawText("Odômetro", margin + 65, y3 + 11f, boldText)
            canvas3.drawText("Combustível", margin + 140, y3 + 11f, boldText)
            canvas3.drawText("Litros", margin + 245, y3 + 11f, boldText)
            canvas3.drawText("R$/L", margin + 305, y3 + 11f, boldText)
            canvas3.drawText("Total Pago", margin + 365, y3 + 11f, boldText)
            canvas3.drawText("Posto / Local", margin + 440, y3 + 11f, boldText)
            y3 += 18f

            var alt = false
            for (f in fuels.take(18)) {
                if (alt) canvas3.drawRect(margin, y3 - 2, margin + contentWidth, y3 + 13f, cardPaint)
                canvas3.drawText(dateOnlyFormat.format(Date(f.dateMillis)), margin + 4, y3 + 9f, normalText)
                canvas3.drawText("${f.odometerKm.toInt()} km", margin + 65, y3 + 9f, normalText)
                canvas3.drawText(f.fuelType.shortName, margin + 140, y3 + 9f, normalText)
                canvas3.drawText(String.format(Locale("pt", "BR"), "%.1f L", f.liters), margin + 245, y3 + 9f, normalText)
                canvas3.drawText(String.format(Locale("pt", "BR"), "%.2f", f.pricePerLiter), margin + 305, y3 + 9f, normalText)
                canvas3.drawText(String.format(Locale("pt", "BR"), "R$ %.2f", f.totalCost), margin + 365, y3 + 9f, boldText)
                val st = if (f.stationName.length > 14) f.stationName.take(12) + ".." else f.stationName
                canvas3.drawText(st.ifBlank { "-" }, margin + 440, y3 + 9f, normalText)
                y3 += 15f
                alt = !alt
            }

            y3 += 16f
            if (y3 < pageHeight - 160 && finances.isNotEmpty()) {
                canvas3.drawText("Receitas & Outras Despesas", margin, y3, sectionPaint)
                y3 += 12f
                canvas3.drawRect(margin, y3, margin + contentWidth, y3 + 16f, thPaint)
                canvas3.drawText("Data", margin + 4, y3 + 11f, boldText)
                canvas3.drawText("Tipo", margin + 80, y3 + 11f, boldText)
                canvas3.drawText("Categoria", margin + 150, y3 + 11f, boldText)
                canvas3.drawText("Valor (R$)", margin + 330, y3 + 11f, boldText)
                canvas3.drawText("Observações", margin + 420, y3 + 11f, boldText)
                y3 += 18f

                alt = false
                for (fn in finances.take(8)) {
                    if (alt) canvas3.drawRect(margin, y3 - 2, margin + contentWidth, y3 + 13f, cardPaint)
                    canvas3.drawText(dateOnlyFormat.format(Date(fn.dateMillis)), margin + 4, y3 + 9f, normalText)
                    canvas3.drawText(if (fn.type == FinanceType.RECEITA) "Receita" else "Despesa", margin + 80, y3 + 9f, normalText)
                    canvas3.drawText(fn.category, margin + 150, y3 + 9f, normalText)
                    val fnPaint = if (fn.type == FinanceType.RECEITA) Paint(boldText).apply { color = Color.rgb(16, 185, 129) } else boldText
                    canvas3.drawText(String.format(Locale("pt", "BR"), "R$ %.2f", fn.amount), margin + 330, y3 + 9f, fnPaint)
                    val noteTrunc = if (fn.notes.length > 20) fn.notes.take(18) + ".." else fn.notes
                    canvas3.drawText(noteTrunc.ifBlank { "-" }, margin + 420, y3 + 9f, normalText)
                    y3 += 15f
                    alt = !alt
                }
            }

            // Footer Page 3
            canvas3.drawLine(margin, pageHeight - 32f, margin + contentWidth, pageHeight - 32f, linePaint)
            canvas3.drawText("Página 3 de $totalPages • Combustível IA • Registro Detalhado", margin, pageHeight - 18f, smallText)
            doc.finishPage(page3)
        }

        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()

        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
