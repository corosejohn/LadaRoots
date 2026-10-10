package com.example.ladaroots.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
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
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.data.model.UserProfile
import com.example.ladaroots.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromPepperType(value: PepperType): String = value.name

    @TypeConverter
    fun toPepperType(value: String): PepperType = PepperType.valueOf(value)

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromListingStatus(value: ListingStatus): String = value.name

    @TypeConverter
    fun toListingStatus(value: String): ListingStatus = ListingStatus.valueOf(value)

    @TypeConverter
    fun fromOfferStatus(value: OfferStatus): String = value.name

    @TypeConverter
    fun toOfferStatus(value: String): OfferStatus = OfferStatus.valueOf(value)

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)

    @TypeConverter
    fun fromMovementType(value: MovementType): String = value.name

    @TypeConverter
    fun toMovementType(value: String): MovementType = MovementType.valueOf(value)

    @TypeConverter
    fun fromDemandStatus(value: DemandStatus): String = value.name

    @TypeConverter
    fun toDemandStatus(value: String): DemandStatus = DemandStatus.valueOf(value)
}

@Database(
    entities = [
        UserProfile::class,
        HarvestRecord::class,
        InventoryMovement::class,
        Listing::class,
        Offer::class,
        Order::class,
        DemandRequest::class,
        NotificationItem::class,
        MarketPriceReference::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LadaRootsDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun harvestDao(): HarvestDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun listingDao(): ListingDao
    abstract fun offerDao(): OfferDao
    abstract fun orderDao(): OrderDao
    abstract fun demandDao(): DemandDao
    abstract fun notificationDao(): NotificationDao
    abstract fun marketPriceDao(): MarketPriceDao

    companion object {
        @Volatile
        private var INSTANCE: LadaRootsDatabase? = null

        fun getDatabase(context: Context): LadaRootsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LadaRootsDatabase::class.java,
                    "ladaroots_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
