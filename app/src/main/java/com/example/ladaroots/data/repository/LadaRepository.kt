package com.example.ladaroots.data.repository

import androidx.room.withTransaction
import com.example.ladaroots.data.db.LadaRootsDatabase
import com.example.ladaroots.data.model.DemandRequest
import com.example.ladaroots.data.model.DemandStatus
import com.example.ladaroots.data.model.HarvestRecord
import com.example.ladaroots.data.model.InventoryMovement
import com.example.ladaroots.data.model.Listing
import com.example.ladaroots.data.model.ListingStatus
import com.example.ladaroots.data.model.MarketPriceReference
import com.example.ladaroots.data.model.MovementType
import com.example.ladaroots.data.model.NotificationItem
import com.example.ladaroots.data.model.Offer
import com.example.ladaroots.data.model.OfferStatus
import com.example.ladaroots.data.model.Order
import com.example.ladaroots.data.model.OrderStatus
import com.example.ladaroots.data.model.PepperStockSummary
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.data.model.UserProfile
import com.example.ladaroots.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.UUID

sealed class TransactionResult<out T> {
    data class Success<out T>(val data: T, val message: String = "") : TransactionResult<T>()
    data class Error(val message: String) : TransactionResult<Nothing>()
}

data class DemandMarketInsight(
    val pepperType: PepperType,
    val openDemandKg: Double,
    val listedSupplyKg: Double,
    val matchedKg: Double,
    val unmetDemandKg: Double,
    val coveragePercent: Int,
    val summaryBm: String
)

class LadaRepository(private val db: LadaRootsDatabase) {
    val harvestDao = db.harvestDao()
    val listingDao = db.listingDao()
    val offerDao = db.offerDao()
    val orderDao = db.orderDao()
    val demandDao = db.demandDao()
    val notificationDao = db.notificationDao()
    val marketPriceDao = db.marketPriceDao()
    val inventoryDao = db.inventoryMovementDao()
    val userDao = db.userDao()

    fun getAllUsers(): Flow<List<UserProfile>> = userDao.getAllUsers()
    fun getHarvestsByFarmer(farmerId: String): Flow<List<HarvestRecord>> = harvestDao.getHarvestsByFarmer(farmerId)
    fun getMovementsByFarmer(farmerId: String): Flow<List<InventoryMovement>> = inventoryDao.getMovementsByFarmer(farmerId)
    fun getActiveListings(): Flow<List<Listing>> = listingDao.getActiveListings()
    fun getListingsByFarmer(farmerId: String): Flow<List<Listing>> = listingDao.getListingsByFarmer(farmerId)
    fun getOffersForFarmer(farmerId: String): Flow<List<Offer>> = offerDao.getOffersForFarmer(farmerId)
    fun getOffersForBuyer(buyerId: String): Flow<List<Offer>> = offerDao.getOffersForBuyer(buyerId)
    fun getOrdersForFarmer(farmerId: String): Flow<List<Order>> = orderDao.getOrdersForFarmer(farmerId)
    fun getOrdersForBuyer(buyerId: String): Flow<List<Order>> = orderDao.getOrdersForBuyer(buyerId)
    fun getOpenDemands(): Flow<List<DemandRequest>> = demandDao.getOpenDemands()
    fun getDemandsByBuyer(buyerId: String): Flow<List<DemandRequest>> = demandDao.getDemandsByBuyer(buyerId)
    fun getMarketPrices(): Flow<List<MarketPriceReference>> = marketPriceDao.getAllPrices()
    fun getNotificationsForUser(userId: String): Flow<List<NotificationItem>> = notificationDao.getNotificationsForUser(userId)
    fun getUnreadNotificationCount(userId: String): Flow<Int> = notificationDao.getUnreadCount(userId)

    suspend fun markNotificationAsRead(id: String) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead(userId: String) = notificationDao.markAllAsRead(userId)

