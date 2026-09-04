package com.example.model

enum class PeriodFilter(val label: String) {
    TODAY("Hoje"),
    WEEK("Esta Semana"),
    MONTH("Este Mês"),
    YEAR("Este Ano"),
    CUSTOM("Personalizado"),
    ALL("Tudo")
}

data class VehicleStats(
    val totalDistanceKm: Double = 0.0,
    val totalLiters: Double = 0.0,
    val totalFuelCost: Double = 0.0,
    val avgKmPerLiter: Double = 0.0,
    val avgCostPerKm: Double = 0.0,
    val avgPricePerLiter: Double = 0.0,
    val totalMaintenanceCost: Double = 0.0,
    val totalOtherExpenses: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalRevenues: Double = 0.0,
    val netBalance: Double = 0.0,
    val fuelCount: Int = 0,
    val maintenanceCount: Int = 0,
    val revenueCount: Int = 0,
    val otherExpensesCount: Int = 0
)

data class ConsumptionPoint(
    val dateMillis: Long,
    val kmPerLiter: Double,
    val costPerKm: Double,
    val fuelType: FuelType,
    val odometerKm: Double,
    val liters: Double,
    val station: String
)

data class ExpenseCategoryBreakdown(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Float
)

enum class ChartExportType(
    val title: String,
    val subtitle: String,
    val description: String
) {
    COLUNAS(
        title = "Colunas",
        subtitle = "Gastos por Período",
        description = "Barras verticais comparando combustíveis, manutenções e totais por mês"
    ),
    PIZZA(
        title = "Pizza",
        subtitle = "Distribuição de Despesas",
        description = "Gráfico circular demonstrando proporção percentual de cada categoria de gasto"
    ),
    TEMPORAL(
        title = "Temporal",
        subtitle = "Evolução do Consumo (km/L)",
        description = "Linha cronológica com médias de consumo e oscilações de rendimento"
    ),
    BARRAS(
        title = "Barras",
        subtitle = "Custos por Combustível",
        description = "Barras horizontais comparando despesa por Gasolina, Etanol, Diesel e GNV"
    ),
    DISPERSAO(
        title = "Dispersão",
        subtitle = "Litros vs. Custo Total",
        description = "Diagrama de dispersão correlacionando volume abastecido com custo final pago"
    )
}
