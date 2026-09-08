package com.example.ui.viewmodel

import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAssistant
import com.example.ai.ParsedAiAction
import com.example.data.AppDatabase
import com.example.data.FuelRepository
import com.example.model.ChartExportType
import com.example.model.ChatMessage
import com.example.model.ConsumptionPoint
import com.example.model.ExpenseCategoryBreakdown
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.FuelType
import com.example.model.MaintenanceComponentType
import com.example.model.MaintenanceEntry
import com.example.model.MaintenanceTrackerItem
import com.example.model.MaintenanceWearCalculator
import com.example.model.MaintenanceWearStatus
import com.example.model.PeriodFilter
import com.example.model.VehicleStats
import com.example.model.WearAlertLevel
import com.example.reports.ReportExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModel(application: Application) : AndroidViewModel(application) {// Inicializa o Firebase Authentication
    private val auth = FirebaseAuth.getInstance()

    // Estado do usuário logado (se for nulo, mostra a tela de login)
    var currentUser by mutableStateOf(auth.currentUser)
        private set

    // Estado para guardar mensagens de erro (ex: senha fraca, usuário não encontrado)
    var authError by mutableStateOf<String?>(null)
        private set

    fun loginUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            authError = "Preencha todos os campos."
            return
        }
        authError = null
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser = auth.currentUser
                } else {
                    authError = task.exception?.message ?: "Erro ao fazer login."
                }
            }
    }

    fun registerUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            authError = "Preencha todos os campos."
            return
        }
        authError = null
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser = auth.currentUser
                } else {
                    authError = task.exception?.message ?: "Erro ao criar conta."
                }
            }
    }

    fun logout() {
        auth.signOut()
        currentUser = null
    }

    private val database = AppDatabase.getInstance(application)
    private val repository = FuelRepository(database)
    private val geminiAssistant = GeminiAssistant()

    val allFuels: StateFlow<List<FuelEntry>> = repository.allFuelEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaintenance: StateFlow<List<MaintenanceEntry>> = repository.allMaintenance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFinances: StateFlow<List<FinanceEntry>> = repository.allFinances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.allChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrackerItems: StateFlow<List<MaintenanceTrackerItem>> = repository.allTrackerItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentVehicleOdometer: StateFlow<Double> = combine(allFuels, allMaintenance) { fuels, maints ->
        val maxFuel = fuels.maxOfOrNull { it.odometerKm } ?: 0.0
        val maxMaint = maints.maxOfOrNull { it.odometerKm } ?: 0.0
        maxOf(maxFuel, maxMaint).coerceAtLeast(45220.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 45220.0)

    val maintenanceWearStatuses: StateFlow<List<MaintenanceWearStatus>> = combine(
        repository.allTrackerItems,
        currentVehicleOdometer
    ) { items, currentOdo ->
        val targetItems = if (items.isEmpty()) {
            MaintenanceWearCalculator.getDefaultItems(currentOdo)
        } else items
        targetItems.map { MaintenanceWearCalculator.calculateStatus(it, currentOdo) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMaintenanceAlerts: StateFlow<List<MaintenanceWearStatus>> = maintenanceWearStatuses
        .combine(currentVehicleOdometer) { statuses, _ ->
            statuses.filter { it.isAlertActive }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val consumptionPoints: StateFlow<List<ConsumptionPoint>> = repository.getConsumptionPoints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPeriod = MutableStateFlow(PeriodFilter.MONTH)
    val selectedPeriod: StateFlow<PeriodFilter> = _selectedPeriod.asStateFlow()

    private val _selectedExportCharts = MutableStateFlow<Set<ChartExportType>>(ChartExportType.entries.toSet())
    val selectedExportCharts: StateFlow<Set<ChartExportType>> = _selectedExportCharts.asStateFlow()

    fun toggleExportChart(chartType: ChartExportType) {
        val current = _selectedExportCharts.value.toMutableSet()
        if (current.contains(chartType)) {
            if (current.size > 1) {
                current.remove(chartType)
            }
        } else {
            current.add(chartType)
        }
        _selectedExportCharts.value = current
    }

    fun selectAllExportCharts() {
        _selectedExportCharts.value = ChartExportType.entries.toSet()
    }

    fun clearExportCharts() {
        _selectedExportCharts.value = setOf(ChartExportType.COLUNAS)
    }

    private val _customStartDate = MutableStateFlow<Long>(System.currentTimeMillis() - 30L * 24 * 3600 * 1000)
    val customStartDate: StateFlow<Long> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow<Long>(System.currentTimeMillis())
    val customEndDate: StateFlow<Long> = _customEndDate.asStateFlow()

    val currentStats: StateFlow<VehicleStats> = combine(
        _selectedPeriod,
        _customStartDate,
        _customEndDate
    ) { period, start, end ->
        Triple(period, start, end)
    }.flatMapLatest { (period, start, end) ->
        repository.getCombinedStats(period, start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleStats())

    val expenseBreakdown: StateFlow<List<ExpenseCategoryBreakdown>> = combine(
        _selectedPeriod,
        _customStartDate,
        _customEndDate
    ) { period, start, end ->
        Triple(period, start, end)
    }.flatMapLatest { (period, start, end) ->
        repository.getExpenseBreakdown(period, start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    // Temporary pending entries stored in memory for actionable cards in chat
    private val _pendingFuelActions = MutableStateFlow<Map<Long, FuelEntry>>(emptyMap())
    val pendingFuelActions: StateFlow<Map<Long, FuelEntry>> = _pendingFuelActions.asStateFlow()

    private val _pendingMaintActions = MutableStateFlow<Map<Long, MaintenanceEntry>>(emptyMap())
    val pendingMaintActions: StateFlow<Map<Long, MaintenanceEntry>> = _pendingMaintActions.asStateFlow()

    private val _pendingFinanceActions = MutableStateFlow<Map<Long, FinanceEntry>>(emptyMap())
    val pendingFinanceActions: StateFlow<Map<Long, FinanceEntry>> = _pendingFinanceActions.asStateFlow()

    init {
        checkAndSeedInitialData()
    }

    private fun checkAndSeedInitialData() {
        viewModelScope.launch {
            val existingFuels = repository.allFuelEntries.firstOrNull() ?: emptyList()
            val existingChat = repository.allChatMessages.firstOrNull() ?: emptyList()

            if (existingChat.isEmpty()) {
                repository.insertChatMessage(
                    ChatMessage(
                        isUser = false,
                        message = "👋 Olá! Sou seu copiloto com IA para controle completo do seu veículo.\n\n" +
                                "Você pode falar comigo naturalmente por mensagem:\n" +
                                "• \"Abasteci 42 litros de gasolina por R$ 240 no Posto Shell com 45.200 km\"\n" +
                                "• \"Gastei R$ 180 em troca de óleo e filtro hoje\"\n" +
                                "• \"Receita de R$ 380 com corridas de app hoje\"\n" +
                                "• \"Qual é a média de km/l e gasto esse mês?\"\n\n" +
                                "Vou interpretar seus dados, calcular suas métricas e gerar relatórios completos em PDF e CSV!"
                    )
                )
            }

            if (existingFuels.isEmpty()) {
                // Seed realistic starter data
                val now = System.currentTimeMillis()
                val dayMillis = 24L * 3600 * 1000

                val f1 = FuelEntry(
                    dateMillis = now - (14 * dayMillis),
                    odometerKm = 44200.0,
                    liters = 42.0,
                    pricePerLiter = 5.79,
                    totalCost = 243.18,
                    fuelType = FuelType.GASOLINA_COMUM,
                    stationName = "Posto Shell Central",
                    isFullTank = true,
                    notes = "Abastecimento inicial para cálculo de média"
                )
                val f2 = FuelEntry(
                    dateMillis = now - (7 * dayMillis),
                    odometerKm = 44740.0, // 540 km traveled / 42 L = 12.85 km/L
                    liters = 42.0,
                    pricePerLiter = 5.75,
                    totalCost = 241.50,
                    fuelType = FuelType.GASOLINA_COMUM,
                    stationName = "Posto Ipiranga",
                    isFullTank = true,
                    notes = "Média de estrada e cidade"
                )
                val f3 = FuelEntry(
                    dateMillis = now - (1 * dayMillis),
                    odometerKm = 45220.0, // 480 km traveled / 39 L = 12.31 km/L
                    liters = 39.0,
                    pricePerLiter = 3.99,
                    totalCost = 155.61,
                    fuelType = FuelType.ETANOL,
                    stationName = "Posto Petrobras BR",
                    isFullTank = true,
                    notes = "Teste com etanol na cidade"
                )
                repository.insertFuel(f1)
                repository.insertFuel(f2)
                repository.insertFuel(f3)

                val m1 = MaintenanceEntry(
                    dateMillis = now - (5 * dayMillis),
                    odometerKm = 44800.0,
                    serviceType = "Troca de Óleo 5W30 e Filtros",
                    workshopName = "Oficina AutoTech",
                    cost = 210.0,
                    notes = "Revisão periódica"
                )
                repository.insertMaintenance(m1)

                val fin1 = FinanceEntry(
                    dateMillis = now - (2 * dayMillis),
                    type = FinanceType.RECEITA,
                    category = "Corridas App / Fretes",
                    amount = 450.0,
                    notes = "Faturamento semanal"
                )
                val fin2 = FinanceEntry(
                    dateMillis = now - (3 * dayMillis),
                    type = FinanceType.OUTRO_GASTO,
                    category = "Pedágio",
                    amount = 32.40,
                    notes = "Viagem rodovia"
                )
                repository.insertFinance(fin1)
                repository.insertFinance(fin2)
            }

            // Ensure tracker items exist
            val existingTracker = repository.allTrackerItems.firstOrNull() ?: emptyList()
            if (existingTracker.isEmpty()) {
                repository.ensureDefaultTrackerItems(45220.0)
            }
        }
    }

    fun recordWearReplacement(
        idKey: String,
        odometerKm: Double,
        cost: Double? = null,
        workshop: String = "",
        notes: String = "",
        createMaintenanceEntry: Boolean = true
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.updateTrackerItemReplacement(idKey, odometerKm, now)

            if (createMaintenanceEntry && cost != null && cost > 0) {
                val item = repository.allTrackerItems.firstOrNull()?.find { it.idKey == idKey }
                val serviceTitle = "Troca de " + (item?.name ?: "Componente")
                val entry = MaintenanceEntry(
                    dateMillis = now,
                    odometerKm = odometerKm,
                    serviceType = serviceTitle,
                    workshopName = workshop,
                    cost = cost,
                    notes = notes.ifBlank { "Substituição preventiva por quilometragem ($odometerKm km)" }
                )
                repository.insertMaintenance(entry)
            }
        }
    }

    fun updateWearInterval(idKey: String, intervalKm: Double) {
        viewModelScope.launch {
            repository.updateTrackerItemInterval(idKey, intervalKm)
        }
    }

    fun setPeriod(period: PeriodFilter) {
        _selectedPeriod.value = period
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _customStartDate.value = startMillis
        _customEndDate.value = endMillis
        _selectedPeriod.value = PeriodFilter.CUSTOM
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isGeneratingAi.value) return

        viewModelScope.launch {
            // Save user message to chat
            val userMsg = ChatMessage(
                isUser = true,
                message = trimmed,
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(userMsg)

            _isGeneratingAi.value = true

            // Get context statistics and latest odometer for AI prompt
            val latestOdo = repository.getLatestFuelEntry()?.odometerKm ?: 0.0
            val stats = currentStats.value
            val wearSummary = maintenanceWearStatuses.value.joinToString("; ") {
                val halfNotice = if (it.status == WearAlertLevel.HALF_LIFE) " [⚠️ METADE DO INTERVALO: inspeção recomendada]" else ""
                "${it.item.name}: ${String.format(Locale("pt", "BR"), "%.0f", it.depreciationPercentage)}% desgaste (${if (it.remainingKm >= 0) "faltam ${it.remainingKm.toInt()} km" else "vencido por ${(-it.remainingKm).toInt()} km"}, status: ${it.status.label}$halfNotice)"
            }

            val statsSummary = "Média atual: ${String.format(Locale("pt", "BR"), "%.2f", stats.avgKmPerLiter)} km/L, " +
                    "Custo por km: R$ ${String.format(Locale("pt", "BR"), "%.3f", stats.avgCostPerKm)}, " +
                    "Total combustível: R$ ${String.format(Locale("pt", "BR"), "%.2f", stats.totalFuelCost)}, " +
                    "Total manutenção: R$ ${String.format(Locale("pt", "BR"), "%.2f", stats.totalMaintenanceCost)}, " +
                    "Total receitas: R$ ${String.format(Locale("pt", "BR"), "%.2f", stats.totalRevenues)}. " +
                    "Status de Filtros e Óleo: $wearSummary"

            val result: ParsedAiAction = geminiAssistant.processUserMessage(trimmed, latestOdo, statsSummary)

            _isGeneratingAi.value = false

            // Save assistant reply to chat
            val botMsg = ChatMessage(
                isUser = false,
                message = result.replyText,
                timestamp = System.currentTimeMillis(),
                actionType = result.actionType
            )
            val botMsgId = repository.insertChatMessage(botMsg)

            // Attach pending actions
            result.fuelEntry?.let {
                _pendingFuelActions.value = _pendingFuelActions.value + (botMsgId to it)
            }
            result.maintenanceEntry?.let {
                _pendingMaintActions.value = _pendingMaintActions.value + (botMsgId to it)
            }
            result.financeEntry?.let {
                _pendingFinanceActions.value = _pendingFinanceActions.value + (botMsgId to it)
            }
        }
    }

    fun confirmPendingFuel(messageId: Long) {
        val entry = _pendingFuelActions.value[messageId] ?: return
        viewModelScope.launch {
            repository.insertFuel(entry)
            _pendingFuelActions.value = _pendingFuelActions.value - messageId
            repository.insertChatMessage(
                ChatMessage(
                    isUser = false,
                    message = "✅ Abastecimento de **${entry.fuelType.displayName}** (${String.format(Locale("pt", "BR"), "%.2f L", entry.liters)}) no valor de **R$ ${String.format(Locale("pt", "BR"), "%.2f", entry.totalCost)}** foi registrado com sucesso!",
                    actionConfirmed = true
                )
            )
        }
    }

    fun confirmPendingMaintenance(messageId: Long) {
        val entry = _pendingMaintActions.value[messageId] ?: return
        viewModelScope.launch {
            repository.insertMaintenance(entry)
            _pendingMaintActions.value = _pendingMaintActions.value - messageId
            repository.insertChatMessage(
                ChatMessage(
                    isUser = false,
                    message = "✅ Manutenção **${entry.serviceType}** no valor de **R$ ${String.format(Locale("pt", "BR"), "%.2f", entry.cost)}** foi registrada com sucesso!",
                    actionConfirmed = true
                )
            )
        }
    }

    fun confirmPendingFinance(messageId: Long) {
        val entry = _pendingFinanceActions.value[messageId] ?: return
        viewModelScope.launch {
            repository.insertFinance(entry)
            _pendingFinanceActions.value = _pendingFinanceActions.value - messageId
            val typeStr = if (entry.type == FinanceType.RECEITA) "Receita" else "Despesa"
            repository.insertChatMessage(
                ChatMessage(
                    isUser = false,
                    message = "✅ $typeStr de **${entry.category}** (R$ ${String.format(Locale("pt", "BR"), "%.2f", entry.amount)}) foi adicionada com sucesso!",
                    actionConfirmed = true
                )
            )
        }
    }

    fun dismissPendingAction(messageId: Long) {
        _pendingFuelActions.value = _pendingFuelActions.value - messageId
        _pendingMaintActions.value = _pendingMaintActions.value - messageId
        _pendingFinanceActions.value = _pendingFinanceActions.value - messageId
    }

    // Manual CRUD
    fun addFuel(
        odometer: Double,
        liters: Double,
        pricePerLiter: Double,
        totalCost: Double,
        fuelType: FuelType,
        station: String,
        isFullTank: Boolean,
        dateMillis: Long,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertFuel(
                FuelEntry(
                    dateMillis = dateMillis,
                    odometerKm = odometer,
                    liters = liters,
                    pricePerLiter = pricePerLiter,
                    totalCost = totalCost,
                    fuelType = fuelType,
                    stationName = station,
                    isFullTank = isFullTank,
                    notes = notes
                )
            )
        }
    }

    fun addMaintenance(
        odometer: Double,
        serviceType: String,
        workshop: String,
        cost: Double,
        dateMillis: Long,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertMaintenance(
                MaintenanceEntry(
                    dateMillis = dateMillis,
                    odometerKm = odometer,
                    serviceType = serviceType,
                    workshopName = workshop,
                    cost = cost,
                    notes = notes
                )
            )
        }
    }

    fun addFinance(
        type: FinanceType,
        category: String,
        amount: Double,
        dateMillis: Long,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertFinance(
                FinanceEntry(
                    dateMillis = dateMillis,
                    type = type,
                    category = category,
                    amount = amount,
                    notes = notes
                )
            )
        }
    }

    fun deleteFuel(entry: FuelEntry) {
        viewModelScope.launch { repository.deleteFuel(entry) }
    }

    fun deleteMaintenance(entry: MaintenanceEntry) {
        viewModelScope.launch { repository.deleteMaintenance(entry) }
    }

    fun deleteFinance(entry: FinanceEntry) {
        viewModelScope.launch { repository.deleteFinance(entry) }
    }

    fun clearAllChat() {
        viewModelScope.launch { repository.clearChat() }
    }

    fun exportPdf(context: Context) {
        viewModelScope.launch {
            val period = _selectedPeriod.value
            val (start, end) = FuelRepository.getFilterDateRange(period, _customStartDate.value, _customEndDate.value)
            val fuels = allFuels.value.filter { it.dateMillis in start..end }
            val maints = allMaintenance.value.filter { it.dateMillis in start..end }
            val fins = allFinances.value.filter { it.dateMillis in start..end }
            val stats = currentStats.value
            val charts = _selectedExportCharts.value

            val file = ReportExporter.exportPdf(
                context = context,
                periodLabel = period.label,
                stats = stats,
                fuels = fuels,
                maintenances = maints,
                finances = fins,
                selectedCharts = charts,
                wearStatuses = maintenanceWearStatuses.value
            )

            ReportExporter.shareFile(context, file, "application/pdf", "Exportar Relatório em PDF")
        }
    }

    fun exportXls(context: Context) {
        viewModelScope.launch {
            val period = _selectedPeriod.value
            val (start, end) = FuelRepository.getFilterDateRange(period, _customStartDate.value, _customEndDate.value)
            val fuels = allFuels.value.filter { it.dateMillis in start..end }
            val maints = allMaintenance.value.filter { it.dateMillis in start..end }
            val fins = allFinances.value.filter { it.dateMillis in start..end }
            val stats = currentStats.value
            val charts = _selectedExportCharts.value

            val file = ReportExporter.exportXls(
                context = context,
                periodLabel = period.label,
                stats = stats,
                fuels = fuels,
                maintenances = maints,
                finances = fins,
                selectedCharts = charts,
                wearStatuses = maintenanceWearStatuses.value
            )

            ReportExporter.shareFile(context, file, "application/vnd.ms-excel", "Exportar Planilha Excel (.XLS)")
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            val period = _selectedPeriod.value
            val (start, end) = FuelRepository.getFilterDateRange(period, _customStartDate.value, _customEndDate.value)
            val fuels = allFuels.value.filter { it.dateMillis in start..end }
            val maints = allMaintenance.value.filter { it.dateMillis in start..end }
            val fins = allFinances.value.filter { it.dateMillis in start..end }
            val stats = currentStats.value

            val file = ReportExporter.exportCsv(
                context = context,
                periodLabel = period.label,
                stats = stats,
                fuels = fuels,
                maintenances = maints,
                finances = fins,
                wearStatuses = maintenanceWearStatuses.value
            )

            ReportExporter.shareFile(context, file, "text/csv", "Exportar Dados em CSV")
        }
    }
}
