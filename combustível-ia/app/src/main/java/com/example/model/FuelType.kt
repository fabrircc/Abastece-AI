package com.example.model

enum class FuelType(val displayName: String, val shortName: String) {
    GASOLINA_COMUM("Gasolina Comum", "Gasolina"),
    GASOLINA_ADITIVADA("Gasolina Aditivada", "Aditivada"),
    ETANOL("Etanol (Álcool)", "Etanol"),
    DIESEL_S10("Diesel S10", "Diesel S10"),
    DIESEL_S500("Diesel S500", "Diesel S500"),
    GNV("GNV (Gás Natural)", "GNV"),
    ELETRICO("Elétrico (kWh)", "Elétrico");

    companion object {
        fun fromString(str: String?): FuelType {
            if (str.isNullOrBlank()) return GASOLINA_COMUM
            val lower = str.lowercase()
            return when {
                "etanol" in lower || "álcool" in lower || "alcool" in lower -> ETANOL
                "aditivada" in lower || "premium" in lower || "podium" in lower -> GASOLINA_ADITIVADA
                "diesel s10" in lower || "s10" in lower -> DIESEL_S10
                "diesel" in lower -> DIESEL_S500
                "gnv" in lower || "gás" in lower || "gas" in lower -> GNV
                "elétrico" in lower || "eletrico" in lower || "kwh" in lower -> ELETRICO
                else -> GASOLINA_COMUM
            }
        }
    }
}
