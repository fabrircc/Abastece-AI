package com.example.data

import com.example.model.ChatMessage
import com.example.model.ConsumptionPoint
import com.example.model.ExpenseCategoryBreakdown
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.MaintenanceComponentType
import com.example.model.MaintenanceEntry
import com.example.model.MaintenanceTrackerItem
import com.example.model.MaintenanceWearCalculator
import com.example.model.PeriodFilter
import com.example.model.VehicleStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class FuelRepository(private val database: AppDatabase) {
    private val fuelDao = database.fuelDao()
    private val maintenanceDao = database.maintenanceDao()
    private val financeDao = database.financeDao()
    private val chatDao = database.chatDao()
    private val trackerDao = database.maintenanceTrackerDao()

    val allFuelEntries: Flow<List<FuelEntry>> = fuelDao.getAllFuelEntries()
    val allFuelEntriesAsc: Flow<List<FuelEntry>> = fuelDao.getAllFuelEntriesAsc()
    val allMaintenance: Flow<List<MaintenanceEntry>> = maintenanceDao.getAllMaintenance()
    val allFinances: Flow<List<FinanceEntry>> = financeDao.getAllFinances()
    val allChatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()
    val allTrackerItems: Flow<List<MaintenanceTrackerItem>> = trackerDao.getAllItems()

    suspend fun insertFuel(entry: FuelEntry): Long = fuelDao.insertFuel(entry)
    suspend fun updateFuel(entry: FuelEntry) = fuelDao.updateFuel(entry)
    suspend fun deleteFuel(entry: FuelEntry) = fuelDao.deleteFuel(entry)
    suspend fun deleteFuelById(id: Long) = fuelDao.deleteFuelById(id)
    suspend fun getLatestFuelEntry(): FuelEntry? = fuelDao.getLatestFuelEntry()

    suspend fun insertMaintenance(entry: MaintenanceEntry): Long {
        val id = maintenanceDao.insertMaintenance(entry)
        autoSyncTrackerFromMaintenance(entry)
        return id
    }
    suspend fun updateMaintenance(entry: MaintenanceEntry) = maintenanceDao.updateMaintenance(entry)
    suspend fun deleteMaintenance(entry: MaintenanceEntry) = maintenanceDao.deleteMaintenance(entry)
    suspend fun deleteMaintenanceById(id: Long) = maintenanceDao.deleteMaintenanceById(id)

    suspend fun insertFinance(entry: FinanceEntry): Long = financeDao.insertFinance(entry)
    suspend fun updateFinance(entry: FinanceEntry) = financeDao.updateFinance(entry)
    suspend fun deleteFinance(entry: FinanceEntry) = financeDao.deleteFinance(entry)
    suspend fun deleteFinanceById(id: Long) = financeDao.deleteFinanceById(id)

    suspend fun insertChatMessage(message: ChatMessage): Long = chatDao.insertMessage(message)
    suspend fun updateChatMessage(message: ChatMessage) = chatDao.updateMessage(message)
    suspend fun clearChat() = chatDao.clearChat()

    suspend fun ensureDefaultTrackerItems(initialOdometerKm: Double) {
        val defaults = MaintenanceWearCalculator.getDefaultItems(initialOdometerKm)
        trackerDao.insertAll(defaults)
    }

    suspend fun updateTrackerItemReplacement(idKey: String, km: Double, dateMillis: Long = System.currentTimeMillis()) {
        trackerDao.updateReplacement(idKey, km, dateMillis)
    }

    suspend fun updateTrackerItemInterval(idKey: String, intervalKm: Double) {
        trackerDao.updateInterval(idKey, intervalKm)
    }

    suspend fun upsertTrackerItem(item: MaintenanceTrackerItem) {
        trackerDao.upsertItem(item)
    }

    private suspend fun autoSyncTrackerFromMaintenance(entry: MaintenanceEntry) {
        val text = "${entry.serviceType} ${entry.notes}".lowercase()
        val km = entry.odometerKm
        val date = entry.dateMillis

        if (km <= 0.0) return

        if (text.contains("óleo") || text.contains("oleo") || text.contains("lubrificante")) {
            trackerDao.updateReplacement(MaintenanceComponentType.OLEO_MOTOR.idKey, km, date)
            if (text.contains("filtro") || text.contains("filtros")) {
                trackerDao.updateReplacement(MaintenanceComponentType.FILTRO_OLEO.idKey, km, date)
            }
        }
        if (text.contains("filtro de óleo") || text.contains("filtro de oleo") || text.contains("filtro oleo")) {
            trackerDao.updateReplacement(MaintenanceComponentType.FILTRO_OLEO.idKey, km, date)
        }
        if (text.contains("combustível") || text.contains("combustivel") || text.contains("filtro combust")) {
            trackerDao.updateReplacement(MaintenanceComponentType.FILTRO_COMBUSTIVEL.idKey, km, date)
        }
        if (text.contains("freio") || text.contains("pastilha") || text.contains("disco") || text.contains("fluido de freio")) {
            trackerDao.updateReplacement(MaintenanceComponentType.FILTRO_FREIO.idKey, km, date)
        }
        if (text.contains("câmbio") || text.contains("cambio") || text.contains("transmissão") || text.contains("transmissao")) {
            trackerDao.updateReplacement(MaintenanceComponentType.FILTRO_CAMBIO.idKey, km, date)
        }
    }

    // Observes combined stats based on period
    fun getCombinedStats(
        filter: PeriodFilter,
        customStartMillis: Long = 0L,
        customEndMillis: Long = Long.MAX_VALUE
    ): Flow<VehicleStats> {
        val (start, end) = getFilterDateRange(filter, customStartMillis, customEndMillis)

        return combine(
            allFuelEntriesAsc,
            allMaintenance,
            allFinances
        ) { fuelsAsc, maintenances, finances ->
            val periodFuels = fuelsAsc.filter { it.dateMillis in start..end }
            val periodMaintenance = maintenances.filter { it.dateMillis in start..end }
            val periodFinances = finances.filter { it.dateMillis in start..end }

            calculateStats(periodFuels, fuelsAsc, periodMaintenance, periodFinances)
        }
    }

    // Computes timeline consumption points for charting
    fun getConsumptionPoints(): Flow<List<ConsumptionPoint>> {
        return allFuelEntriesAsc.combine(allFuelEntries) { ascList, _ ->
            val points = mutableListOf<ConsumptionPoint>()
            if (ascList.size >= 2) {
                for (i in 1 until ascList.size) {
                    val prev = ascList[i - 1]
                    val curr = ascList[i]
                    val distance = curr.odometerKm - prev.odometerKm
                    if (distance > 0 && curr.liters > 0) {
                        val kmPerL = distance / curr.liters
                        val costPerKm = curr.totalCost / distance
                        points.add(
                            ConsumptionPoint(
                                dateMillis = curr.dateMillis,
                                kmPerLiter = kmPerL,
                                costPerKm = costPerKm,
                                fuelType = curr.fuelType,
                                odometerKm = curr.odometerKm,
                                liters = curr.liters,
                                station = curr.stationName
                            )
                        )
                    }
                }
            }
            points
        }
    }

    // Breakdowns of expenses
    fun getExpenseBreakdown(filter: PeriodFilter, customStart: Long = 0L, customEnd: Long = Long.MAX_VALUE): Flow<List<ExpenseCategoryBreakdown>> {
        val (start, end) = getFilterDateRange(filter, customStart, customEnd)

        return combine(allFuelEntries, allMaintenance, allFinances) { fuels, maintenances, finances ->
            val fuelCost = fuels.filter { it.dateMillis in start..end }.sumOf { it.totalCost }
            val maintCost = maintenances.filter { it.dateMillis in start..end }.sumOf { it.cost }
            val otherExpenses = finances
                .filter { it.type == FinanceType.OUTRO_GASTO && it.dateMillis in start..end }

            val total = fuelCost + maintCost + otherExpenses.sumOf { it.amount }
            if (total <= 0.0) return@combine emptyList<ExpenseCategoryBreakdown>()

            val list = mutableListOf<ExpenseCategoryBreakdown>()
            if (fuelCost > 0) {
                list.add(ExpenseCategoryBreakdown("Combustível", fuelCost, (fuelCost / total).toFloat()))
            }
            if (maintCost > 0) {
                list.add(ExpenseCategoryBreakdown("Manutenção", maintCost, (maintCost / total).toFloat()))
            }

            // Group other expenses by category (e.g. Pedágio, Seguro, Estacionamento)
            val groupedOthers = otherExpenses.groupBy { it.category }
            for ((cat, items) in groupedOthers) {
                val catSum = items.sumOf { it.amount }
                if (catSum > 0) {
                    list.add(ExpenseCategoryBreakdown(cat.ifBlank { "Outros" }, catSum, (catSum / total).toFloat()))
                }
            }

            list.sortedByDescending { it.totalAmount }
        }
    }

    companion object {
        fun getFilterDateRange(
            filter: PeriodFilter,
            customStartMillis: Long = 0L,
            customEndMillis: Long = Long.MAX_VALUE
        ): Pair<Long, Long> {
            val cal = Calendar.getInstance()
            return when (filter) {
                PeriodFilter.TODAY -> {
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    Pair(start, end)
                }
                PeriodFilter.WEEK -> {
                    // Last 7 days to end of today
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, -6)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    Pair(start, end)
                }
                PeriodFilter.MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis

                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    Pair(start, end)
                }
                PeriodFilter.YEAR -> {
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis

                    cal.set(Calendar.MONTH, Calendar.DECEMBER)
                    cal.set(Calendar.DAY_OF_MONTH, 31)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    Pair(start, end)
                }
                PeriodFilter.CUSTOM -> {
                    Pair(customStartMillis, customEndMillis)
                }
                PeriodFilter.ALL -> {
                    Pair(0L, Long.MAX_VALUE)
                }
            }
        }

        fun calculateStats(
            periodFuels: List<FuelEntry>,
            allFuelsAsc: List<FuelEntry>,
            periodMaintenance: List<MaintenanceEntry>,
            periodFinances: List<FinanceEntry>
        ): VehicleStats {
            val totalFuelCost = periodFuels.sumOf { it.totalCost }
            val totalLiters = periodFuels.sumOf { it.liters }
            val avgPricePerLiter = if (totalLiters > 0) totalFuelCost / totalLiters else 0.0

            // Distance calculation:
            // For the fuels in this period, we locate their previous entries in allFuelsAsc
            var totalDistance = 0.0
            var validConsumptionLiters = 0.0
            var consumptionCostSum = 0.0

            if (allFuelsAsc.size >= 2) {
                for (i in 1 until allFuelsAsc.size) {
                    val curr = allFuelsAsc[i]
                    val prev = allFuelsAsc[i - 1]
                    if (curr in periodFuels) {
                        val deltaKm = curr.odometerKm - prev.odometerKm
                        if (deltaKm > 0) {
                            totalDistance += deltaKm
                            if (curr.liters > 0) {
                                validConsumptionLiters += curr.liters
                                consumptionCostSum += curr.totalCost
                            }
                        }
                    }
                }
            }

            val avgKmPerLiter = if (validConsumptionLiters > 0 && totalDistance > 0) {
                totalDistance / validConsumptionLiters
            } else if (totalLiters > 0 && totalDistance > 0) {
                totalDistance / totalLiters
            } else 0.0

            val avgCostPerKm = if (totalDistance > 0 && totalFuelCost > 0) {
                totalFuelCost / totalDistance
            } else 0.0

            val totalMaintenance = periodMaintenance.sumOf { it.cost }
            val totalOtherExpenses = periodFinances
                .filter { it.type == FinanceType.OUTRO_GASTO }
                .sumOf { it.amount }
            val totalExpenses = totalFuelCost + totalMaintenance + totalOtherExpenses

            val totalRevenues = periodFinances
                .filter { it.type == FinanceType.RECEITA }
                .sumOf { it.amount }

            val netBalance = totalRevenues - totalExpenses

            return VehicleStats(
                totalDistanceKm = totalDistance,
                totalLiters = totalLiters,
                totalFuelCost = totalFuelCost,
                avgKmPerLiter = avgKmPerLiter,
                avgCostPerKm = avgCostPerKm,
                avgPricePerLiter = avgPricePerLiter,
                totalMaintenanceCost = totalMaintenance,
                totalOtherExpenses = totalOtherExpenses,
                totalExpenses = totalExpenses,
                totalRevenues = totalRevenues,
                netBalance = netBalance,
                fuelCount = periodFuels.size,
                maintenanceCount = periodMaintenance.size,
                revenueCount = periodFinances.count { it.type == FinanceType.RECEITA },
                otherExpensesCount = periodFinances.count { it.type == FinanceType.OUTRO_GASTO }
            )
        }
    }
}
