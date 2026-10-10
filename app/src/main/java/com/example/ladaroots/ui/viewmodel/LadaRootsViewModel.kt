package com.example.ladaroots.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladaroots.ai.AiActionProposal
import com.example.ladaroots.ai.AiAssistantEngine
import com.example.ladaroots.ai.ChatMessage
import com.example.ladaroots.data.db.LadaRootsDatabase
import com.example.ladaroots.data.model.DemandRequest
import com.example.ladaroots.data.model.HarvestRecord
import com.example.ladaroots.data.model.InventoryMovement
import com.example.ladaroots.data.model.Listing
import com.example.ladaroots.data.model.MarketPriceReference
import com.example.ladaroots.data.model.NotificationItem
import com.example.ladaroots.data.model.Offer
import com.example.ladaroots.data.model.Order
import com.example.ladaroots.data.model.OrderStatus
import com.example.ladaroots.data.model.PepperStockSummary
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.data.model.UserProfile
import com.example.ladaroots.data.model.UserRole
import com.example.ladaroots.data.repository.DemandMarketInsight
import com.example.ladaroots.data.repository.LadaRepository
import com.example.ladaroots.data.repository.TransactionResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    HOME_AI,
    INVENTORY,
    OFFERS,
    LISTINGS,
    ORDERS,
    DEMAND_MARKET,
    MPB_PRICES,
    NOTIFICATIONS,
    SETTINGS
}

data class UiNotificationEvent(val message: String, val isError: Boolean = false)

class LadaRootsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LadaRootsDatabase.getDatabase(application)
    val repository = LadaRepository(db)
    private val aiEngine = AiAssistantEngine()

    // Active User / Role
    private val _currentRole = MutableStateFlow(UserRole.FARMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentUser = MutableStateFlow(
        UserProfile("usr_farmer_1", "Pak Samad Jalil", UserRole.FARMER, "Sarikei, Sarawak", "+60 19-882 3411", "👨‍🌾")
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Navigation Destination
    private val _currentScreen = MutableStateFlow(ScreenDestination.HOME_AI)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Selected offer / order deep-link target if any
    private val _focusedEntityId = MutableStateFlow<String?>(null)
    val focusedEntityId: StateFlow<String?> = _focusedEntityId.asStateFlow()

    // Notification toast events
    private val _eventFlow = MutableSharedFlow<UiNotificationEvent>()
    val eventFlow: SharedFlow<UiNotificationEvent> = _eventFlow.asSharedFlow()

    // Offline simulation flag
    private val _isOfflineSimulated = MutableStateFlow(false)
    val isOfflineSimulated: StateFlow<Boolean> = _isOfflineSimulated.asStateFlow()

    // Data streams
    val activeListings: StateFlow<List<Listing>> = repository.getActiveListings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmerListings: StateFlow<List<Listing>> = repository.getListingsByFarmer("usr_farmer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmerHarvests: StateFlow<List<HarvestRecord>> = repository.getHarvestsByFarmer("usr_farmer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmerMovements: StateFlow<List<InventoryMovement>> = repository.getMovementsByFarmer("usr_farmer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmerOffers: StateFlow<List<Offer>> = repository.getOffersForFarmer("usr_farmer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buyerOffers: StateFlow<List<Offer>> = repository.getOffersForBuyer("usr_buyer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmerOrders: StateFlow<List<Order>> = repository.getOrdersForFarmer("usr_farmer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buyerOrders: StateFlow<List<Order>> = repository.getOrdersForBuyer("usr_buyer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openDemands: StateFlow<List<DemandRequest>> = repository.getOpenDemands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketPrices: StateFlow<List<MarketPriceReference>> = repository.getMarketPrices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmerNotifications: StateFlow<List<NotificationItem>> = repository.getNotificationsForUser("usr_farmer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = repository.getUnreadNotificationCount("usr_farmer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stockBlack: StateFlow<PepperStockSummary> = repository.getStockSummary("usr_farmer_1", PepperType.BLACK)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PepperStockSummary(PepperType.BLACK, 0.0, 0.0, 0.0, 0.0))

    val stockWhite: StateFlow<PepperStockSummary> = repository.getStockSummary("usr_farmer_1", PepperType.WHITE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PepperStockSummary(PepperType.WHITE, 0.0, 0.0, 0.0, 0.0))

    val marketInsights: StateFlow<Map<PepperType, DemandMarketInsight>> = repository.getMarketInsights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Chat history
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "init_1",
                text = "Selamat datang ke LadaRoots. Nak rekod hasil lada, semak stok atau mula menjual hari ini?",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
        }
    }

    fun navigateTo(screen: ScreenDestination, entityId: String? = null) {
        _focusedEntityId.value = entityId
        _currentScreen.value = screen
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        if (role == UserRole.FARMER) {
            _currentUser.value = UserProfile("usr_farmer_1", "Pak Samad Jalil", UserRole.FARMER, "Sarikei, Sarawak", "+60 19-882 3411", "👨‍🌾")
        } else {
            _currentUser.value = UserProfile("usr_buyer_1", "Mega Spices Sdn Bhd", UserRole.BUYER, "Kuching, Sarawak", "+60 12-871 9090", "🏢")
        }
        viewModelScope.launch {
            _eventFlow.emit(UiNotificationEvent("Mod ditukar kepada: ${role.labelBm}"))
        }
    }

    fun toggleOfflineSimulation() {
        _isOfflineSimulated.value = !_isOfflineSimulated.value
        val state = if (_isOfflineSimulated.value) "Luar Talian (Offline)" else "Dalam Talian (Online)"
        viewModelScope.launch {
            _eventFlow.emit(UiNotificationEvent("Mod rangkaian: $state"))
        }
    }

    fun sendUserMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(id = "user_${System.currentTimeMillis()}", text = text, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        val aiMsg = aiEngine.processMessage(
            userText = text,
            stockBlack = stockBlack.value,
            stockWhite = stockWhite.value,
            insights = marketInsights.value
        )
        _chatMessages.value = _chatMessages.value + aiMsg
    }

    fun executeProposal(proposal: AiActionProposal) {
        when (proposal) {
            is AiActionProposal.RecordHarvestProposal -> {
                recordHarvest(
                    pepperType = proposal.pepperType,
                    qty = proposal.quantity,
                    unit = proposal.unit,
                    notes = proposal.notes
                )
            }
            is AiActionProposal.CreateListingProposal -> {
                createListing(
                    pepperType = proposal.pepperType,
                    qtyKg = proposal.quantityKg,
                    pricePerKg = proposal.askingPricePerKg
                )
            }
            is AiActionProposal.NavigateToInventory -> navigateTo(ScreenDestination.INVENTORY)
            is AiActionProposal.NavigateToOffers -> navigateTo(ScreenDestination.OFFERS)
            is AiActionProposal.NavigateToDemand -> navigateTo(ScreenDestination.DEMAND_MARKET)
            AiActionProposal.None -> {}
        }
    }

    fun recordHarvest(
        pepperType: PepperType,
        qty: Double,
        unit: String,
        notes: String = "",
        harvestDate: String = "Hari ini"
    ) {
        viewModelScope.launch {
            val result = repository.recordHarvest(
                farmerId = "usr_farmer_1",
                pepperType = pepperType,
                quantity = qty,
                unit = unit,
                notes = notes,
                harvestDate = harvestDate
            )
            when (result) {
                is TransactionResult.Success -> {
                    _eventFlow.emit(UiNotificationEvent(result.message))
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        id = "ai_ack_${System.currentTimeMillis()}",
                        text = "✅ ${result.message}\nStok fizikal kini telah dikemaskini.",
                        isUser = false
                    )
                }
                is TransactionResult.Error -> {
                    _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
                }
            }
        }
    }

    fun createListing(
        pepperType: PepperType,
        qtyKg: Double,
        pricePerKg: Double,
        grade: String = "FAQ (Sarawak)",
        description: String = ""
    ) {
        viewModelScope.launch {
            val result = repository.createListing(
                farmerId = "usr_farmer_1",
                farmerName = currentUser.value.name,
                pepperType = pepperType,
                qtyKg = qtyKg,
                pricePerKg = pricePerKg,
                grade = grade,
                description = description
            )
            when (result) {
                is TransactionResult.Success -> {
                    _eventFlow.emit(UiNotificationEvent(result.message))
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        id = "ai_ack_${System.currentTimeMillis()}",
                        text = "📢 Penyenaraian ${qtyKg}kg ${pepperType.displayNameBm} pada RM$pricePerKg/kg telah aktif di pasaran pembeli.",
                        isUser = false
                    )
                }
                is TransactionResult.Error -> {
                    _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
                }
            }
        }
    }

    fun submitOffer(
        listingId: String,
        requestedQtyKg: Double,
        offeredPricePerKg: Double,
        message: String
    ) {
        viewModelScope.launch {
            val result = repository.submitOffer(
                listingId = listingId,
                buyerId = "usr_buyer_1",
                buyerName = "Mega Spices Sdn Bhd",
                requestedQtyKg = requestedQtyKg,
                offeredPricePerKg = offeredPricePerKg,
                message = message
            )
            when (result) {
                is TransactionResult.Success -> {
                    _eventFlow.emit(UiNotificationEvent(result.message))
                }
                is TransactionResult.Error -> {
                    _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
                }
            }
        }
    }

    fun acceptOffer(offerId: String) {
        viewModelScope.launch {
            val result = repository.acceptOffer("usr_farmer_1", offerId)
            when (result) {
                is TransactionResult.Success -> {
                    _eventFlow.emit(UiNotificationEvent(result.message))
                }
                is TransactionResult.Error -> {
                    _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
                }
            }
        }
    }

    fun rejectOffer(offerId: String) {
        viewModelScope.launch {
            val result = repository.rejectOffer("usr_farmer_1", offerId)
            when (result) {
                is TransactionResult.Success -> _eventFlow.emit(UiNotificationEvent(result.message))
                is TransactionResult.Error -> _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
            }
        }
    }

    fun counterOffer(offerId: String, counterPrice: Double) {
        viewModelScope.launch {
            val result = repository.counterOffer("usr_farmer_1", offerId, counterPrice)
            when (result) {
                is TransactionResult.Success -> _eventFlow.emit(UiNotificationEvent(result.message))
                is TransactionResult.Error -> _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
            }
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.completeOrder(orderId)
            when (result) {
                is TransactionResult.Success -> _eventFlow.emit(UiNotificationEvent(result.message))
                is TransactionResult.Error -> _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
            }
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.cancelOrder(orderId, reason)
            when (result) {
                is TransactionResult.Success -> _eventFlow.emit(UiNotificationEvent(result.message))
                is TransactionResult.Error -> _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
            }
        }
    }

    fun updateOrderStage(orderId: String, stage: OrderStatus) {
        viewModelScope.launch {
            val result = repository.updateOrderStage(orderId, stage)
            when (result) {
                is TransactionResult.Success -> _eventFlow.emit(UiNotificationEvent(result.message))
                is TransactionResult.Error -> _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
            }
        }
    }

    fun createDemandRequest(
        pepperType: PepperType,
        targetQtyKg: Double,
        targetPricePerKg: Double,
        requiredByDate: String,
        location: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.createDemandRequest(
                buyerId = "usr_buyer_1",
                buyerName = currentUser.value.name,
                pepperType = pepperType,
                targetQtyKg = targetQtyKg,
                targetPricePerKg = targetPricePerKg,
                requiredByDate = requiredByDate,
                location = location,
                notes = notes
            )
            when (result) {
                is TransactionResult.Success -> _eventFlow.emit(UiNotificationEvent(result.message))
                is TransactionResult.Error -> _eventFlow.emit(UiNotificationEvent(result.message, isError = true))
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(currentUser.value.id)
            _eventFlow.emit(UiNotificationEvent("Semua notifikasi ditandakan sebagai dibaca."))
        }
    }
}
