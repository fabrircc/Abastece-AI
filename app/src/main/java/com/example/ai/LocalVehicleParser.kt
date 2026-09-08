package com.example.ai

import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.FuelType
import com.example.model.MaintenanceEntry
import java.util.Locale
import java.util.regex.Pattern

data class ParsedAiAction(
    val replyText: String,
    val actionType: String? = null, // "FUEL", "MAINTENANCE", "FINANCE"
    val fuelEntry: FuelEntry? = null,
    val maintenanceEntry: MaintenanceEntry? = null,
    val financeEntry: FinanceEntry? = null
)

object LocalVehicleParser {

    fun parseMessage(
        userMessage: String,
        latestOdometer: Double = 0.0,
        currentStatsSummary: String = ""
    ): ParsedAiAction {
        val lower = userMessage.lowercase(Locale.ROOT)

        // 1. Detect Intent: Queries vs Records
        val isQueryAverage = lower.contains("média") || lower.contains("media") || lower.contains("km/l") || lower.contains("consumo")
        val isQueryParity = (lower.contains("álcool") || lower.contains("alcool") || lower.contains("etanol")) &&
                (lower.contains("gasolina") || lower.contains("compensa") || lower.contains("vale a pena"))
        val isQueryExpenses = (lower.contains("quanto gastei") || lower.contains("total gasto") || lower.contains("finança") || lower.contains("resumo"))

        if (isQueryParity) {
            return ParsedAiAction(
                replyText = "💡 **Regra de Paridade (Etanol x Gasolina):**\n\n" +
                        "Historicamente, o etanol é vantajoso se o preço por litro for de até **70%** do valor da gasolina (Preço Etanol ÷ Preço Gasolina ≤ 0,70).\n\n" +
                        "👉 Exemplo: Se a gasolina custa R$ 5,90, o etanol vale a pena se estiver abaixo de **R$ 4,13**.\n" +
                        "Você pode verificar a paridade exata e o histórico do seu veículo na aba **Métricas**!"
            )
        }

        if (isQueryAverage || isQueryExpenses) {
            val statsInfo = if (currentStatsSummary.isNotBlank()) {
                "\n\n📊 **Seus dados registrados:**\n$currentStatsSummary"
            } else {
                "\n\nVocê ainda não possui abastecimentos suficientes para calcular médias. Registre ao menos 2 abastecimentos com odômetro para ver seu consumo exato em km/L!"
            }
            return ParsedAiAction(
                replyText = "Aqui está um resumo do seu controle veicular:$statsInfo\n\nVocê pode alternar os períodos (Dia, Semana, Mês, Ano) na aba **Métricas** e exportar relatórios em **PDF e CSV**!"
            )
        }

        // Query maintenance wear & filters diagnosis
        val isQueryWear = (lower.contains("filtro") || lower.contains("óleo") || lower.contains("oleo") || lower.contains("desgaste") || lower.contains("alerta") || lower.contains("câmbio") || lower.contains("cambio") || lower.contains("freio")) &&
                (lower.contains("como") || lower.contains("quando") || lower.contains("status") || lower.contains("qual") || lower.contains("alerta") || lower.contains("vencid") || lower.contains("situa") || lower.contains("trocar") || lower.contains("revis")) &&
                !lower.contains("gastei") && !lower.contains("troquei") && !lower.contains("paguei") && !lower.contains("fiz")

        if (isQueryWear) {
            val wearSummaryPart = if (currentStatsSummary.contains("Status de Filtros e Óleo:")) {
                currentStatsSummary.substringAfter("Status de Filtros e Óleo:").trim()
            } else {
                "• Óleo do Motor (10.000 km)\n• Filtro de Óleo (10.000 km)\n• Filtro de Combustível (12.000 km)\n• Filtro/Fluido de Freio (30.000 km)\n• Filtro/Fluido de Câmbio (50.000 km)"
            }

            return ParsedAiAction(
                replyText = "🛠️ **Diagnóstico de Desgaste e Alertas de Troca:**\n\n" +
                        "Acompanho o desgaste contínuo por quilometragem percorrida dos 5 itens essenciais:\n\n" +
                        "📋 **Situação Atual:**\n" +
                        wearSummaryPart.split("; ").joinToString("\n") { "• $it" } +
                        "\n\n💡 Você pode registrar trocas a qualquer momento na aba **Métricas** ou me enviar mensagens como: *\"Troquei o óleo de motor com 45.500 km por R$ 190\"*."
            )
        }

        // 2. Detect Maintenance Intent
        val maintenanceKeywords = listOf("óleo", "oleo", "revisão", "revisao", "freio", "pastilha", "pneu", "alinhamento", "balanceamento", "filtro", "vela", "correia", "bateria", "suspensão", "mantenção", "manutenção", "mecânica", "oficina")
        val isMaintenance = maintenanceKeywords.any { lower.contains(it) }

        // 3. Detect Revenue Intent
        val revenueKeywords = listOf("receita", "ganhei", "faturei", "corrida", "uber", "99", "inDrive", "frete", "entrega", "gorjeta")
        val isRevenue = revenueKeywords.any { lower.contains(it) } && !isMaintenance

        // 4. Detect Other Expenses Intent
        val otherExpensesKeywords = listOf("pedágio", "pedagio", "estacionamento", "lavagem", "lava-rápido", "ipva", "seguro", "licenciamento", "multa")
        val isOtherExpense = otherExpensesKeywords.any { lower.contains(it) }

        // Extract Common Entities
        val amounts = extractMoneyAmounts(userMessage)
        val odometer = extractOdometer(userMessage, latestOdometer)

        // Branch: Maintenance
        if (isMaintenance) {
            val cost = amounts.firstOrNull() ?: 0.0
            val serviceName = extractMaintenanceService(lower)
            val workshop = extractWorkshop(userMessage)

            val entry = MaintenanceEntry(
                odometerKm = odometer,
                serviceType = serviceName,
                workshopName = workshop,
                cost = cost,
                notes = userMessage
            )

            val costStr = if (cost > 0) "R$ ${String.format(Locale("pt", "BR"), "%.2f", cost)}" else "a definir"
            val odoStr = if (odometer > 0) "${odometer.toInt()} km" else "não informado"

            return ParsedAiAction(
                replyText = "🔧 **Manutenção identificada!**\n\n" +
                        "• Serviço: **$serviceName**\n" +
                        "• Valor: **$costStr**\n" +
                        "• Odômetro: **$odoStr**\n" +
                        (if (workshop.isNotBlank()) "• Oficina: **$workshop**\n" else "") +
                        "\nClique em **Confirmar Registro** abaixo para salvar na sua base de dados.",
                actionType = "MAINTENANCE",
                maintenanceEntry = entry
            )
        }

        // Branch: Revenue
        if (isRevenue) {
            val amount = amounts.firstOrNull() ?: 0.0
            val category = if (lower.contains("uber")) "Corrida Uber"
            else if (lower.contains("99")) "Corrida 99"
            else if (lower.contains("frete")) "Frete"
            else if (lower.contains("entrega")) "Entregas"
            else "Receita de Corridas"

            val entry = FinanceEntry(
                type = FinanceType.RECEITA,
                category = category,
                amount = amount,
                notes = userMessage
            )

            val amtStr = String.format(Locale("pt", "BR"), "%.2f", amount)
            return ParsedAiAction(
                replyText = "💰 **Receita identificada!**\n\n" +
                        "• Categoria: **$category**\n" +
                        "• Valor recebido: **R$ $amtStr**\n\n" +
                        "Deseja adicionar esse ganho ao seu saldo veicular?",
                actionType = "FINANCE",
                financeEntry = entry
            )
        }

        // Branch: Other Expense
        if (isOtherExpense) {
            val amount = amounts.firstOrNull() ?: 0.0
            val category = when {
                lower.contains("pedágio") || lower.contains("pedagio") -> "Pedágio"
                lower.contains("estacionamento") -> "Estacionamento"
                lower.contains("lavagem") || lower.contains("lava") -> "Lavagem"
                lower.contains("ipva") -> "IPVA"
                lower.contains("seguro") -> "Seguro"
                lower.contains("multa") -> "Multa"
                else -> "Outro Gasto"
            }

            val entry = FinanceEntry(
                type = FinanceType.OUTRO_GASTO,
                category = category,
                amount = amount,
                notes = userMessage
            )

            val amtStr = String.format(Locale("pt", "BR"), "%.2f", amount)
            return ParsedAiAction(
                replyText = "📋 **Despesa veicular identificada!**\n\n" +
                        "• Categoria: **$category**\n" +
                        "• Valor: **R$ $amtStr**\n\n" +
                        "Deseja registrar essa despesa nas suas finanças?",
                actionType = "FINANCE",
                financeEntry = entry
            )
        }

        // Default or Explicit Branch: Fueling
        val liters = extractLiters(userMessage)
        val fuelType = FuelType.fromString(userMessage)
        val station = extractStation(userMessage)
        val isFullTank = !lower.contains("não enchi") && !lower.contains("não foi tanque cheio") && !lower.contains("parcial")

        // Money: could be total cost or price per liter
        var totalCost = 0.0
        var pricePerLiter = 0.0

        if (amounts.size >= 2) {
            val maxAmt = amounts.maxOrNull() ?: 0.0
            val minAmt = amounts.minOrNull() ?: 0.0
            if (minAmt in 2.0..15.0 && maxAmt > 20.0) {
                pricePerLiter = minAmt
                totalCost = maxAmt
            } else {
                totalCost = maxAmt
            }
        } else if (amounts.isNotEmpty()) {
            val single = amounts.first()
            if (single in 2.0..15.0 && liters > 0) {
                pricePerLiter = single
                totalCost = pricePerLiter * liters
            } else {
                totalCost = single
                if (liters > 0) {
                    pricePerLiter = totalCost / liters
                }
            }
        }

        if (pricePerLiter == 0.0 && liters > 0 && totalCost > 0) {
            pricePerLiter = totalCost / liters
        }

        val finalOdometer = if (odometer > 0) odometer else latestOdometer

        val fuelEntry = FuelEntry(
            odometerKm = finalOdometer,
            liters = liters,
            pricePerLiter = pricePerLiter,
            totalCost = totalCost,
            fuelType = fuelType,
            stationName = station,
            isFullTank = isFullTank,
            notes = userMessage
        )

        val litersStr = if (liters > 0) String.format(Locale("pt", "BR"), "%.2f L", liters) else "não informado"
        val costStr = if (totalCost > 0) "R$ ${String.format(Locale("pt", "BR"), "%.2f", totalCost)}" else "não informado"
        val priceStr = if (pricePerLiter > 0) "R$ ${String.format(Locale("pt", "BR"), "%.3f", pricePerLiter)}/L" else "a calcular"
        val odoStr = if (finalOdometer > 0) "${finalOdometer.toInt()} km" else "não informado"
        val stationStr = if (station.isNotBlank()) "• Posto: **$station**\n" else ""

        return ParsedAiAction(
            replyText = "⛽ **Abastecimento compreendido!**\n\n" +
                    "• Combustível: **${fuelType.displayName}**\n" +
                    "• Litros: **$litersStr**\n" +
                    "• Valor Total: **$costStr**\n" +
                    "• Preço/L: **$priceStr**\n" +
                    "• Odômetro: **$odoStr**\n" +
                    stationStr +
                    "• Tanque cheio: **${if (isFullTank) "Sim" else "Não"}**\n\n" +
                    "Confirme abaixo para registrar e atualizar suas métricas de consumo.",
            actionType = "FUEL",
            fuelEntry = fuelEntry
        )
    }

