package com.example.reports

import com.example.model.ChartExportType
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.MaintenanceEntry
import com.example.model.MaintenanceWearStatus
import com.example.model.VehicleStats
import com.example.model.WearAlertLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates rich Microsoft Excel SpreadsheetML XML format (.xls).
 * Fully compatible with Microsoft Excel, Google Sheets, LibreOffice Calc, and mobile office apps.
 * Features multiple worksheets, formatting styles, formulas, and structured datasets for charts.
 */
object ExcelXmlExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val monthFormat = SimpleDateFormat("MM/yyyy", Locale("pt", "BR"))

    fun generateXls(
        periodLabel: String,
        stats: VehicleStats,
        fuels: List<FuelEntry>,
        maintenances: List<MaintenanceEntry>,
        finances: List<FinanceEntry>,
        selectedCharts: Set<ChartExportType> = ChartExportType.entries.toSet(),
        wearStatuses: List<MaintenanceWearStatus> = emptyList()
    ): String {
        val sb = StringBuilder()
        val now = dateFormat.format(Date())

        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 <DocumentProperties xmlns="urn:schemas-microsoft-com:office:office">
  <Author>Combustível IA</Author>
  <Created>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())}</Created>
  <Company>Assistente Automotivo Inteligente</Company>
  <Title>Relatório Financeiro e Operacional Veicular</Title>
 </DocumentProperties>
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Center"/>
   <Borders/>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#000000"/>
   <Interior/>
   <NumberFormat/>
   <Protection/>
  </Style>
  <Style ss:ID="TitleHeader">
   <Font ss:FontName="Calibri" ss:Size="15" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#004A77" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="SubTitle">
   <Font ss:FontName="Calibri" ss:Size="10" ss:Italic="1" ss:Color="#64748B"/>
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="SectionHeader">
   <Font ss:FontName="Calibri" ss:Size="12" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#0F4C81" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="TableHeader">
   <Font ss:FontName="Calibri" ss:Size="10" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#1E293B" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="TableHeaderAlt">
   <Font ss:FontName="Calibri" ss:Size="10" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#008080" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Currency">
   <NumberFormat ss:Format="&quot;R$&quot;\ #,##0.00"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="CurrencyBold">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#004A77"/>
   <NumberFormat ss:Format="&quot;R$&quot;\ #,##0.00"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Decimal2">
   <NumberFormat ss:Format="#,##0.00"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Decimal3">
   <NumberFormat ss:Format="#,##0.000"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Integer">
   <NumberFormat ss:Format="#,##0"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Percent">
   <NumberFormat ss:Format="0.0%"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="DateCenter">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="KpiLabel">
   <Font ss:FontName="Calibri" ss:Size="10" ss:Bold="1" ss:Color="#334155"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="KpiValue">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#004A77"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
  </Style>
 </Styles>
""")

        // WORKSHEET 1: Resumo Executivo
        sb.append(""" <Worksheet ss:Name="Resumo Executivo">
  <Table ss:ExpandedColumnCount="5" x:FullColumns="1" x:FullRows="1" ss:DefaultRowHeight="20">
   <Column ss:Width="230"/>
   <Column ss:Width="130"/>
   <Column ss:Width="30"/>
   <Column ss:Width="200"/>
   <Column ss:Width="130"/>
   <Row ss:Height="26">
    <Cell ss:MergeAcross="4" ss:StyleID="TitleHeader"><Data ss:Type="String"> COMBUSTÍVEL IA - RELATÓRIO EXECUTIVO VEICULAR</Data></Cell>
   </Row>
   <Row>
    <Cell ss:MergeAcross="4" ss:StyleID="SubTitle"><Data ss:Type="String">Período: ${escapeXml(periodLabel)}  |  Emitido em: ${escapeXml(now)}</Data></Cell>
   </Row>
   <Row><Cell><Data ss:Type="String"></Data></Cell></Row>
   <Row ss:Height="22">
    <Cell ss:MergeAcross="1" ss:StyleID="SectionHeader"><Data ss:Type="String"> INDICADORES DE EFICIÊNCIA</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:MergeAcross="1" ss:StyleID="SectionHeader"><Data ss:Type="String"> INDICADORES FINANCEIROS</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Distância Total Percorrida</Data></Cell>
    <Cell ss:StyleID="KpiValue"><Data ss:Type="Number">${String.format(Locale.US, "%.1f", stats.totalDistanceKm)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Gasto Total com Combustível</Data></Cell>
    <Cell ss:StyleID="CurrencyBold"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalFuelCost)}</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Combustível Total Abastecido</Data></Cell>
    <Cell ss:StyleID="KpiValue"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalLiters)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Gasto Total com Manutenção</Data></Cell>
    <Cell ss:StyleID="CurrencyBold"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalMaintenanceCost)}</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Consumo Médio Geral (km/L)</Data></Cell>
    <Cell ss:StyleID="KpiValue"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.avgKmPerLiter)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Outras Despesas (Impostos/Taxas)</Data></Cell>
    <Cell ss:StyleID="CurrencyBold"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalOtherExpenses)}</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Custo Médio por Km Rodado</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.3f", stats.avgCostPerKm)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">TOTAL GERAL DE DESPESAS</Data></Cell>
    <Cell ss:StyleID="CurrencyBold"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalExpenses)}</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Preço Médio por Litro</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.3f", stats.avgPricePerLiter)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Total de Receitas Operacionais</Data></Cell>
    <Cell ss:StyleID="CurrencyBold"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalRevenues)}</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">Quantidade de Abastecimentos</Data></Cell>
    <Cell ss:StyleID="Integer"><Data ss:Type="Number">${stats.fuelCount}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="KpiLabel"><Data ss:Type="String">SALDO LÍQUIDO OPERACIONAL</Data></Cell>
    <Cell ss:StyleID="CurrencyBold"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.netBalance)}</Data></Cell>
   </Row>
  </Table>
 </Worksheet>