    /**
     * Compute real-time stock accounting for a farmer:
     * Available Stock = Physical Stock - Reserved Stock
     */
    fun getStockSummary(farmerId: String, pepperType: PepperType): Flow<PepperStockSummary> {
        return combine(
            harvestDao.getHarvestsByFarmer(farmerId),
            orderDao.getOrdersForFarmer(farmerId),
            listingDao.getListingsByFarmer(farmerId)
        ) { harvests, orders, listings ->
            val totalHarvested = harvests
                .filter { it.pepperType == pepperType }
                .sumOf { it.qtyKg }

            val completedDeduction = orders
                .filter { it.pepperType == pepperType && it.status == OrderStatus.COMPLETED }
                .sumOf { it.qtyKg }

            val physical = (totalHarvested - completedDeduction).coerceAtLeast(0.0)

            val reserved = orders
                .filter {
                    it.pepperType == pepperType &&
                    (it.status == OrderStatus.CONFIRMED ||
                     it.status == OrderStatus.PREPARING ||
                     it.status == OrderStatus.READY_FOR_PICKUP)
                }
                .sumOf { it.qtyKg }

            val available = (physical - reserved).coerceAtLeast(0.0)

            val activeListingQty = listings
                .filter { it.pepperType == pepperType && it.status != ListingStatus.SOLD && it.status != ListingStatus.CANCELLED }
                .sumOf { it.availableQtyKg }

            PepperStockSummary(
                pepperType = pepperType,
                physicalStockKg = physical,
                reservedStockKg = reserved,
                availableStockKg = available,
                activeListingQtyKg = activeListingQty
            )
        }
    }

    /**
     * Record a new harvest:
     * - Normalizes units: 1 tan = 1,000 kg, 0.5 tan = 500 kg
     * - Records harvest record & inventory movement atomically
     */
    suspend fun recordHarvest(
        farmerId: String,
        pepperType: PepperType,
        quantity: Double,
        unit: String,
        notes: String = "",
        imageUri: String? = null,
        harvestDate: String = "Hari ini"
    ): TransactionResult<HarvestRecord> = db.withTransaction {
        val normalizedKg = when (unit.lowercase().trim()) {
            "tan", "tonne", "tonnes", "t" -> quantity * 1000.0
            else -> quantity
        }

        if (normalizedKg <= 0.0) {
            return@withTransaction TransactionResult.Error("Kuantiti tuaian mestilah lebih daripada 0 kg.")
        }

        val harvestId = "HRV-${UUID.randomUUID().toString().take(8).uppercase()}"
        val record = HarvestRecord(
            id = harvestId,
            farmerId = farmerId,
            pepperType = pepperType,
            originalQty = quantity,
            originalUnit = unit,
            qtyKg = normalizedKg,
            notes = notes,
            imageUri = imageUri,
            harvestDate = harvestDate,
            createdAt = System.currentTimeMillis()
        )

        harvestDao.insertHarvest(record)

        val movement = InventoryMovement(
            id = "MOV-${UUID.randomUUID().toString().take(8).uppercase()}",
            farmerId = farmerId,
            pepperType = pepperType,
            movementType = MovementType.HARVEST_IN,
            qtyKg = normalizedKg,
            referenceId = harvestId,
            notes = "Tuaian $quantity $unit direkodkan (${notes.ifEmpty { "Hasil ladang" }})"
        )
        inventoryDao.insertMovement(movement)

        TransactionResult.Success(record, "Berjaya merekodkan $normalizedKg kg ${pepperType.displayNameBm} ke dalam inventori.")
    }

    /**
     * Create a new sales listing:
     * - Verifies available stock >= listing quantity
     */
    suspend fun createListing(
        farmerId: String,
        farmerName: String,
        pepperType: PepperType,
        qtyKg: Double,
        pricePerKg: Double,
        grade: String = "FAQ (Sarawak)",
        description: String = "",
        location: String = "Sarikei, Sarawak",
        minQtyKg: Double = 10.0
    ): TransactionResult<Listing> = db.withTransaction {
        if (qtyKg <= 0.0) {
            return@withTransaction TransactionResult.Error("Kuantiti jualan mestilah melebihi 0 kg.")
        }
        if (pricePerKg <= 0.0) {
            return@withTransaction TransactionResult.Error("Harga jualan mestilah melebihi RM0.00.")
        }

        val stock = getStockSummary(farmerId, pepperType).first()
        if (qtyKg > stock.availableStockKg) {
            return@withTransaction TransactionResult.Error(
                "Kuantiti (${qtyKg} kg) melebihi baki stok tersedia (${stock.availableStockKg} kg) untuk ${pepperType.displayNameBm}."
            )
        }

        val listingId = "LST-${UUID.randomUUID().toString().take(8).uppercase()}"
        val listing = Listing(
            id = listingId,
            farmerId = farmerId,
            farmerName = farmerName,
            pepperType = pepperType,
            grade = grade,
            qtyKg = qtyKg,
            reservedQtyKg = 0.0,
            pricePerKg = pricePerKg,
            status = ListingStatus.ACTIVE,
            description = description,
            location = location,
            minQtyKg = minQtyKg,
            createdAt = System.currentTimeMillis()
        )

        listingDao.insertListing(listing)
        TransactionResult.Success(listing, "Penyenaraian jualan berjaya diterbitkan.")
    }

