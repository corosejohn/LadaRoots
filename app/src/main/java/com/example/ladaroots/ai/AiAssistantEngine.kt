package com.example.ladaroots.ai

import com.example.ladaroots.data.model.PepperStockSummary
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.data.repository.DemandMarketInsight
import java.util.regex.Pattern

sealed class AiActionProposal {
    data class RecordHarvestProposal(
        val pepperType: PepperType,
        val quantity: Double,
        val unit: String,
        val normalizedKg: Double,
        val notes: String = ""
    ) : AiActionProposal()

    data class CreateListingProposal(
        val pepperType: PepperType,
        val quantityKg: Double,
        val askingPricePerKg: Double,
        val estimatedTotal: Double
    ) : AiActionProposal()

    data object NavigateToInventory : AiActionProposal()
    data object NavigateToOffers : AiActionProposal()
    data object NavigateToDemand : AiActionProposal()
    data object None : AiActionProposal()
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val proposal: AiActionProposal = AiActionProposal.None
)

class AiAssistantEngine {

    fun processMessage(
        userText: String,
        stockBlack: PepperStockSummary?,
        stockWhite: PepperStockSummary?,
        insights: Map<PepperType, DemandMarketInsight>?
    ): ChatMessage {
        val lower = userText.lowercase().trim()

        // 1. Check for Harvest intent
        if (lower.contains("tuai") || lower.contains("dapat") || lower.contains("petik") || lower.contains("harvest") || (lower.contains("kilo") && !lower.contains("jual"))) {
            val (type, qty, unit) = extractPepperAndQuantity(lower)
            if (qty != null && qty > 0) {
                val normalizedKg = if (unit == "tan") qty * 1000.0 else qty
                val proposal = AiActionProposal.RecordHarvestProposal(
                    pepperType = type,
                    quantity = qty,
                    unit = unit,
                    normalizedKg = normalizedKg,
                    notes = "Direkodkan melalui Pembantu AI"
                )
                val reply = "Baik, saya fahami anda ingin merekodkan ${String.format("%.1f", qty)} $unit ${type.displayNameBm.lowercase()} (${String.format("%.1f", normalizedKg)} kg). Nak saya simpan terus ke dalam rekod inventori anda?"
                return ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    text = reply,
                    isUser = false,
                    proposal = proposal
                )
            }
        }

        // 2. Check for Sell / Listing intent
        if (lower.contains("jual") || lower.contains("listing") || lower.contains("iklan") || lower.contains("sell")) {
            val (type, qty, _) = extractPepperAndQuantity(lower)
            val price = extractPrice(lower) ?: 27.0 // Default suggested price around MPB benchmark
            val quantityKg = qty ?: 50.0

            val currentStock = if (type == PepperType.BLACK) stockBlack?.availableStockKg ?: 0.0 else stockWhite?.availableStockKg ?: 0.0
            if (quantityKg > currentStock) {
                val reply = "Maaf, kuantiti yang ingin dijual (${String.format("%.1f", quantityKg)} kg) melebihi stok tersedia anda (${String.format("%.1f", currentStock)} kg ${type.displayNameBm.lowercase()}). Sila semak semula stok anda terlebih dahulu."
                return ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    text = reply,
                    isUser = false,
                    proposal = AiActionProposal.NavigateToInventory
                )
            }

