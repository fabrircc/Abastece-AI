package com.example.ai

import com.example.BuildConfig
import com.example.model.FinanceEntry
import com.example.model.FinanceType
import com.example.model.FuelEntry
import com.example.model.FuelType
import com.example.model.MaintenanceEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun processUserMessage(
        userMessage: String,
        latestOdometer: Double = 0.0,
        currentStatsSummary: String = ""
    ): ParsedAiAction = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) {
            ""
        }

        // If no API key or placeholder key, use LocalVehicleParser directly
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext LocalVehicleParser.parseMessage(userMessage, latestOdometer, currentStatsSummary)
        }

        try {
            val responseJson = callGeminiRest(apiKey, userMessage, latestOdometer, currentStatsSummary)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isNotBlank()) {
                parseGeminiResponse(text, userMessage, latestOdometer)
            } else {
                LocalVehicleParser.parseMessage(userMessage, latestOdometer, currentStatsSummary)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LocalVehicleParser.parseMessage(userMessage, latestOdometer, currentStatsSummary)
        }
    }

    private fun callGeminiRest(
        apiKey: String,
        userMessage: String,
        latestOdometer: Double,
        statsSummary: String
    ): JSONObject {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemPrompt = """
            Você é o assistente inteligente do app 'Combustível IA', especialista em médias de consumo, manutenção e finanças veiculares.
            Se o usuário informar um abastecimento, manutenção ou despesa/receita, responda de forma prestativa em português do Brasil e inclua SEMPRE no final da sua resposta um bloco JSON estruturado com o formato:
            ```json
            {
               "actionType": "FUEL" | "MAINTENANCE" | "FINANCE" | null,
               "odometerKm": number (km atual, último conhecido foi $latestOdometer),
               "liters": number,
               "totalCost": number (valor total em R$),
               "pricePerLiter": number (preço por litro em R$),
               "fuelType": "Gasolina Comum" | "Gasolina Aditivada" | "Etanol" | "Diesel S10" | "Diesel S500" | "GNV" | "Elétrico",
               "stationName": string (nome do posto ou oficina),
               "isFullTank": boolean,
               "serviceType": string (caso seja manutenção),
               "financeType": "RECEITA" | "OUTRO_GASTO" (caso seja financeiro),
               "category": string (categoria financeira)
            }
            ```
            Contexto atual das métricas do usuário:
            $statsSummary
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contentsArr = JSONArray()
            val userContent = JSONObject().apply {
                val partsArr = JSONArray()
                partsArr.put(JSONObject().apply { put("text", userMessage) })
                put("parts", partsArr)
            }
            contentsArr.put(userContent)
            put("contents", contentsArr)

            val sysContent = JSONObject().apply {
                val partsArr = JSONArray()
                partsArr.put(JSONObject().apply { put("text", systemPrompt) })
                put("parts", partsArr)
            }
            put("systemInstruction", sysContent)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Gemini API HTTP ${response.code}: ${response.message}")
            }
            val responseString = response.body?.string() ?: "{}"
            return JSONObject(responseString)
        }
    }

    private fun parseGeminiResponse(fullText: String, originalMessage: String, fallbackOdometer: Double): ParsedAiAction {
        val jsonPattern = Regex("```(?:json)?\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)
        val match = jsonPattern.find(fullText)

        if (match == null) {
            return ParsedAiAction(replyText = fullText.trim())
        }

        val jsonStr = match.groupValues[1]
        val cleanReply = fullText.replace(match.value, "").trim()

        return try {
            val obj = JSONObject(jsonStr)
            val actionType = obj.optString("actionType", null)

            when (actionType) {
                "FUEL" -> {
                    val odo = obj.optDouble("odometerKm", fallbackOdometer)
                    val liters = obj.optDouble("liters", 0.0)
                    val totalCost = obj.optDouble("totalCost", 0.0)
                    var price = obj.optDouble("pricePerLiter", 0.0)
                    if (price == 0.0 && liters > 0 && totalCost > 0) {
                        price = totalCost / liters
                    }
                    val fuelTypeStr = obj.optString("fuelType", "Gasolina Comum")
                    val station = obj.optString("stationName", "")
                    val isFullTank = obj.optBoolean("isFullTank", true)

                    val entry = FuelEntry(
                        odometerKm = if (odo.isNaN()) fallbackOdometer else odo,
                        liters = if (liters.isNaN()) 0.0 else liters,
                        pricePerLiter = if (price.isNaN()) 0.0 else price,
                        totalCost = if (totalCost.isNaN()) 0.0 else totalCost,
                        fuelType = FuelType.fromString(fuelTypeStr),
                        stationName = station,
                        isFullTank = isFullTank,
                        notes = originalMessage
                    )
                    ParsedAiAction(
                        replyText = cleanReply,
                        actionType = "FUEL",
                        fuelEntry = entry
                    )
                }
                "MAINTENANCE" -> {
                    val odo = obj.optDouble("odometerKm", fallbackOdometer)
                    val cost = obj.optDouble("totalCost", 0.0)
                    val service = obj.optString("serviceType", "Manutenção")
                    val workshop = obj.optString("stationName", "")

                    val entry = MaintenanceEntry(
                        odometerKm = if (odo.isNaN()) fallbackOdometer else odo,
                        serviceType = service,
                        workshopName = workshop,
                        cost = if (cost.isNaN()) 0.0 else cost,
                        notes = originalMessage
                    )
                    ParsedAiAction(
                        replyText = cleanReply,
                        actionType = "MAINTENANCE",
                        maintenanceEntry = entry
                    )
                }
                "FINANCE" -> {
                    val fTypeStr = obj.optString("financeType", "OUTRO_GASTO")
                    val fType = if (fTypeStr.contains("REC", ignoreCase = true)) FinanceType.RECEITA else FinanceType.OUTRO_GASTO
                    val amount = obj.optDouble("totalCost", 0.0)
                    val category = obj.optString("category", if (fType == FinanceType.RECEITA) "Receita" else "Despesa")

                    val entry = FinanceEntry(
                        type = fType,
                        category = category,
                        amount = if (amount.isNaN()) 0.0 else amount,
                        notes = originalMessage
                    )
                    ParsedAiAction(
                        replyText = cleanReply,
                        actionType = "FINANCE",
                        financeEntry = entry
                    )
                }
                else -> {
                    ParsedAiAction(replyText = cleanReply)
                }
            }
        } catch (_: Exception) {
            ParsedAiAction(replyText = cleanReply)
        }
    }
}