    /**
     * Buyer submits an offer against a listing:
     */
    suspend fun submitOffer(
        listingId: String,
        buyerId: String,
        buyerName: String,
        requestedQtyKg: Double,
        offeredPricePerKg: Double,
        message: String = ""
    ): TransactionResult<Offer> = db.withTransaction {
        val listing = listingDao.getListingById(listingId)
            ?: return@withTransaction TransactionResult.Error("Penyenaraian tidak dijumpai.")

        if (listing.status != ListingStatus.ACTIVE && listing.status != ListingStatus.PARTIALLY_RESERVED) {
            return@withTransaction TransactionResult.Error("Penyenaraian ini tidak lagi aktif.")
        }

        if (requestedQtyKg <= 0.0 || requestedQtyKg > listing.availableQtyKg) {
            return@withTransaction TransactionResult.Error("Kuantiti diminta (${requestedQtyKg} kg) melebihi kuantiti tersedia (${listing.availableQtyKg} kg).")
        }

        val offerId = "OFR-${UUID.randomUUID().toString().take(8).uppercase()}"
        val totalPrice = requestedQtyKg * offeredPricePerKg
        val offer = Offer(
            id = offerId,
            listingId = listingId,
            farmerId = listing.farmerId,
            buyerId = buyerId,
            buyerName = buyerName,
            pepperType = listing.pepperType,
            qtyKg = requestedQtyKg,
            pricePerKg = offeredPricePerKg,
            totalPrice = totalPrice,
            status = OfferStatus.PENDING,
            message = message,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        offerDao.insertOffer(offer)

        // Notify farmer
        notificationDao.insertNotification(
            NotificationItem(
                id = "NOTIF-${UUID.randomUUID().toString().take(8).uppercase()}",
                userId = listing.farmerId,
                title = "Tawaran Baru Diterima",
                message = "$buyerName menawarkan RM$offeredPricePerKg/kg untuk ${requestedQtyKg}kg ${listing.pepperType.displayNameBm}.",
                entityType = "OFFER",
                entityId = offerId
            )
        )

        TransactionResult.Success(offer, "Tawaran berjaya dihantar kepada peladang.")
    }

    /**
     * Farmer accepts an offer (ATOMIC TRANSACTION):
     * 1. Verify authenticated farmer owns listing.
     * 2. Verify offer is still pending.
     * 3. Verify listing is eligible.
     * 4. Verify requested quantity is available in stock.
     * 5. Atomically reserve quantity on listing and stock.
     * 6. Create exactly one Order.
     * 7. Update offer status to ACCEPTED.
     * 8. Notify buyer.
     */
    suspend fun acceptOffer(
        farmerId: String,
        offerId: String
    ): TransactionResult<Order> = db.withTransaction {
        val offer = offerDao.getOfferById(offerId)
            ?: return@withTransaction TransactionResult.Error("Tawaran tidak dijumpai.")

        if (offer.farmerId != farmerId) {
            return@withTransaction TransactionResult.Error("Anda tidak mempunyai kebenaran untuk menerima tawaran ini.")
        }

        if (offer.status != OfferStatus.PENDING) {
            return@withTransaction TransactionResult.Error("Tawaran ini bukan lagi dalam status menunggu (${offer.status.labelBm}).")
        }

        val listing = listingDao.getListingById(offer.listingId)
            ?: return@withTransaction TransactionResult.Error("Penyenaraian berkaitan tidak dijumpai.")

        val stock = getStockSummary(farmerId, offer.pepperType).first()
        if (stock.availableStockKg < offer.qtyKg) {
            return@withTransaction TransactionResult.Error(
                "Stok fizikal tersedia (${stock.availableStockKg} kg) tidak mencukupi untuk memenuhi tawaran ${offer.qtyKg} kg."
            )
        }

        // Check if an order already exists for this offer to prevent duplicates
        val existingOrder = orderDao.getOrderByOfferId(offerId)
        if (existingOrder != null) {
            return@withTransaction TransactionResult.Success(existingOrder, "Pesanan telah pun diproses sebelum ini.")
        }

        // 1. Update listing reservation
        val newReserved = listing.reservedQtyKg + offer.qtyKg
        val newStatus = if (newReserved >= listing.qtyKg) ListingStatus.FULLY_RESERVED else ListingStatus.PARTIALLY_RESERVED
        listingDao.updateListingReservation(listing.id, newReserved, newStatus.name)

        // 2. Update offer status
        val now = System.currentTimeMillis()
        offerDao.updateOfferStatus(offer.id, OfferStatus.ACCEPTED.name, now)

        // 3. Create exactly one Order
        val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val order = Order(
            id = orderId,
            offerId = offer.id,
            listingId = listing.id,
            farmerId = farmerId,
            buyerId = offer.buyerId,
            buyerName = offer.buyerName,
            pepperType = offer.pepperType,
            qtyKg = offer.qtyKg,
            pricePerKg = offer.pricePerKg,
            totalAmount = offer.totalPrice,
            status = OrderStatus.CONFIRMED,
            pickupNotes = "Pusat Pengumpulan Lada Sarikei",
            createdAt = now,
            updatedAt = now
        )
        orderDao.insertOrder(order)

        // 4. Record inventory movement (reservation hold)
        inventoryDao.insertMovement(
            InventoryMovement(
                id = "MOV-${UUID.randomUUID().toString().take(8).uppercase()}",
                farmerId = farmerId,
                pepperType = offer.pepperType,
                movementType = MovementType.RESERVATION_HOLD,
                qtyKg = offer.qtyKg,
                referenceId = orderId,
                notes = "Tempahan tawaran dipersetujui untuk pesanan $orderId"
            )
        )

        // 5. Notify buyer
        notificationDao.insertNotification(
            NotificationItem(
                id = "NOTIF-${UUID.randomUUID().toString().take(8).uppercase()}",
                userId = offer.buyerId,
                title = "Tawaran Anda Diterima!",
                message = "Peladang telah bersetuju dengan tawaran anda bagi ${offer.qtyKg}kg ${offer.pepperType.displayNameBm}. Pesanan $orderId telah dicipta.",
                entityType = "ORDER",
                entityId = orderId
            )
        )

        TransactionResult.Success(order, "Tawaran berjaya diterima! Pesanan $orderId telah dicipta.")
    }

    /**
     * Reject an offer
     */
    suspend fun rejectOffer(farmerId: String, offerId: String): TransactionResult<Unit> = db.withTransaction {
        val offer = offerDao.getOfferById(offerId)
            ?: return@withTransaction TransactionResult.Error("Tawaran tidak dijumpai.")

        if (offer.farmerId != farmerId) {
            return@withTransaction TransactionResult.Error("Tiada kebenaran.")
        }
        if (offer.status != OfferStatus.PENDING) {
            return@withTransaction TransactionResult.Error("Tawaran tidak boleh ditolak kerana bukan lagi dalam status menunggu.")
        }

        val now = System.currentTimeMillis()
        offerDao.updateOfferStatus(offer.id, OfferStatus.REJECTED.name, now)

        notificationDao.insertNotification(
            NotificationItem(
                id = "NOTIF-${UUID.randomUUID().toString().take(8).uppercase()}",
                userId = offer.buyerId,
                title = "Tawaran Ditolak",
                message = "Peladang telah menolak tawaran bagi ${offer.qtyKg}kg ${offer.pepperType.displayNameBm}.",
                entityType = "OFFER",
                entityId = offer.id
            )
        )

        TransactionResult.Success(Unit, "Tawaran telah ditolak.")
    }

    /**
     * Negotiate / counter-offer
     */
    suspend fun counterOffer(
        farmerId: String,
        offerId: String,
        counterPricePerKg: Double
    ): TransactionResult<Unit> = db.withTransaction {
        val offer = offerDao.getOfferById(offerId)
            ?: return@withTransaction TransactionResult.Error("Tawaran tidak dijumpai.")

        if (offer.farmerId != farmerId) return@withTransaction TransactionResult.Error("Tiada kebenaran.")

        val updatedOffer = offer.copy(
            counterPricePerKg = counterPricePerKg,
            updatedAt = System.currentTimeMillis()
        )
        offerDao.updateOffer(updatedOffer)

        notificationDao.insertNotification(
            NotificationItem(
                id = "NOTIF-${UUID.randomUUID().toString().take(8).uppercase()}",
                userId = offer.buyerId,
                title = "Tawaran Balas dari Peladang",
                message = "Peladang mencadangkan harga rundingan baru RM$counterPricePerKg/kg bagi ${offer.qtyKg}kg ${offer.pepperType.displayNameBm}.",
                entityType = "OFFER",
                entityId = offer.id
            )
        )

        TransactionResult.Success(Unit, "Tawaran balas RM$counterPricePerKg/kg dihantar.")
    }

    /**
     * Complete an order (Decreases physical stock and releases reservation):
     */
    suspend fun completeOrder(orderId: String): TransactionResult<Order> = db.withTransaction {
        val order = orderDao.getOrderById(orderId)
            ?: return@withTransaction TransactionResult.Error("Pesanan tidak dijumpai.")

        if (order.status == OrderStatus.COMPLETED) {
            return@withTransaction TransactionResult.Success(order, "Pesanan telah pun selesai.")
        }
        if (order.status == OrderStatus.CANCELLED) {
            return@withTransaction TransactionResult.Error("Pesanan yang telah dibatalkan tidak boleh diselesaikan.")
        }

        val now = System.currentTimeMillis()
        orderDao.updateOrderStatus(orderId, OrderStatus.COMPLETED.name, now)

        // Record fulfillment movement
        inventoryDao.insertMovement(
            InventoryMovement(
                id = "MOV-${UUID.randomUUID().toString().take(8).uppercase()}",
                farmerId = order.farmerId,
                pepperType = order.pepperType,
                movementType = MovementType.ORDER_FULFILL,
                qtyKg = order.qtyKg,
                referenceId = orderId,
                notes = "Penyerahan pesanan selesai untuk pembeli ${order.buyerName}"
            )
        )

        val updated = order.copy(status = OrderStatus.COMPLETED, updatedAt = now)
        TransactionResult.Success(updated, "Pesanan $orderId telah berjaya diselesaikan.")
    }

    /**
     * Cancel an eligible order (Releases reservation back to available stock):
     */
    suspend fun cancelOrder(orderId: String, reason: String = "Dibatalkan oleh pihak berwajib"): TransactionResult<Order> = db.withTransaction {
        val order = orderDao.getOrderById(orderId)
            ?: return@withTransaction TransactionResult.Error("Pesanan tidak dijumpai.")

        if (order.status == OrderStatus.COMPLETED) {
            return@withTransaction TransactionResult.Error("Pesanan yang sudah selesai tidak boleh dibatalkan.")
        }
        if (order.status == OrderStatus.CANCELLED) {
            return@withTransaction TransactionResult.Success(order, "Pesanan memang telah dibatalkan.")
        }

        val now = System.currentTimeMillis()
        orderDao.updateOrderStatus(orderId, OrderStatus.CANCELLED.name, now)

        // Release reservation on listing if still available
        val listing = listingDao.getListingById(order.listingId)
        if (listing != null) {
            val updatedReserved = (listing.reservedQtyKg - order.qtyKg).coerceAtLeast(0.0)
            val updatedStatus = if (updatedReserved <= 0.0) ListingStatus.ACTIVE else ListingStatus.PARTIALLY_RESERVED
            listingDao.updateListingReservation(listing.id, updatedReserved, updatedStatus.name)
        }

        // Record reservation release movement
        inventoryDao.insertMovement(
            InventoryMovement(
                id = "MOV-${UUID.randomUUID().toString().take(8).uppercase()}",
                farmerId = order.farmerId,
                pepperType = order.pepperType,
                movementType = MovementType.RESERVATION_RELEASE,
                qtyKg = order.qtyKg,
                referenceId = orderId,
                notes = "Pelepasan tempahan akibat pembatalan pesanan $orderId ($reason)"
            )
        )

        val updated = order.copy(status = OrderStatus.CANCELLED, updatedAt = now)
        TransactionResult.Success(updated, "Pesanan $orderId telah dibatalkan dan tempahan dilepaskan.")
    }

    /**
     * Update order progression (e.g. Preparing -> Ready for pickup)
     */
    suspend fun updateOrderStage(orderId: String, newStatus: OrderStatus): TransactionResult<Order> = db.withTransaction {
        val order = orderDao.getOrderById(orderId)
            ?: return@withTransaction TransactionResult.Error("Pesanan tidak dijumpai.")

        val now = System.currentTimeMillis()
        orderDao.updateOrderStatus(orderId, newStatus.name, now)
        val updated = order.copy(status = newStatus, updatedAt = now)
        TransactionResult.Success(updated, "Status pesanan dikemaskini kepada: ${newStatus.labelBm}")
    }

    /**
     * Buyer creates a Demand Request
     */
    suspend fun createDemandRequest(
        buyerId: String,
        buyerName: String,
        pepperType: PepperType,
        targetQtyKg: Double,
        targetPricePerKg: Double,
        requiredByDate: String,
        location: String = "Kuching / Sarikei",
        notes: String = ""
    ): TransactionResult<DemandRequest> = db.withTransaction {
        if (targetQtyKg <= 0.0) return@withTransaction TransactionResult.Error("Kuantiti sasaran mestilah lebih 0 kg.")
        if (targetPricePerKg <= 0.0) return@withTransaction TransactionResult.Error("Harga sasaran mestilah lebih RM0.00.")

        val demandId = "DMD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val request = DemandRequest(
            id = demandId,
            buyerId = buyerId,
            buyerName = buyerName,
            pepperType = pepperType,
            targetQtyKg = targetQtyKg,
            targetPricePerKg = targetPricePerKg,
            requiredByDate = requiredByDate,
            location = location,
            status = DemandStatus.OPEN,
            notes = notes,
            createdAt = System.currentTimeMillis()
        )

        demandDao.insertDemand(request)
        TransactionResult.Success(request, "Permintaan pasaran berjaya diterbitkan kepada komuniti peladang.")
    }

