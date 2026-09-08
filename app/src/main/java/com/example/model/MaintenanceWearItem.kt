package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WearAlertLevel(val label: String, val severity: Int) {
    OK("Em Dia", 0),
    HALF_LIFE("Metade Atingida (50%)", 1),
    WARNING("Troca Próxima", 2),
    CRITICAL("Vencido", 3)
}

enum class MaintenanceComponentType(
    val idKey: String,
    val defaultName: String,
    val category: String,
    val defaultIntervalKm: Double,
    val description: String
) {
    OLEO_MOTOR(
        idKey = "oleo_motor",
        defaultName = "Óleo do Motor",
        category = "Motor",
        defaultIntervalKm = 10000.0,
        description = "Lubrificante de proteção e refrigeração do motor"
    ),
    FILTRO_OLEO(
        idKey = "filtro_oleo",
        defaultName = "Filtro de Óleo do Motor",
        category = "Filtragem",
        defaultIntervalKm = 10000.0,
        description = "Retenção de limalhas e impurezas do lubrificante"
    ),
    FILTRO_COMBUSTIVEL(
        idKey = "filtro_combustivel",
        defaultName = "Filtro de Combustível",
        category = "Alimentação",
        defaultIntervalKm = 12000.0,
        description = "Retenção de partículas e sedimentos no combustível"
    ),
    FILTRO_FREIO(
        idKey = "filtro_freio",
        defaultName = "Filtro / Fluido de Freio",
        category = "Frenagem",
        defaultIntervalKm = 30000.0,
        description = "Fluido hidráulico e filtragem do sistema de freios"
    ),
    FILTRO_CAMBIO(
        idKey = "filtro_cambio",
        defaultName = "Filtro / Fluido de Câmbio",
        category = "Transmissão",
        defaultIntervalKm = 50000.0,
        description = "Fluido e filtro da transmissão mecânica/automática"
    );

    companion object {
        fun fromIdKey(key: String): MaintenanceComponentType? {
            return entries.firstOrNull { it.idKey.equals(key, ignoreCase = true) }
        }
    }
}

