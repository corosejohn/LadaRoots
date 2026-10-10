package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ladaroots.data.db.LadaRootsDatabase
import com.example.ladaroots.data.model.DemandStatus
import com.example.ladaroots.data.model.ListingStatus
import com.example.ladaroots.data.model.OrderStatus
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.data.model.UserProfile
import com.example.ladaroots.data.model.UserRole
import com.example.ladaroots.data.repository.LadaRepository
import com.example.ladaroots.data.repository.TransactionResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LadaRootsAcceptanceTest {

    private lateinit var db: LadaRootsDatabase
    private lateinit var repository: LadaRepository
    private val farmerId = "farmer_101"
    private val buyerId = "buyer_202"

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LadaRootsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LadaRepository(db)

        db.userDao().insertUser(
            UserProfile(farmerId, "Pak Jalil", UserRole.FARMER, "Sarikei", "019-1111111")
        )
        db.userDao().insertUser(
            UserProfile(buyerId, "Pemborong Spices", UserRole.BUYER, "Kuching", "012-2222222")
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun test01_record120kgBlackPepper() = runBlocking {
        val res = repository.recordHarvest(
            farmerId = farmerId,
            pepperType = PepperType.BLACK,
            quantity = 120.0,
            unit = "kg",
            notes = "Petak bukit"
        )
        assertTrue(res is TransactionResult.Success)
        val harvest = (res as TransactionResult.Success).data
        assertEquals(120.0, harvest.qtyKg, 0.01)

        val stock = repository.getStockSummary(farmerId, PepperType.BLACK).first()
        assertEquals(120.0, stock.physicalStockKg, 0.01)
        assertEquals(120.0, stock.availableStockKg, 0.01)
    }

    @Test
    fun test02_halfTonneNormalisesTo500kg() = runBlocking {
        val res = repository.recordHarvest(
            farmerId = farmerId,
            pepperType = PepperType.WHITE,
            quantity = 0.5,
            unit = "tan",
            notes = "Tuaian gred A"
        )
        assertTrue(res is TransactionResult.Success)
        val harvest = (res as TransactionResult.Success).data
        assertEquals(500.0, harvest.qtyKg, 0.01)
        assertEquals("tan", harvest.originalUnit)
    }

    @Test
    fun test03_harvestTimestampsRecordedCorrectly() = runBlocking {
        val before = System.currentTimeMillis()
        val res = repository.recordHarvest(
            farmerId = farmerId,
            pepperType = PepperType.BLACK,
            quantity = 40.0,
            unit = "kg"
        )
        val after = System.currentTimeMillis()
        val harvest = (res as TransactionResult.Success).data
        assertTrue(harvest.createdAt in before..after)
    }

    @Test
    fun test04_farmerCannotListMoreThanAvailableStock() = runBlocking {
        // Stock: 100 kg
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")

        // Try listing 150 kg
        val res = repository.createListing(
            farmerId = farmerId,
            farmerName = "Pak Jalil",
            pepperType = PepperType.BLACK,
            qtyKg = 150.0,
            pricePerKg = 25.0
        )
        assertTrue(res is TransactionResult.Error)
        assertTrue((res as TransactionResult.Error).message.contains("melebihi"))
    }

    @Test
    fun test05_buyerCanOfferAgainstValidListing() = runBlocking {
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")
        val listRes = repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 80.0, 26.0)
        val listing = (listRes as TransactionResult.Success).data

        val offerRes = repository.submitOffer(
            listingId = listing.id,
            buyerId = buyerId,
            buyerName = "Pemborong Spices",
            requestedQtyKg = 50.0,
            offeredPricePerKg = 25.50,
            message = "Ambil segera"
        )
        assertTrue(offerRes is TransactionResult.Success)
        val offer = (offerRes as TransactionResult.Success).data
        assertEquals(50.0 * 25.50, offer.totalPrice, 0.01)
    }

    @Test
    fun test06_buyerCanCreateDemandRequestWithoutCreatingOrder() = runBlocking {
        val demRes = repository.createDemandRequest(
            buyerId = buyerId,
            buyerName = "Pemborong Spices",
            pepperType = PepperType.BLACK,
            targetQtyKg = 200.0,
            targetPricePerKg = 27.0,
            requiredByDate = "15 Nov 2026",
            location = "Kuching"
        )
        assertTrue(demRes is TransactionResult.Success)
        val demand = (demRes as TransactionResult.Success).data
        assertEquals(DemandStatus.OPEN, demand.status)

        // Ensure no orders exist
        val orders = repository.getOrdersForBuyer(buyerId).first()
        assertTrue(orders.isEmpty())
    }

    @Test
    fun test07_notificationOpensCorrectOffer() = runBlocking {
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")
        val listing = (repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 80.0, 26.0) as TransactionResult.Success).data

        val offer = (repository.submitOffer(listing.id, buyerId, "Pemborong Spices", 30.0, 25.0) as TransactionResult.Success).data
        val notifs = repository.getNotificationsForUser(farmerId).first()
        val notif = notifs.firstOrNull { it.entityType == "OFFER" }
        assertNotNull(notif)
        assertEquals(offer.id, notif?.entityId)
    }

    @Test
    fun test08_acceptingValidOfferCreatesExactlyOneOrder() = runBlocking {
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")
        val listing = (repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 80.0, 26.0) as TransactionResult.Success).data
        val offer = (repository.submitOffer(listing.id, buyerId, "Pemborong Spices", 40.0, 26.0) as TransactionResult.Success).data

        val acceptRes = repository.acceptOffer(farmerId, offer.id)
        assertTrue(acceptRes is TransactionResult.Success)
        val order = (acceptRes as TransactionResult.Success).data
        assertEquals(40.0, order.qtyKg, 0.01)
        assertEquals(OrderStatus.CONFIRMED, order.status)

        // Check stock reservations
        val stock = repository.getStockSummary(farmerId, PepperType.BLACK).first()
        assertEquals(100.0, stock.physicalStockKg, 0.01)
        assertEquals(40.0, stock.reservedStockKg, 0.01)
        assertEquals(60.0, stock.availableStockKg, 0.01)
    }

    @Test
    fun test09_repeatedAcceptanceDoesNotCreateDuplicateOrders() = runBlocking {
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")
        val listing = (repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 80.0, 26.0) as TransactionResult.Success).data
        val offer = (repository.submitOffer(listing.id, buyerId, "Pemborong Spices", 30.0, 26.0) as TransactionResult.Success).data

        repository.acceptOffer(farmerId, offer.id)
        val secondAttempt = repository.acceptOffer(farmerId, offer.id)

        val orders = repository.getOrdersForFarmer(farmerId).first()
        assertEquals(1, orders.size)
    }

    @Test
    fun test10_competingOffersCannotReserveMoreStockThanExists() = runBlocking {
        // Stock: 50 kg
        repository.recordHarvest(farmerId, PepperType.BLACK, 50.0, "kg")
        val listing = (repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 50.0, 26.0) as TransactionResult.Success).data

        val offer1 = (repository.submitOffer(listing.id, buyerId, "Buyer 1", 40.0, 26.0) as TransactionResult.Success).data
        val offer2 = (repository.submitOffer(listing.id, buyerId, "Buyer 2", 30.0, 26.0) as TransactionResult.Success).data

        // Accept offer 1 (reserves 40 kg, leaves 10 kg)
        repository.acceptOffer(farmerId, offer1.id)

        // Attempt to accept offer 2 (requests 30 kg > 10 kg available)
        val res2 = repository.acceptOffer(farmerId, offer2.id)
        assertTrue(res2 is TransactionResult.Error)
        assertTrue((res2 as TransactionResult.Error).message.contains("tidak mencukupi"))
    }

    @Test
    fun test11_cancellingOrderReleasesReservation() = runBlocking {
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")
        val listing = (repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 100.0, 26.0) as TransactionResult.Success).data
        val offer = (repository.submitOffer(listing.id, buyerId, "Buyer", 50.0, 26.0) as TransactionResult.Success).data
        val order = (repository.acceptOffer(farmerId, offer.id) as TransactionResult.Success).data

        // Stock reserved: 50 kg, available: 50 kg
        var stock = repository.getStockSummary(farmerId, PepperType.BLACK).first()
        assertEquals(50.0, stock.reservedStockKg, 0.01)

        // Cancel order
        val cancelRes = repository.cancelOrder(order.id, "Batal")
        assertTrue(cancelRes is TransactionResult.Success)

        // Reservation released! Available back to 100 kg
        stock = repository.getStockSummary(farmerId, PepperType.BLACK).first()
        assertEquals(0.0, stock.reservedStockKg, 0.01)
        assertEquals(100.0, stock.availableStockKg, 0.01)
    }

    @Test
    fun test12_completingOrderReducesPhysicalStockCorrectly() = runBlocking {
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")
        val listing = (repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 100.0, 26.0) as TransactionResult.Success).data
        val offer = (repository.submitOffer(listing.id, buyerId, "Buyer", 60.0, 26.0) as TransactionResult.Success).data
        val order = (repository.acceptOffer(farmerId, offer.id) as TransactionResult.Success).data

        // Complete order
        val compRes = repository.completeOrder(order.id)
        assertTrue(compRes is TransactionResult.Success)

        // Physical stock reduced to 40 kg, reserved released
        val stock = repository.getStockSummary(farmerId, PepperType.BLACK).first()
        assertEquals(40.0, stock.physicalStockKg, 0.01)
        assertEquals(0.0, stock.reservedStockKg, 0.01)
        assertEquals(40.0, stock.availableStockKg, 0.01)
    }

    @Test
    fun test13_unauthorisedUserCannotAcceptOffer() = runBlocking {
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")
        val listing = (repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 50.0, 26.0) as TransactionResult.Success).data
        val offer = (repository.submitOffer(listing.id, buyerId, "Buyer", 20.0, 26.0) as TransactionResult.Success).data

        val res = repository.acceptOffer("stranger_farmer_999", offer.id)
        assertTrue(res is TransactionResult.Error)
        assertTrue((res as TransactionResult.Error).message.contains("kebenaran"))
    }

    @Test
    fun test14_demandMatchingDoesNotDoubleCount() = runBlocking {
        repository.createDemandRequest(buyerId, "Buyer", PepperType.BLACK, 200.0, 27.0, "Nov 2026", "Kuching")
        repository.recordHarvest(farmerId, PepperType.BLACK, 100.0, "kg")
        repository.createListing(farmerId, "Pak Jalil", PepperType.BLACK, 80.0, 27.0)

        val insights = repository.getMarketInsights().first()
        val blackInsight = insights[PepperType.BLACK]
        assertNotNull(blackInsight)
        assertEquals(200.0, blackInsight!!.openDemandKg, 0.01)
        assertEquals(80.0, blackInsight.listedSupplyKg, 0.01)
        assertEquals(120.0, blackInsight.unmetDemandKg, 0.01)
        assertEquals(40, blackInsight.coveragePercent)
    }

    @Test
    fun test15_zeroOrNegativeValuesRejected() = runBlocking {
        val res = repository.recordHarvest(farmerId, PepperType.BLACK, -10.0, "kg")
        assertTrue(res is TransactionResult.Error)
    }

    @Test
    fun test16_importantMutationsConfirmedByDatabase() = runBlocking {
        val res = repository.recordHarvest(farmerId, PepperType.BLACK, 55.5, "kg", "Petak C")
        assertTrue(res is TransactionResult.Success)
        val allHarvests = db.harvestDao().getAllHarvests().first()
        assertTrue(allHarvests.any { it.qtyKg == 55.5 })
    }
}