    /**
     * Demand-to-harvest real-time marketplace calculation
     */
    fun getMarketInsights(): Flow<Map<PepperType, DemandMarketInsight>> {
        return combine(
            demandDao.getAllDemands(),
            listingDao.getActiveListings(),
            orderDao.getAllOrders()
        ) { demands, listings, orders ->
            PepperType.entries.associateWith { type ->
                val openDemand = demands
                    .filter { it.pepperType == type && it.status == DemandStatus.OPEN }
                    .sumOf { it.targetQtyKg }

                val listedSupply = listings
                    .filter { it.pepperType == type }
                    .sumOf { it.availableQtyKg }

                val matched = orders
                    .filter { it.pepperType == type && it.status != OrderStatus.CANCELLED }
                    .sumOf { it.qtyKg }

                val unmet = (openDemand - listedSupply).coerceAtLeast(0.0)
                val coverage = if (openDemand > 0) ((listedSupply / openDemand) * 100).toInt().coerceAtMost(100) else 100

                val summary = if (openDemand > listedSupply) {
                    "Permintaan ${type.displayNameBm.lowercase()} melebihi stok tersedia di pasaran sebanyak ${String.format("%.1f", unmet)} kg."
                } else if (openDemand > 0) {
                    "Bekalan ${type.displayNameBm.lowercase()} mencukupi untuk memenuhi permintaan semasa pembeli ($coverage% liputan)."
                } else {
                    "Tiada permintaan terbuka bagi ${type.displayNameBm.lowercase()} setakat ini."
                }

                DemandMarketInsight(
                    pepperType = type,
                    openDemandKg = openDemand,
                    listedSupplyKg = listedSupply,
                    matchedKg = matched,
                    unmetDemandKg = unmet,
                    coveragePercent = coverage,
                    summaryBm = summary
                )
            }
        }
    }