    private fun extractMoneyAmounts(text: String): List<Double> {
        val list = mutableListOf<Double>()
        // Match R$ 250, 250,50, R$250.00, 250 reais
        val pattern = Pattern.compile("(?:r\\$|reais)?\\s*(\\d{1,5}(?:[.,]\\d{1,2})?)\\s*(?:r\\$|reais)?", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val numStr = matcher.group(1)?.replace(",", ".") ?: continue
            val value = numStr.toDoubleOrNull() ?: continue
            // filter out odometers or years
            if (value in 0.5..50000.0 && value != 2024.0 && value != 2025.0 && value != 2026.0) {
                list.add(value)
            }
        }
        return list
    }

    private fun extractLiters(text: String): Double {
        // Match 40 litros, 35.5 l, 42.8L, 40litros
        val pattern = Pattern.compile("(\\d{1,3}(?:[.,]\\d{1,2})?)\\s*(?:litros?|lts?|l\\b)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            val numStr = matcher.group(1)?.replace(",", ".") ?: return 0.0
            return numStr.toDoubleOrNull() ?: 0.0
        }
        return 0.0
    }

    private fun extractOdometer(text: String, latestOdometer: Double): Double {
        // Match "odometro 45000", "hodometro 45.000", "km 45000", "45000 km", "45.200 km"
        val patterns = listOf(
            Pattern.compile("(?:od[oô]metro|hod[oô]metro|km|com)\\s*[:=]?\\s*(\\d{1,3}(?:[.,]\\d{3})*|\\d{2,7})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})+|\\d{3,7})\\s*km\\b", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val raw = matcher.group(1) ?: continue
                val clean = raw.replace(".", "").replace(",", "")
                val parsed = clean.toDoubleOrNull()
                if (parsed != null && parsed >= 10.0) {
                    return parsed
                }
            }
        }
        return 0.0
    }