""")

        // WORKSHEET 2: Abastecimentos
        sb.append(""" <Worksheet ss:Name="Abastecimentos">
  <Table ss:ExpandedColumnCount="9" x:FullColumns="1" x:FullRows="1" ss:DefaultRowHeight="18">
   <Column ss:Width="110"/>
   <Column ss:Width="95"/>
   <Column ss:Width="130"/>
   <Column ss:Width="80"/>
   <Column ss:Width="90"/>
   <Column ss:Width="100"/>
   <Column ss:Width="160"/>
   <Column ss:Width="85"/>
   <Column ss:Width="180"/>
   <Row ss:Height="22">
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Data / Hora</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Odômetro (km)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Combustível</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Litros</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Preço / L</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Total Pago</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Posto / Local</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Tanque Cheio</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Observações</Data></Cell>
   </Row>
""")
        for (f in fuels) {
            sb.append("""   <Row>
    <Cell ss:StyleID="DateCenter"><Data ss:Type="String">${escapeXml(dateFormat.format(Date(f.dateMillis)))}</Data></Cell>
    <Cell ss:StyleID="Integer"><Data ss:Type="Number">${f.odometerKm.toInt()}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(f.fuelType.displayName)}</Data></Cell>
    <Cell ss:StyleID="Decimal2"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", f.liters)}</Data></Cell>
    <Cell ss:StyleID="Decimal3"><Data ss:Type="Number">${String.format(Locale.US, "%.3f", f.pricePerLiter)}</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", f.totalCost)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(f.stationName)}</Data></Cell>
    <Cell ss:StyleID="DateCenter"><Data ss:Type="String">${if (f.isFullTank) "Sim" else "Não"}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(f.notes)}</Data></Cell>
   </Row>
""")
        }
        sb.append("""  </Table>
 </Worksheet>
""")

        // WORKSHEET 3: Manutenções
        sb.append(""" <Worksheet ss:Name="Manutenções">
  <Table ss:ExpandedColumnCount="6" x:FullColumns="1" x:FullRows="1" ss:DefaultRowHeight="18">
   <Column ss:Width="110"/>
   <Column ss:Width="95"/>
   <Column ss:Width="180"/>
   <Column ss:Width="160"/>
   <Column ss:Width="110"/>
   <Column ss:Width="200"/>
   <Row ss:Height="22">
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Data</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Odômetro (km)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Serviço Realizado</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Oficina / Mecânica</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Custo Total</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Observações</Data></Cell>
   </Row>
""")
        for (m in maintenances) {
            sb.append("""   <Row>
    <Cell ss:StyleID="DateCenter"><Data ss:Type="String">${escapeXml(dateFormat.format(Date(m.dateMillis)))}</Data></Cell>
    <Cell ss:StyleID="Integer"><Data ss:Type="Number">${m.odometerKm.toInt()}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(m.serviceType)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(m.workshopName)}</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", m.cost)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(m.notes)}</Data></Cell>
   </Row>
""")
        }
        sb.append("""  </Table>
 </Worksheet>
""")

        // WORKSHEET 4: Receitas e Despesas
        sb.append(""" <Worksheet ss:Name="Receitas e Despesas">
  <Table ss:ExpandedColumnCount="5" x:FullColumns="1" x:FullRows="1" ss:DefaultRowHeight="18">
   <Column ss:Width="110"/>
   <Column ss:Width="90"/>
   <Column ss:Width="160"/>
   <Column ss:Width="110"/>
   <Column ss:Width="220"/>
   <Row ss:Height="22">
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Data</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Tipo</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Categoria</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Valor</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Observações</Data></Cell>
   </Row>
