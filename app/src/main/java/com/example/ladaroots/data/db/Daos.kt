package com.example.ladaroots.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ladaroots.data.model.DemandRequest
import com.example.ladaroots.data.model.HarvestRecord
import com.example.ladaroots.data.model.InventoryMovement
import com.example.ladaroots.data.model.Listing
import com.example.ladaroots.data.model.MarketPriceReference
import com.example.ladaroots.data.model.NotificationItem
import com.example.ladaroots.data.model.Offer
import com.example.ladaroots.data.model.Order
import com.example.ladaroots.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getUserById(id: String): UserProfile?

    @Query("SELECT * FROM profiles")
    fun getAllUsers(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)
}

@Dao
interface HarvestDao {
    @Query("SELECT * FROM harvest_records WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getHarvestsByFarmer(farmerId: String): Flow<List<HarvestRecord>>

    @Query("SELECT * FROM harvest_records ORDER BY createdAt DESC")
    fun getAllHarvests(): Flow<List<HarvestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: HarvestRecord)

    @Query("SELECT SUM(qtyKg) FROM harvest_records WHERE farmerId = :farmerId AND pepperType = :pepperType")
    suspend fun getTotalHarvestedKg(farmerId: String, pepperType: String): Double?
}

@Dao
interface InventoryMovementDao {
    @Query("SELECT * FROM inventory_movements WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getMovementsByFarmer(farmerId: String): Flow<List<InventoryMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovement)
}

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings WHERE status IN ('ACTIVE', 'PARTIALLY_RESERVED') ORDER BY createdAt DESC")
    fun getActiveListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getListingsByFarmer(farmerId: String): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getListingById(id: String): Listing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: Listing)

    @Update
    suspend fun updateListing(listing: Listing)

    @Query("UPDATE listings SET reservedQtyKg = :reservedQty, status = :status WHERE id = :id")
    suspend fun updateListingReservation(id: String, reservedQty: Double, status: String)
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getOffersForFarmer(farmerId: String): Flow<List<Offer>>

    @Query("SELECT * FROM offers WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getOffersForBuyer(buyerId: String): Flow<List<Offer>>

    @Query("SELECT * FROM offers ORDER BY createdAt DESC")
    fun getAllOffers(): Flow<List<Offer>>

    @Query("SELECT * FROM offers WHERE id = :id")
    suspend fun getOfferById(id: String): Offer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer)

    @Update
    suspend fun updateOffer(offer: Offer)

    @Query("UPDATE offers SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOfferStatus(id: String, status: String, updatedAt: Long)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getOrdersForFarmer(farmerId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getOrdersForBuyer(buyerId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): Order?

    @Query("SELECT * FROM orders WHERE offerId = :offerId")
    suspend fun getOrderByOfferId(offerId: String): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String, updatedAt: Long)
}

@Dao
interface DemandDao {
    @Query("SELECT * FROM demand_requests WHERE status = 'OPEN' ORDER BY createdAt DESC")
    fun getOpenDemands(): Flow<List<DemandRequest>>

    @Query("SELECT * FROM demand_requests WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getDemandsByBuyer(buyerId: String): Flow<List<DemandRequest>>

    @Query("SELECT * FROM demand_requests ORDER BY createdAt DESC")
    fun getAllDemands(): Flow<List<DemandRequest>>

    @Query("SELECT * FROM demand_requests WHERE id = :id")
    suspend fun getDemandById(id: String): DemandRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDemand(demand: DemandRequest)

    @Update
    suspend fun updateDemand(demand: DemandRequest)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface MarketPriceDao {
    @Query("SELECT * FROM market_price_references")
    fun getAllPrices(): Flow<List<MarketPriceReference>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<MarketPriceReference>)
}