    private fun extractStation(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            "ipiranga" in lower -> "Posto Ipiranga"
            "shell" in lower -> "Posto Shell"
            "petrobras" in lower || "br" in lower -> "Posto Petrobras (BR)"
            "ale" in lower -> "Posto Ale"
            "graal" in lower -> "Posto Graal"
            "boxter" in lower -> "Posto Boxter"
            "carrefour" in lower -> "Posto Carrefour"
            "extra" in lower -> "Posto Extra"
            else -> {
                val pattern = Pattern.compile("(?:posto|no posto)\\s+([A-Za-z0-9À-ÿ\\s]{3,20})", Pattern.CASE_INSENSITIVE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    "Posto " + (matcher.group(1)?.trim()?.replaceFirstChar { it.uppercase() } ?: "")
                } else ""
            }
        }
    }

    private fun extractMaintenanceService(text: String): String {
        return when {
            "combustível" in text || "combustivel" in text || "filtro combust" in text -> "Troca de Filtro de Combustível"
            "câmbio" in text || "cambio" in text || "transmissão" in text || "transmissao" in text -> "Troca de Fluido e Filtro de Câmbio"
            "filtro de óleo" in text || "filtro de oleo" in text || "filtro oleo" in text -> "Troca de Filtro de Óleo do Motor"
            "óleo" in text || "oleo" in text -> "Troca de Óleo do Motor e Filtros"
            "freio" in text || "pastilha" in text || "fluido de freio" in text -> "Filtro e Fluido de Freio"
            "pneu" in text -> "Troca de Pneus"
            "alinhamento" in text || "balanceamento" in text -> "Alinhamento e Balanceamento"
            "revisão" in text || "revisao" in text -> "Revisão Preventiva Geral"
            "bateria" in text -> "Substituição da Bateria"
            "suspensão" in text || "suspensao" in text || "amortecedor" in text -> "Suspensão e Amortecedores"
            "vela" in text || "correia" in text -> "Velas e Correia Dentada"
            "ar condicionado" in text || "higienização" in text -> "Filtro de Cabine e Ar Condicionado"
            else -> "Manutenção Mecânica"
        }
    }

    private fun extractWorkshop(text: String): String {
        val pattern = Pattern.compile("(?:oficina|mec[aâ]nica|na|no)\\s+([A-Za-z0-9À-ÿ\\s]{3,25})", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            val found = matcher.group(1)?.trim() ?: ""
            if (!found.equals("posto", ignoreCase = true) && !found.startsWith("km", ignoreCase = true)) {
                return found.replaceFirstChar { it.uppercase() }
            }
        }
        return ""
    }
}