""")
        for (fn in finances) {
            sb.append("""   <Row>
    <Cell ss:StyleID="DateCenter"><Data ss:Type="String">${escapeXml(dateFormat.format(Date(fn.dateMillis)))}</Data></Cell>
    <Cell ss:StyleID="DateCenter"><Data ss:Type="String">${if (fn.type == FinanceType.RECEITA) "Receita" else "Despesa"}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(fn.category)}</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", fn.amount)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(fn.notes)}</Data></Cell>
   </Row>
""")
        }
        sb.append("""  </Table>
 </Worksheet>
""")

        // WORKSHEET 5: Dados para Gráficos (Data tables ready for Excel Chart Wizard)
        sb.append(""" <Worksheet ss:Name="Dados para Gráficos">
  <Table ss:ExpandedColumnCount="6" x:FullColumns="1" x:FullRows="1" ss:DefaultRowHeight="18">
   <Column ss:Width="180"/>
   <Column ss:Width="120"/>
   <Column ss:Width="100"/>
   <Column ss:Width="100"/>
   <Column ss:Width="120"/>
   <Column ss:Width="150"/>
   <Row ss:Height="24">
    <Cell ss:MergeAcross="4" ss:StyleID="TitleHeader"><Data ss:Type="String"> SÉRIES E TABELAS ESTRUTURADAS PARA GRÁFICOS NO EXCEL</Data></Cell>
   </Row>
   <Row><Cell><Data ss:Type="String"></Data></Cell></Row>
""")

        // 1. Table for PIZZA (Distribuição de Despesas)
        val totalExp = (stats.totalFuelCost + stats.totalMaintenanceCost + stats.totalOtherExpenses).coerceAtLeast(1.0)
        sb.append("""   <Row ss:Height="20">
    <Cell ss:MergeAcross="2" ss:StyleID="TableHeaderAlt"><Data ss:Type="String">1. Série para Gráfico de PIZZA: Distribuição de Despesas</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Categoria de Custo</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Valor Total (R$)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Participação (%)</Data></Cell>
   </Row>
   <Row>
    <Cell><Data ss:Type="String">Combustível</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalFuelCost)}</Data></Cell>
    <Cell ss:StyleID="Percent"><Data ss:Type="Number">${String.format(Locale.US, "%.4f", stats.totalFuelCost / totalExp)}</Data></Cell>
   </Row>
   <Row>
    <Cell><Data ss:Type="String">Manutenção Preventiva e Corretiva</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalMaintenanceCost)}</Data></Cell>
    <Cell ss:StyleID="Percent"><Data ss:Type="Number">${String.format(Locale.US, "%.4f", stats.totalMaintenanceCost / totalExp)}</Data></Cell>
   </Row>
   <Row>
    <Cell><Data ss:Type="String">Outras Despesas Operacionais</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", stats.totalOtherExpenses)}</Data></Cell>
    <Cell ss:StyleID="Percent"><Data ss:Type="Number">${String.format(Locale.US, "%.4f", stats.totalOtherExpenses / totalExp)}</Data></Cell>
   </Row>
   <Row><Cell><Data ss:Type="String"></Data></Cell></Row>
""")

        // 2. Table for BARRAS (Gastos por Tipo de Combustível)
        val fuelsByType = fuels.groupBy { it.fuelType }
        sb.append("""   <Row ss:Height="20">
    <Cell ss:MergeAcross="3" ss:StyleID="TableHeaderAlt"><Data ss:Type="String">2. Série para Gráfico de BARRAS: Custos por Combustível</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Tipo de Combustível</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Total Gasto (R$)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Volume (Litros)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Preço Médio / L</Data></Cell>
   </Row>
""")
        for ((fType, list) in fuelsByType) {
            val totalCost = list.sumOf { it.totalCost }
            val totalL = list.sumOf { it.liters }
            val avgPrice = if (totalL > 0) totalCost / totalL else 0.0
            sb.append("""   <Row>
    <Cell><Data ss:Type="String">${escapeXml(fType.displayName)}</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", totalCost)}</Data></Cell>
    <Cell ss:StyleID="Decimal2"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", totalL)}</Data></Cell>
    <Cell ss:StyleID="Decimal3"><Data ss:Type="Number">${String.format(Locale.US, "%.3f", avgPrice)}</Data></Cell>
   </Row>
""")
        }
        sb.append("""   <Row><Cell><Data ss:Type="String"></Data></Cell></Row>