@Entity(tableName = "maintenance_tracker_items")
data class MaintenanceTrackerItem(
    @PrimaryKey val idKey: String,
    val name: String,
    val category: String,
    val recommendedIntervalKm: Double,
    val lastReplacedKm: Double,
    val lastReplacedDateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class MaintenanceWearStatus(
    val item: MaintenanceTrackerItem,
    val currentOdometerKm: Double,
    val kmDriven: Double,
    val remainingKm: Double,
    val depreciationPercentage: Double,
    val depreciationRatio: Float,
    val status: WearAlertLevel,
    val halfIntervalKm: Double = item.recommendedIntervalKm / 2.0,
    val reachedHalfInterval: Boolean = kmDriven >= (item.recommendedIntervalKm / 2.0)
) {
    val isAlertActive: Boolean get() = status != WearAlertLevel.OK
    val isCritical: Boolean get() = status == WearAlertLevel.CRITICAL
    val isWarning: Boolean get() = status == WearAlertLevel.WARNING
    val isHalfLife: Boolean get() = status == WearAlertLevel.HALF_LIFE
    val kmToHalfInterval: Double get() = (halfIntervalKm - kmDriven).coerceAtLeast(0.0)
    val kmAfterHalfInterval: Double get() = (kmDriven - halfIntervalKm).coerceAtLeast(0.0)
}

object MaintenanceWearCalculator {
    fun calculateStatus(
        item: MaintenanceTrackerItem,
        currentOdometerKm: Double
    ): MaintenanceWearStatus {
        val kmDriven = (currentOdometerKm - item.lastReplacedKm).coerceAtLeast(0.0)
        val interval = item.recommendedIntervalKm.coerceAtLeast(100.0)
        val halfInterval = interval / 2.0
        val remainingKm = interval - kmDriven
        val ratio = (kmDriven / interval).toFloat()
        val percentage = (ratio * 100.0).coerceAtLeast(0.0)

        val status = when {
            remainingKm <= 0.0 || ratio >= 1.0f -> WearAlertLevel.CRITICAL
            remainingKm <= 1500.0 || ratio >= 0.85f -> WearAlertLevel.WARNING
            kmDriven >= halfInterval || ratio >= 0.50f -> WearAlertLevel.HALF_LIFE
            else -> WearAlertLevel.OK
        }

        return MaintenanceWearStatus(
            item = item,
            currentOdometerKm = currentOdometerKm,
            kmDriven = kmDriven,
            remainingKm = remainingKm,
            depreciationPercentage = percentage,
            depreciationRatio = ratio.coerceIn(0f, 1.5f),
            status = status,
            halfIntervalKm = halfInterval,
            reachedHalfInterval = kmDriven >= halfInterval
        )
    }

    fun getDefaultItems(initialOdometerKm: Double = 0.0): List<MaintenanceTrackerItem> {
        val now = System.currentTimeMillis()
        val odo = initialOdometerKm.coerceAtLeast(0.0)

        return listOf(
            MaintenanceTrackerItem(
                idKey = MaintenanceComponentType.OLEO_MOTOR.idKey,
                name = MaintenanceComponentType.OLEO_MOTOR.defaultName,
                category = MaintenanceComponentType.OLEO_MOTOR.category,
                recommendedIntervalKm = 10000.0,
                lastReplacedKm = (odo - 400.0).coerceAtLeast(0.0),
                lastReplacedDateMillis = now - (5L * 24 * 3600 * 1000),
                notes = "Óleo sintético 5W30"
            ),
            MaintenanceTrackerItem(
                idKey = MaintenanceComponentType.FILTRO_OLEO.idKey,
                name = MaintenanceComponentType.FILTRO_OLEO.defaultName,
                category = MaintenanceComponentType.FILTRO_OLEO.category,
                recommendedIntervalKm = 10000.0,
                lastReplacedKm = (odo - 400.0).coerceAtLeast(0.0),
                lastReplacedDateMillis = now - (5L * 24 * 3600 * 1000),
                notes = "Filtro blindado original"
            ),
            MaintenanceTrackerItem(
                idKey = MaintenanceComponentType.FILTRO_COMBUSTIVEL.idKey,
                name = MaintenanceComponentType.FILTRO_COMBUSTIVEL.defaultName,
                category = MaintenanceComponentType.FILTRO_COMBUSTIVEL.category,
                recommendedIntervalKm = 12000.0,
                lastReplacedKm = (odo - 9800.0).coerceAtLeast(0.0),
                lastReplacedDateMillis = now - (60L * 24 * 3600 * 1000),
                notes = "Filtro de linha de combustível"
            ),
            MaintenanceTrackerItem(
                idKey = MaintenanceComponentType.FILTRO_FREIO.idKey,
                name = MaintenanceComponentType.FILTRO_FREIO.defaultName,
                category = MaintenanceComponentType.FILTRO_FREIO.category,
                recommendedIntervalKm = 30000.0,
                lastReplacedKm = (odo - 18000.0).coerceAtLeast(0.0),
                lastReplacedDateMillis = now - (180L * 24 * 3600 * 1000),
                notes = "Fluido DOT 4 e revisão de reservatório"
            ),
            MaintenanceTrackerItem(
                idKey = MaintenanceComponentType.FILTRO_CAMBIO.idKey,
                name = MaintenanceComponentType.FILTRO_CAMBIO.defaultName,
                category = MaintenanceComponentType.FILTRO_CAMBIO.category,
                recommendedIntervalKm = 50000.0,
                lastReplacedKm = (odo - 32000.0).coerceAtLeast(0.0),
                lastReplacedDateMillis = now - (300L * 24 * 3600 * 1000),
                notes = "Fluido e filtro da transmissão"
            )
        )
    }
}
