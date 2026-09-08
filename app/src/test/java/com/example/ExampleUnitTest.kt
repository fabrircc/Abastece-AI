package com.example

import com.example.model.ChartExportType
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.FuelType
import com.example.model.MaintenanceEntry
import com.example.model.VehicleStats
import com.example.reports.ExcelXmlExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testChartExportTypesAvailable() {
    val types = ChartExportType.entries
    assertEquals(5, types.size)
    assertTrue(types.contains(ChartExportType.COLUNAS))
    assertTrue(types.contains(ChartExportType.PIZZA))
    assertTrue(types.contains(ChartExportType.TEMPORAL))
    assertTrue(types.contains(ChartExportType.BARRAS))
    assertTrue(types.contains(ChartExportType.DISPERSAO))
  }

  @Test
  fun testExcelXmlExporterGeneratesValidSpreadsheetMl() {
    val stats = VehicleStats(
      totalDistanceKm = 850.0,
      totalLiters = 75.0,
      totalFuelCost = 412.50,
      totalMaintenanceCost = 250.0,
      totalOtherExpenses = 50.0,
      totalRevenues = 1200.0,
      avgKmPerLiter = 11.33,
      avgCostPerKm = 0.485
    )

    val fuels = listOf(
      FuelEntry(
        id = 1,
        dateMillis = System.currentTimeMillis(),
        odometerKm = 50000.0,
        fuelType = FuelType.GASOLINA_COMUM,
        pricePerLiter = 5.50,
        liters = 40.0,
        totalCost = 220.0,
        stationName = "Posto Shell"
      )
    )

    val maints = listOf(
      MaintenanceEntry(
        id = 1,
        dateMillis = System.currentTimeMillis(),
        odometerKm = 50000.0,
        serviceType = "Troca de Óleo e Filtro",
        workshopName = "Oficina Central",
        cost = 250.0
      )
    )

    val finances = listOf(
      FinanceEntry(
        id = 1,
        dateMillis = System.currentTimeMillis(),
        type = FinanceType.RECEITA,
        category = "Frete Particular",
        amount = 1200.0
      )
    )

    val xml = ExcelXmlExporter.generateXls(
      periodLabel = "Este Mês",
      stats = stats,
      fuels = fuels,
      maintenances = maints,
      finances = finances,
      selectedCharts = ChartExportType.entries.toSet()
    )

    assertTrue(xml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
    assertTrue(xml.contains("Workbook"))
    assertTrue(xml.contains("Resumo Executivo"))
    assertTrue(xml.contains("Abastecimentos"))
    assertTrue(xml.contains("Manutenções"))
    assertTrue(xml.contains("Receitas e Despesas"))
    assertTrue(xml.contains("Dados para Gráficos"))
    assertTrue(xml.contains("Posto Shell"))
    assertTrue(xml.contains("Troca de Óleo e Filtro"))
    assertTrue(xml.contains("Frete Particular"))
  }

  @Test
  fun testHalfIntervalMaintenanceAlerts() {
    val item = com.example.model.MaintenanceTrackerItem(
      idKey = "oleo_motor",
      name = "Óleo do Motor",
      category = "Motor",
      recommendedIntervalKm = 10000.0,
      lastReplacedKm = 40000.0
    )

    // At 40% (4,000 km driven) -> OK
    val status40 = com.example.model.MaintenanceWearCalculator.calculateStatus(item, 44000.0)
    assertEquals(com.example.model.WearAlertLevel.OK, status40.status)
    assertEquals(false, status40.reachedHalfInterval)
    assertEquals(false, status40.isAlertActive)

    // At 50% (5,000 km driven) -> HALF_LIFE alert
    val status50 = com.example.model.MaintenanceWearCalculator.calculateStatus(item, 45000.0)
    assertEquals(com.example.model.WearAlertLevel.HALF_LIFE, status50.status)
    assertEquals(true, status50.reachedHalfInterval)
    assertEquals(true, status50.isHalfLife)
    assertEquals(true, status50.isAlertActive)
    assertEquals(5000.0, status50.halfIntervalKm, 0.01)

    // At 70% (7,000 km driven) -> Still HALF_LIFE before warning threshold
    val status70 = com.example.model.MaintenanceWearCalculator.calculateStatus(item, 47000.0)
    assertEquals(com.example.model.WearAlertLevel.HALF_LIFE, status70.status)
    assertEquals(true, status70.reachedHalfInterval)

    // At 86% (8,600 km driven, remaining 1,400 km) -> WARNING
    val status86 = com.example.model.MaintenanceWearCalculator.calculateStatus(item, 48600.0)
    assertEquals(com.example.model.WearAlertLevel.WARNING, status86.status)
    assertEquals(true, status86.reachedHalfInterval)

    // At 100% (10,000 km driven) -> CRITICAL
    val status100 = com.example.model.MaintenanceWearCalculator.calculateStatus(item, 50000.0)
    assertEquals(com.example.model.WearAlertLevel.CRITICAL, status100.status)
    assertEquals(true, status100.isCritical)
  }
}
