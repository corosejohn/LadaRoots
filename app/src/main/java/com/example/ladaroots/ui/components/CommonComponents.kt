package com.example.ladaroots.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ladaroots.data.model.OfferStatus
import com.example.ladaroots.data.model.OrderStatus
import com.example.ladaroots.data.model.PepperStockSummary
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.data.model.UserRole
import com.example.ladaroots.ui.viewmodel.ScreenDestination
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedLight
import com.example.ui.theme.BlackPepperDark
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.HarvestGoldLight
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueLight
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WhitePepperBorder
import com.example.ui.theme.WhitePepperCream
import com.example.ui.theme.WhitePepperText

@Composable
fun PepperTypeBadge(
    pepperType: PepperType,
    modifier: Modifier = Modifier
) {
    val isBlack = pepperType == PepperType.BLACK
    val bg = if (isBlack) BlackPepperDark else WhitePepperCream
    val fg = if (isBlack) Color.White else WhitePepperText
    val borderCol = if (isBlack) Color.DarkGray else WhitePepperBorder

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isBlack) Color.White else Color(0xFF6C757D))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = pepperType.displayNameBm,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OfferStatusBadge(
    status: OfferStatus,
    modifier: Modifier = Modifier
) {
    val bg: Color
    val fg: Color
    when (status) {
        OfferStatus.PENDING -> { bg = HarvestGoldLight; fg = HarvestGold }
        OfferStatus.ACCEPTED -> { bg = SuccessGreenLight; fg = SuccessGreen }
        OfferStatus.REJECTED -> { bg = AlertRedLight; fg = AlertRed }
        OfferStatus.WITHDRAWN -> { bg = Color(0xFFE5E7EB); fg = Color(0xFF6B7280) }
        OfferStatus.EXPIRED -> { bg = Color(0xFFF3F4F6); fg = Color(0xFF9CA3AF) }
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = status.labelBm,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun OrderStatusBadge(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val bg: Color
    val fg: Color
    when (status) {
        OrderStatus.CONFIRMED -> { bg = InfoBlueLight; fg = InfoBlue }
        OrderStatus.PREPARING -> { bg = HarvestGoldLight; fg = HarvestGold }
        OrderStatus.READY_FOR_PICKUP -> { bg = MintSurface; fg = LeafGreen }
        OrderStatus.COMPLETED -> { bg = SuccessGreenLight; fg = SuccessGreen }
        OrderStatus.CANCELLED -> { bg = AlertRedLight; fg = AlertRed }
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = status.labelBm,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PepperStockCard(
    summary: PepperStockSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PepperTypeBadge(pepperType = summary.pepperType)
                Text(
                    text = "Baki Bersih",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Available Stock Primary Focus
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format("%.1f", summary.availableStockKg),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "kg tersedia",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ForestGreenPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-metrics row: Physical and Reserved
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Stok Fizikal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${String.format("%.1f", summary.physicalStockKg)} kg",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Ditempah (Pesanan)", fontSize = 11.sp, color = AlertRed)
                    Text(
                        text = "${String.format("%.1f", summary.reservedStockKg)} kg",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AlertRed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadaRootsTopBar(
    currentScreen: ScreenDestination,
    currentRole: UserRole,
    userName: String,
    unreadNotifCount: Int,
    isOffline: Boolean,
    onNavigate: (ScreenDestination) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onToggleOffline: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ForestGreenPrimary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        navigationIcon = {
            if (currentScreen != ScreenDestination.HOME_AI &&
                currentScreen != ScreenDestination.INVENTORY &&
                currentScreen != ScreenDestination.OFFERS
            ) {
                IconButton(
                    onClick = { onNavigate(ScreenDestination.HOME_AI) },
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
            } else {
                Text(
                    text = "🌱",
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                )
            }
        },
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LadaRoots",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Role Indicator Pill
                    Surface(
                        color = if (currentRole == UserRole.FARMER) Color(0xFF2D6A4F) else Color(0xFF0369A1),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = currentRole.labelBm,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = userName,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        },
        actions = {
            // Offline/Online toggle status icon
            IconButton(
                onClick = onToggleOffline,
                modifier = Modifier.testTag("network_toggle_button")
            ) {
                Icon(
                    imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                    contentDescription = if (isOffline) "Offline" else "Online",
                    tint = if (isOffline) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
                )
            }

            // Notifications Icon with badge
            IconButton(
                onClick = { onNavigate(ScreenDestination.NOTIFICATIONS) },
                modifier = Modifier.testTag("notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotifCount > 0) {
                            Badge(containerColor = AlertRed) {
                                Text(text = "$unreadNotifCount", color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifikasi")
                }
            }

            // Three-dot overflow menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("three_dot_menu_button")
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu Pilihan")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Penyenaraian Jualan (Listings)") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(ScreenDestination.LISTINGS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Pesanan (Orders)") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(ScreenDestination.ORDERS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Permintaan & Pasaran (Demand)") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(ScreenDestination.DEMAND_MARKET)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Harga Pasaran Rasmi MPB") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(ScreenDestination.MPB_PRICES)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tukar Peranan: ${if (currentRole == UserRole.FARMER) "Pembeli" else "Peladang"}") },
                        leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            val nextRole = if (currentRole == UserRole.FARMER) UserRole.BUYER else UserRole.FARMER
                            onSwitchRole(nextRole)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tetapan & Demo") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(ScreenDestination.SETTINGS)
                        }
                    )
                }
            }
        }
    )
}