""")

        // 3. Table for TEMPORAL (Evolução Cronológica de Consumo e Preço)
        sb.append("""   <Row ss:Height="20">
    <Cell ss:MergeAcross="4" ss:StyleID="TableHeaderAlt"><Data ss:Type="String">3. Série para Gráfico TEMPORAL: Evolução Histórica</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Data do Registro</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Odômetro (km)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Litros</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Preço/L (R$)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Custo Total (R$)</Data></Cell>
   </Row>
""")
        val sortedFuels = fuels.sortedBy { it.dateMillis }
        for (f in sortedFuels) {
            sb.append("""   <Row>
    <Cell ss:StyleID="DateCenter"><Data ss:Type="String">${escapeXml(dateOnlyFormat.format(Date(f.dateMillis)))}</Data></Cell>
    <Cell ss:StyleID="Integer"><Data ss:Type="Number">${f.odometerKm.toInt()}</Data></Cell>
    <Cell ss:StyleID="Decimal2"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", f.liters)}</Data></Cell>
    <Cell ss:StyleID="Decimal3"><Data ss:Type="Number">${String.format(Locale.US, "%.3f", f.pricePerLiter)}</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", f.totalCost)}</Data></Cell>
   </Row>
""")
        }
        sb.append("""   <Row><Cell><Data ss:Type="String"></Data></Cell></Row>
""")

        // 4. Table for DISPERSÃO (Litros vs Custo)
        sb.append("""   <Row ss:Height="20">
    <Cell ss:MergeAcross="3" ss:StyleID="TableHeaderAlt"><Data ss:Type="String">4. Série para Gráfico de DISPERSÃO: Litros (Eixo X) vs. Custo R$ (Eixo Y)</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Data</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Litros Abastecidos (X)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Custo Total Pago (Y)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Posto / Cidade</Data></Cell>
   </Row>
""")
        for (f in fuels) {
            sb.append("""   <Row>
    <Cell ss:StyleID="DateCenter"><Data ss:Type="String">${escapeXml(dateOnlyFormat.format(Date(f.dateMillis)))}</Data></Cell>
    <Cell ss:StyleID="Decimal2"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", f.liters)}</Data></Cell>
    <Cell ss:StyleID="Currency"><Data ss:Type="Number">${String.format(Locale.US, "%.2f", f.totalCost)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(f.stationName)}</Data></Cell>
   </Row>
""")
        }

        sb.append("""  </Table>
 </Worksheet>
""")

        if (wearStatuses.isNotEmpty()) {
            sb.append(""" <Worksheet ss:Name="Desgaste e Filtros">
  <Table ss:DefaultRowHeight="18">
   <Column ss:Width="160"/>
   <Column ss:Width="110"/>
   <Column ss:Width="120"/>
   <Column ss:Width="100"/>
   <Column ss:Width="90"/>
   <Column ss:Width="120"/>
   <Row ss:Height="24">
    <Cell ss:MergeAcross="5" ss:StyleID="Header"><Data ss:Type="String">CONTROLE DE TROCA DE ÓLEO &amp; DESGASTE DE FILTROS</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Item / Componente</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Última Troca (km)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Intervalo Recomendado</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Km Restantes</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Desgaste (%)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">Situação / Alerta</Data></Cell>
   </Row>
""")
            for (w in wearStatuses) {
                val statusLabel = when (w.status) {
                    WearAlertLevel.CRITICAL -> "CRÍTICO (VENCIDO)"
                    WearAlertLevel.WARNING -> "ATENÇÃO (PRÓXIMO)"
                    WearAlertLevel.HALF_LIFE -> "METADE DO INTERVALO (50%)"
                    WearAlertLevel.OK -> "EM DIA"
                }
                sb.append("""   <Row>
    <Cell><Data ss:Type="String">${escapeXml(w.item.name)}</Data></Cell>
    <Cell ss:StyleID="Integer"><Data ss:Type="Number">${w.item.lastReplacedKm.toInt()}</Data></Cell>
    <Cell ss:StyleID="Integer"><Data ss:Type="Number">${w.item.recommendedIntervalKm.toInt()}</Data></Cell>
    <Cell ss:StyleID="Integer"><Data ss:Type="Number">${w.remainingKm.toInt()}</Data></Cell>
    <Cell ss:StyleID="Decimal2"><Data ss:Type="Number">${String.format(Locale.US, "%.1f", w.depreciationPercentage)}</Data></Cell>
    <Cell><Data ss:Type="String">$statusLabel</Data></Cell>
   </Row>
""")
            }
            sb.append("""  </Table>
 </Worksheet>
""")
        }

        sb.append("""</Workbook>""")

        return sb.toString()
    }

    private fun escapeXml(str: String?): String {
        if (str.isNullOrEmpty()) return ""
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
