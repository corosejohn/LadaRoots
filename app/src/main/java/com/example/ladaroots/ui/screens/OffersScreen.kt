package com.example.ladaroots.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ladaroots.data.model.Offer
import com.example.ladaroots.data.model.OfferStatus
import com.example.ladaroots.data.model.UserRole
import com.example.ladaroots.ui.components.OfferStatusBadge
import com.example.ladaroots.ui.components.PepperTypeBadge
import com.example.ladaroots.ui.viewmodel.ScreenDestination
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SuccessGreen

@Composable
fun OffersScreen(
    currentRole: UserRole,
    offers: List<Offer>,
    focusedOfferId: String?,
    onAcceptOffer: (String) -> Unit,
    onRejectOffer: (String) -> Unit,
    onCounterOffer: (String, Double) -> Unit,
    onNavigate: (ScreenDestination, String?) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<OfferStatus?>(null) }
    var offerToAccept by remember { mutableStateOf<Offer?>(null) }
    var offerToReject by remember { mutableStateOf<Offer?>(null) }
    var offerToCounter by remember { mutableStateOf<Offer?>(null) }

    val pendingCount = offers.count { it.status == OfferStatus.PENDING }
    val filteredOffers = offers.filter { offer ->
        if (selectedFilter == null) true else offer.status == selectedFilter
    }.sortedByDescending { it.status == OfferStatus.PENDING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (currentRole == UserRole.FARMER) "Tawaran Pembeli" else "Tawaran Dihantar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Text(
                    text = "$pendingCount tawaran menunggu tindakan anda",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("Semua (${offers.size})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == OfferStatus.PENDING,
                    onClick = { selectedFilter = OfferStatus.PENDING },
                    label = { Text("Menunggu ($pendingCount)") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == OfferStatus.ACCEPTED,
                    onClick = { selectedFilter = OfferStatus.ACCEPTED },
                    label = { Text("Diterima") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == OfferStatus.REJECTED,
                    onClick = { selectedFilter = OfferStatus.REJECTED },
                    label = { Text("Ditolak") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredOffers.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        tint = ForestGreenLight,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tiada tawaran dalam kategori ini.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOffers) { offer ->
                    val isFocused = offer.id == focusedOfferId
                    OfferCard(
                        offer = offer,
                        isFarmer = currentRole == UserRole.FARMER,
                        isFocused = isFocused,
                        onAccept = { offerToAccept = offer },
                        onReject = { offerToReject = offer },
                        onCounter = { offerToCounter = offer },
                        onOpenOrder = { onNavigate(ScreenDestination.ORDERS, offer.id) }
                    )
                }
            }
        }
    }

    // Confirmation Dialogs
    offerToAccept?.let { offer ->
        AlertDialog(
            onDismissRequest = { offerToAccept = null },
            title = { Text("Terima Tawaran Pembeli?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tindakan ini akan mengesahkan transaksi komersial secara selamat:")
                    Text("• Pembeli: ${offer.buyerName}", fontWeight = FontWeight.SemiBold)
                    Text("• Kuantiti: ${offer.qtyKg} kg ${offer.pepperType.displayNameBm}")
                    Text("• Harga: RM${offer.pricePerKg}/kg (Jumlah: RM${String.format("%.2f", offer.totalPrice)})", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                    Surface(color = MintSurface, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "ℹ️ Sistem akan mengunci stok (Reserved Stock) secara atomik dan mencipta 1 Pesanan rasmi.",
                            fontSize = 11.sp,
                            color = ForestGreenPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAcceptOffer(offer.id)
                        offerToAccept = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("confirm_accept_offer_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sahkan & Kunci Stok")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { offerToAccept = null }) {
                    Text("Batal")
                }
            }
        )
    }

    offerToReject?.let { offer ->
        AlertDialog(
            onDismissRequest = { offerToReject = null },
            title = { Text("Tolak Tawaran?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Adakah anda pasti ingin menolak tawaran daripada ${offer.buyerName} untuk ${offer.qtyKg} kg ${offer.pepperType.displayNameBm}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectOffer(offer.id)
                        offerToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    modifier = Modifier.testTag("confirm_reject_offer_button")
                ) {
                    Text("Tolak Tawaran")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { offerToReject = null }) {
                    Text("Batal")
                }
            }
        )
    }

    offerToCounter?.let { offer ->
        var counterPriceString by remember { mutableStateOf("${offer.pricePerKg + 1.0}") }
        AlertDialog(
            onDismissRequest = { offerToCounter = null },
            title = { Text("Hantar Tawaran Balas (Rundingan)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tawaran Asal Pembeli: RM${offer.pricePerKg}/kg", fontSize = 13.sp)
                    OutlinedTextField(
                        value = counterPriceString,
                        onValueChange = { counterPriceString = it },
                        label = { Text("Harga Cadangan Anda (RM/kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("counter_price_input")
                    )
                    val cp = counterPriceString.toDoubleOrNull() ?: 0.0
                    if (cp > 0) {
                        Text(
                            text = "Nilai Jumlah Baru: RM${String.format("%.2f", cp * offer.qtyKg)}",
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cp = counterPriceString.toDoubleOrNull() ?: 0.0
                        if (cp > 0) {
                            onCounterOffer(offer.id, cp)
                            offerToCounter = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier.testTag("submit_counter_offer_button")
                ) {
                    Text("Hantar Rundingan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { offerToCounter = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun OfferCard(
    offer: Offer,
    isFarmer: Boolean,
    isFocused: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCounter: () -> Unit,
    onOpenOrder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offer_card_${offer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) MintSurface else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isFocused) 4.dp else 1.dp),
        border = if (isFocused) androidx.compose.foundation.BorderStroke(2.dp, ForestGreenPrimary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Pepper type + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PepperTypeBadge(pepperType = offer.pepperType)
                OfferStatusBadge(status = offer.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantities and pricing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${String.format("%.1f", offer.qtyKg)} kg",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@ RM${String.format("%.2f", offer.pricePerKg)} / kg",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RM ${String.format("%.2f", offer.totalPrice)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                    Text(
                        text = "Nilai Jumlah",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Buyer info & message
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Pembeli: ${offer.buyerName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (offer.message.isNotBlank()) {
                        Text(
                            text = "\"${offer.message}\"",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (offer.counterPricePerKg != null) {
                        Text(
                            text = "💡 Harga Rundingan Balas: RM${offer.counterPricePerKg}/kg",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarvestGold
                        )
                    }
                }
            }

            // Action Buttons for Farmer on PENDING offers
            if (isFarmer && offer.status == OfferStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("accept_offer_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terima", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onCounter,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("counter_offer_button")
                    ) {
                        Text("Runding", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("reject_offer_button")
                    ) {
                        Text("Tolak", fontSize = 12.sp)
                    }
                }
            } else if (offer.status == OfferStatus.ACCEPTED) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenLight),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lihat Status Pesanan Berkenaan", fontSize = 12.sp)
                }
            }
        }
    }
}
