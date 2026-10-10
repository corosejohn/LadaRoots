package com.example.ladaroots.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PepperType(val displayNameBm: String, val displayNameEn: String) {
    BLACK("Lada Hitam", "Black Pepper"),
    WHITE("Lada Putih", "White Pepper")
}

enum class UserRole(val labelBm: String) {
    FARMER("Peladang"),
    BUYER("Pembeli")
}

enum class ListingStatus {
    ACTIVE,
    PARTIALLY_RESERVED,
    FULLY_RESERVED,
    SOLD,
    CANCELLED
}

enum class OfferStatus(val labelBm: String) {
    PENDING("Menunggu"),
    ACCEPTED("Diterima"),
    REJECTED("Ditolak"),
    WITHDRAWN("Ditarik Balik"),
    EXPIRED("Tamat Tempoh")
}

enum class OrderStatus(val labelBm: String) {
    CONFIRMED("Disahkan"),
    PREPARING("Sedang Disediakan"),
    READY_FOR_PICKUP("Sedia Diambil / Dihantar"),
    COMPLETED("Selesai"),
    CANCELLED("Dibatalkan")
}

enum class MovementType(val labelBm: String) {
    HARVEST_IN("Hasil Tuaian"),
    RESERVATION_HOLD("Tempahan Tawaran"),
    RESERVATION_RELEASE("Pelepasan Tempahan"),
    ORDER_FULFILL("Penyerahan Pesanan"),
    MANUAL_ADJUST("Pelarasan Stok")
}

enum class DemandStatus(val labelBm: String) {
    OPEN("Terbuka"),
    PARTIALLY_MET("Separa Dipenuhi"),
    FULFILLED("Selesai Dipenuhi"),
    EXPIRED("Tamat Tempoh")
}

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val name: String,
    val role: UserRole,
    val location: String,
    val phone: String,
    val avatarEmoji: String = "👨‍🌾"
)

@Entity(tableName = "harvest_records")
data class HarvestRecord(
    @PrimaryKey val id: String,
    val farmerId: String,
    val pepperType: PepperType,
    val originalQty: Double,
    val originalUnit: String, // "kg" or "tan"
    val qtyKg: Double,
    val notes: String = "",
    val imageUri: String? = null,
    val harvestDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory_movements")
data class InventoryMovement(
    @PrimaryKey val id: String,
    val farmerId: String,
    val pepperType: PepperType,
    val movementType: MovementType,
    val qtyKg: Double,
    val referenceId: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey val id: String,
    val farmerId: String,
    val farmerName: String,
    val pepperType: PepperType,
    val grade: String = "FAQ (Sarawak)",
    val qtyKg: Double,
    val reservedQtyKg: Double = 0.0,
    val pricePerKg: Double,
    val status: ListingStatus = ListingStatus.ACTIVE,
    val description: String = "",
    val location: String = "Sarikei, Sarawak",
    val minQtyKg: Double = 10.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val availableQtyKg: Double
        get() = (qtyKg - reservedQtyKg).coerceAtLeast(0.0)
}

@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey val id: String,
    val listingId: String,
    val farmerId: String,
    val buyerId: String,
    val buyerName: String,
    val pepperType: PepperType,
    val qtyKg: Double,
    val pricePerKg: Double,
    val totalPrice: Double,
    val status: OfferStatus = OfferStatus.PENDING,
    val message: String = "",
    val counterPricePerKg: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val offerId: String,
    val listingId: String,
    val farmerId: String,
    val buyerId: String,
    val buyerName: String,
    val pepperType: PepperType,
    val qtyKg: Double,
    val pricePerKg: Double,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.CONFIRMED,
    val pickupNotes: String = "Pusat Pengumpulan Lada Sarikei",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "demand_requests")
data class DemandRequest(
    @PrimaryKey val id: String,
    val buyerId: String,
    val buyerName: String,
    val pepperType: PepperType,
    val targetQtyKg: Double,
    val targetPricePerKg: Double,
    val requiredByDate: String,
    val location: String = "Kuching / Sarikei",
    val deliveryPreference: String = "Penghantaran atau Ambil Sendiri",
    val status: DemandStatus = DemandStatus.OPEN,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val entityType: String, // "OFFER", "ORDER", "LISTING", "DEMAND"
    val entityId: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "market_price_references")
data class MarketPriceReference(
    @PrimaryKey val id: String,
    val pepperType: PepperType,
    val grade: String,
    val pricePerKg: Double,
    val location: String,
    val source: String = "Lembaga Lada Malaysia (MPB)",
    val lastUpdated: String
)

data class PepperStockSummary(
    val pepperType: PepperType,
    val physicalStockKg: Double,
    val reservedStockKg: Double,
    val availableStockKg: Double,
    val activeListingQtyKg: Double
)