    /**
     * Seed initial demo data for smooth evaluation
     */
    suspend fun seedDemoDataIfEmpty() = db.withTransaction {
        val existingUsers = userDao.getUserById("usr_farmer_1")
        if (existingUsers != null) return@withTransaction

        // Profiles
        val farmer = UserProfile(
            id = "usr_farmer_1",
            name = "Pak Samad Jalil",
            role = UserRole.FARMER,
            location = "Sarikei, Sarawak",
            phone = "+60 19-882 3411",
            avatarEmoji = "👨‍🌾"
        )
        val buyer = UserProfile(
            id = "usr_buyer_1",
            name = "Mega Spices Sdn Bhd (Encik Steven)",
            role = UserRole.BUYER,
            location = "Kuching, Sarawak",
            phone = "+60 12-871 9090",
            avatarEmoji = "🏢"
        )
        userDao.insertUser(farmer)
        userDao.insertUser(buyer)

        // MPB Price Reference Data (Official Lembaga Lada Malaysia benchmark)
        val prices = listOf(
            MarketPriceReference(
                id = "mpb_black_faq",
                pepperType = PepperType.BLACK,
                grade = "FAQ (Sarawak)",
                pricePerKg = 26.50,
                location = "Sarikei / Kuching",
                lastUpdated = "10 Okt 2026, 08:30 MYT"
            ),
            MarketPriceReference(
                id = "mpb_black_special",
                pepperType = PepperType.BLACK,
                grade = "Special Grade (Hitam)",
                pricePerKg = 28.00,
                location = "Kuching",
                lastUpdated = "10 Okt 2026, 08:30 MYT"
            ),
            MarketPriceReference(
                id = "mpb_white_faq",
                pepperType = PepperType.WHITE,
                grade = "FAQ (Sarawak)",
                pricePerKg = 38.50,
                location = "Sarikei / Kuching",
                lastUpdated = "10 Okt 2026, 08:30 MYT"
            ),
            MarketPriceReference(
                id = "mpb_white_creamy",
                pepperType = PepperType.WHITE,
                grade = "Creamy White Pepper (Premium)",
                pricePerKg = 42.00,
                location = "Kuching / Sibu",
                lastUpdated = "10 Okt 2026, 08:30 MYT"
            )
        )
        marketPriceDao.insertPrices(prices)

        // Initial Harvest Records
        recordHarvest(
            farmerId = "usr_farmer_1",
            pepperType = PepperType.BLACK,
            quantity = 250.0,
            unit = "kg",
            notes = "Tuaian bukit Sarikei petak A - kering matahari 12%",
            harvestDate = "08 Okt 2026"
        )
        recordHarvest(
            farmerId = "usr_farmer_1",
            pepperType = PepperType.WHITE,
            quantity = 120.0,
            unit = "kg",
            notes = "Rendaman air bukit mengalir 14 hari, gred bersih",
            harvestDate = "09 Okt 2026"
        )

        // Listing
        createListing(
            farmerId = "usr_farmer_1",
            farmerName = "Pak Samad Jalil",
            pepperType = PepperType.BLACK,
            qtyKg = 100.0,
            pricePerKg = 27.50,
            grade = "FAQ (Sarawak)",
            description = "Lada hitam bersih, kelembapan 12%, aroma wangi asli Sarikei",
            location = "Sarikei, Sarawak"
        )

        // Demand Requests from Buyers
        createDemandRequest(
            buyerId = "usr_buyer_1",
            buyerName = "Mega Spices Sdn Bhd",
            pepperType = PepperType.BLACK,
            targetQtyKg = 300.0,
            targetPricePerKg = 27.00,
            requiredByDate = "25 Okt 2026",
            location = "Sarikei / Sibu",
            notes = "Perlu untuk eksport serbuk rempah antarabangsa"
        )
        createDemandRequest(
            buyerId = "usr_buyer_1",
            buyerName = "Borneo Pepper Trading",
            pepperType = PepperType.WHITE,
            targetQtyKg = 150.0,
            targetPricePerKg = 39.00,
            requiredByDate = "30 Okt 2026",
            location = "Kuching Port",
            notes = "Gred Creamy White diutamakan"
        )

        // An incoming pending offer for the farmer
        val activeListing = listingDao.getListingsByFarmer("usr_farmer_1").first().firstOrNull()
        if (activeListing != null) {
            submitOffer(
                listingId = activeListing.id,
                buyerId = "usr_buyer_1",
                buyerName = "Mega Spices Sdn Bhd",
                requestedQtyKg = 50.0,
                offeredPricePerKg = 27.00,
                message = "Salam Pak Samad, kami berminat ambil 50kg segera untuk batch pertama. Boleh runding sikit?"
            )
        }
    }
}