            val estTotal = quantityKg * price
            val proposal = AiActionProposal.CreateListingProposal(
                pepperType = type,
                quantityKg = quantityKg,
                askingPricePerKg = price,
                estimatedTotal = estTotal
            )
            val reply = "Baik, anda ingin menyenaraikan ${String.format("%.1f", quantityKg)} kg ${type.displayNameBm.lowercase()} pada harga RM${String.format("%.2f", price)}/kg (Anggaran nilai RM${String.format("%.2f", estTotal)}). Adakah maklumat ini tepat untuk diterbitkan?"
            return ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                text = reply,
                isUser = false,
                proposal = proposal
            )
        }

        // 3. Check for Stock status
        if (lower.contains("stok") || lower.contains("baki") || lower.contains("inventory") || lower.contains("inventori")) {
            val blackAvail = stockBlack?.availableStockKg ?: 0.0
            val blackReserved = stockBlack?.reservedStockKg ?: 0.0
            val whiteAvail = stockWhite?.availableStockKg ?: 0.0
            val whiteReserved = stockWhite?.reservedStockKg ?: 0.0

            val reply = "Status stok anda semasa:\n" +
                    "• Lada Hitam: ${String.format("%.1f", blackAvail)} kg tersedia (${String.format("%.1f", blackReserved)} kg ditempah)\n" +
                    "• Lada Putih: ${String.format("%.1f", whiteAvail)} kg tersedia (${String.format("%.1f", whiteReserved)} kg ditempah)\n\n" +
                    "Formula: Stok Tersedia = Stok Fizikal − Stok Ditempah."
            return ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                text = reply,
                isUser = false,
                proposal = AiActionProposal.NavigateToInventory
            )
        }

        // 4. Check for Buyer Demand / Permintaan
        if (lower.contains("minta") || lower.contains("permintaan") || lower.contains("demand") || lower.contains("pasaran")) {
            val blackInsight = insights?.get(PepperType.BLACK)
            val whiteInsight = insights?.get(PepperType.WHITE)
            val reply = "Gambaran Permintaan Pasaran:\n" +
                    "• Lada Hitam: ${blackInsight?.summaryBm ?: "Permintaan aktif oleh pembeli borong."}\n" +
                    "• Lada Putih: ${whiteInsight?.summaryBm ?: "Harga premium terbuka untuk gred tinggi."}\n\n" +
                    "Ketik butang di bawah untuk melihat senarai lengkap Permintaan Pembeli."
            return ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                text = reply,
                isUser = false,
                proposal = AiActionProposal.NavigateToDemand
            )
        }

        // 5. Check for Offers
        if (lower.contains("tawar") || lower.contains("offer") || lower.contains("pembeli") || lower.contains("buyer")) {
            val reply = "Anda boleh menyemak semua tawaran baru yang masuk daripada pembeli di tab 'Tawaran'. Anda boleh terima, tolak, atau buat tawaran balas (rundingan harga)."
            return ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                text = reply,
                isUser = false,
                proposal = AiActionProposal.NavigateToOffers
            )
        }

        // Default friendly response
        val reply = "Saya faham. Anda boleh meminta saya untuk merekod hasil tuaian (cth: 'Saya baru dapat 100 kg lada hitam'), semak baki stok, semak harga MPB, atau buka iklan jualan lada."
        return ChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            text = reply,
            isUser = false
        )
    }

    private fun extractPepperAndQuantity(text: String): Triple<PepperType, Double?, String> {
        val type = if (text.contains("putih") || text.contains("white")) PepperType.WHITE else PepperType.BLACK
        val unit = if (text.contains("tan") || text.contains("tonne")) "tan" else "kg"

        // Search for numbers like 120, 0.5, 50, etc.
        val matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(text)
        var qty: Double? = null
        if (matcher.find()) {
            qty = matcher.group(1)?.toDoubleOrNull()
        }
        return Triple(type, qty, unit)
    }

    private fun extractPrice(text: String): Double? {
        val patterns = listOf(
            "rm\\s*(\\d+(\\.\\d+)?)",
            "(\\d+(\\.\\d+)?)\\s*ringgit",
            "harga\\s*(\\d+(\\.\\d+)?)"
        )
        for (pat in patterns) {
            val matcher = Pattern.compile(pat).matcher(text)
            if (matcher.find()) {
                val group = matcher.group(1)
                val p = group?.toDoubleOrNull()
                if (p != null) return p
            }
        }
        return null
    }
}
