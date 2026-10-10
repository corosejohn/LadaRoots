package com.example.ladaroots.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ladaroots.data.model.OfferStatus
import com.example.ladaroots.data.model.UserRole
import com.example.ladaroots.ui.components.LadaRootsTopBar
import com.example.ladaroots.ui.screens.DemandMarketScreen
import com.example.ladaroots.ui.screens.HomeAiScreen
import com.example.ladaroots.ui.screens.InventoryScreen
import com.example.ladaroots.ui.screens.ListingsScreen
import com.example.ladaroots.ui.screens.MpbPricingScreen
import com.example.ladaroots.ui.screens.NotificationsScreen
import com.example.ladaroots.ui.screens.OffersScreen
import com.example.ladaroots.ui.screens.OrdersScreen
import com.example.ladaroots.ui.screens.SettingsScreen
import com.example.ladaroots.ui.viewmodel.LadaRootsViewModel
import com.example.ladaroots.ui.viewmodel.ScreenDestination
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintSurface
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LadaRootsApp(viewModel: LadaRootsViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val focusedEntityId by viewModel.focusedEntityId.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOfflineSimulated.collectAsStateWithLifecycle()

    val stockBlack by viewModel.stockBlack.collectAsStateWithLifecycle()
    val stockWhite by viewModel.stockWhite.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

    val farmerHarvests by viewModel.farmerHarvests.collectAsStateWithLifecycle()
    val farmerMovements by viewModel.farmerMovements.collectAsStateWithLifecycle()
    val activeListings by viewModel.activeListings.collectAsStateWithLifecycle()
    val farmerListings by viewModel.farmerListings.collectAsStateWithLifecycle()
    val farmerOffers by viewModel.farmerOffers.collectAsStateWithLifecycle()
    val buyerOffers by viewModel.buyerOffers.collectAsStateWithLifecycle()
    val farmerOrders by viewModel.farmerOrders.collectAsStateWithLifecycle()
    val buyerOrders by viewModel.buyerOrders.collectAsStateWithLifecycle()
    val openDemands by viewModel.openDemands.collectAsStateWithLifecycle()
    val marketPrices by viewModel.marketPrices.collectAsStateWithLifecycle()
    val notifications by viewModel.farmerNotifications.collectAsStateWithLifecycle()
    val unreadNotifCount by viewModel.unreadNotifCount.collectAsStateWithLifecycle()
    val marketInsights by viewModel.marketInsights.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            snackbarHostState.showSnackbar(event.message)
        }
    }

    // Hardware back press handling
    BackHandler(enabled = currentScreen != ScreenDestination.HOME_AI) {
        viewModel.navigateTo(ScreenDestination.HOME_AI)
    }

    val pendingOffersCount = if (currentRole == UserRole.FARMER) {
        farmerOffers.count { it.status == OfferStatus.PENDING }
    } else {
        buyerOffers.count { it.status == OfferStatus.PENDING }
    }

    Scaffold(
        topBar = {
            LadaRootsTopBar(
                currentScreen = currentScreen,
                currentRole = currentRole,
                userName = currentUser.name,
                unreadNotifCount = unreadNotifCount,
                isOffline = isOffline,
                onNavigate = { viewModel.navigateTo(it) },
                onSwitchRole = { viewModel.switchRole(it) },
                onToggleOffline = { viewModel.toggleOfflineSimulation() }
            )
        },
        bottomBar = {
            // Persistent Bottom Tab Bar with exactly 3 primary destinations
            NavigationBar(
                containerColor = Color.White,
                contentColor = ForestGreenPrimary,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Tab 1: Home AI
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.HOME_AI,
                    onClick = { viewModel.navigateTo(ScreenDestination.HOME_AI) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Home AI") },
                    label = { Text("Home AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreenPrimary,
                        selectedTextColor = ForestGreenPrimary,
                        indicatorColor = MintSurface
                    ),
                    modifier = Modifier.testTag("tab_home_ai")
                )

                // Tab 2: Inventory
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.INVENTORY,
                    onClick = { viewModel.navigateTo(ScreenDestination.INVENTORY) },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventori") },
                    label = { Text("Inventori", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreenPrimary,
                        selectedTextColor = ForestGreenPrimary,
                        indicatorColor = MintSurface
                    ),
                    modifier = Modifier.testTag("tab_inventory")
                )

                // Tab 3: Offers
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.OFFERS,
                    onClick = { viewModel.navigateTo(ScreenDestination.OFFERS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingOffersCount > 0) {
                                    Badge(containerColor = AlertRed) {
                                        Text(text = "$pendingOffersCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Handshake, contentDescription = "Tawaran")
                        }
                    },
                    label = { Text("Tawaran", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreenPrimary,
                        selectedTextColor = ForestGreenPrimary,
                        indicatorColor = MintSurface
                    ),
                    modifier = Modifier.testTag("tab_offers")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenDestination.HOME_AI -> {
                    HomeAiScreen(
                        messages = chatMessages,
                        stockBlack = stockBlack,
                        stockWhite = stockWhite,
                        onSendMessage = { viewModel.sendUserMessage(it) },
                        onExecuteProposal = { viewModel.executeProposal(it) },
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }

                ScreenDestination.INVENTORY -> {
                    InventoryScreen(
                        stockBlack = stockBlack,
                        stockWhite = stockWhite,
                        harvests = farmerHarvests,
                        movements = farmerMovements,
                        onAddHarvest = { type, qty, unit, notes ->
                            viewModel.recordHarvest(type, qty, unit, notes)
                        },
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }

                ScreenDestination.OFFERS -> {
                    OffersScreen(
                        currentRole = currentRole,
                        offers = if (currentRole == UserRole.FARMER) farmerOffers else buyerOffers,
                        focusedOfferId = focusedEntityId,
                        onAcceptOffer = { viewModel.acceptOffer(it) },
                        onRejectOffer = { viewModel.rejectOffer(it) },
                        onCounterOffer = { id, price -> viewModel.counterOffer(id, price) },
                        onNavigate = { screen, entityId -> viewModel.navigateTo(screen, entityId) }
                    )
                }

                ScreenDestination.LISTINGS -> {
                    ListingsScreen(
                        currentRole = currentRole,
                        listings = if (currentRole == UserRole.FARMER) farmerListings else activeListings,
                        stockBlack = stockBlack,
                        stockWhite = stockWhite,
                        onCreateListing = { type, qty, price, grade, desc ->
                            viewModel.createListing(type, qty, price, grade, desc)
                        },
                        onSubmitOffer = { listingId, qty, price, msg ->
                            viewModel.submitOffer(listingId, qty, price, msg)
                        }
                    )
                }

                ScreenDestination.ORDERS -> {
                    OrdersScreen(
                        currentRole = currentRole,
                        orders = if (currentRole == UserRole.FARMER) farmerOrders else buyerOrders,
                        onCompleteOrder = { viewModel.completeOrder(it) },
                        onCancelOrder = { id, reason -> viewModel.cancelOrder(id, reason) },
                        onUpdateStage = { id, stage -> viewModel.updateOrderStage(id, stage) }
                    )
                }

                ScreenDestination.DEMAND_MARKET -> {
                    DemandMarketScreen(
                        currentRole = currentRole,
                        insights = marketInsights,
                        demands = openDemands,
                        onCreateDemand = { type, qty, price, date, loc, notes ->
                            viewModel.createDemandRequest(type, qty, price, date, loc, notes)
                        },
                        onNavigateToListings = { viewModel.navigateTo(ScreenDestination.LISTINGS) }
                    )
                }

                ScreenDestination.MPB_PRICES -> {
                    MpbPricingScreen(prices = marketPrices)
                }

                ScreenDestination.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onNotificationClick = { notif ->
                            viewModel.markNotificationRead(notif.id)
                            when (notif.entityType) {
                                "OFFER" -> viewModel.navigateTo(ScreenDestination.OFFERS, notif.entityId)
                                "ORDER" -> viewModel.navigateTo(ScreenDestination.ORDERS, notif.entityId)
                                "LISTING" -> viewModel.navigateTo(ScreenDestination.LISTINGS, notif.entityId)
                                "DEMAND" -> viewModel.navigateTo(ScreenDestination.DEMAND_MARKET, notif.entityId)
                                else -> {}
                            }
                        },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() }
                    )
                }

                ScreenDestination.SETTINGS -> {
                    SettingsScreen(
                        currentRole = currentRole,
                        currentUser = currentUser,
                        isOffline = isOffline,
                        onSwitchRole = { viewModel.switchRole(it) },
                        onToggleOffline = { viewModel.toggleOfflineSimulation() }
                    )
                }
            }
        }
    }
}
