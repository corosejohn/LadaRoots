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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.example.ladaroots.data.model.Listing
import com.example.ladaroots.data.model.ListingStatus
import com.example.ladaroots.data.model.PepperStockSummary
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.data.model.UserRole
import com.example.ladaroots.ui.components.PepperTypeBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SoftMint

@Composable
fun ListingsScreen(
    currentRole: UserRole,
    listings: List<Listing>,
    stockBlack: PepperStockSummary,
    stockWhite: PepperStockSummary,
    onCreateListing: (PepperType, Double, Double, String, String) -> Unit,
    onSubmitOffer: (String, Double, Double, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedListingForOffer by remember { mutableStateOf<Listing?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = if (currentRole == UserRole.FARMER) "Penyenaraian Jualan Saya" else "Pasaran Lada Aktif",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
            Text(
                text = if (currentRole == UserRole.FARMER)
                    "Urus kuantiti jualan daripada stok sedia ada anda."
                else
                    "Pilih lada daripada peladang tempatan dan kemukakan tawaran.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (listings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = ForestGreenLight, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentRole == UserRole.FARMER)
                                "Anda belum menerbitkan sebarang penyenaraian jualan."
                            else
                                "Tiada penyenaraian jualan aktif di pasaran sekarang.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(listings) { item ->
                        ListingCard(
                            listing = item,
                            isBuyer = currentRole == UserRole.BUYER,
                            onOfferClick = { selectedListingForOffer = item }
                        )
                    }
                }
            }
        }

        // FAB for Farmer to create listing
        if (currentRole == UserRole.FARMER) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("create_listing_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Iklan Baru")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Cipta Jualan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // Farmer Create Listing Dialog
    if (showCreateDialog) {
        CreateListingDialog(
            stockBlack = stockBlack,
            stockWhite = stockWhite,
            onDismiss = { showCreateDialog = false },
            onConfirm = { type, qty, price, grade, desc ->
                onCreateListing(type, qty, price, grade, desc)
                showCreateDialog = false
            }
        )
    }

    // Buyer Submit Offer Dialog
    selectedListingForOffer?.let { listing ->
        SubmitOfferDialog(
            listing = listing,
            onDismiss = { selectedListingForOffer = null },
            onConfirm = { qty, price, msg ->
                onSubmitOffer(listing.id, qty, price, msg)
                selectedListingForOffer = null
            }
        )
    }
}

@Composable
fun ListingCard(
    listing: Listing,
    isBuyer: Boolean,
    onOfferClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("listing_card_${listing.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PepperTypeBadge(pepperType = listing.pepperType)
                Surface(
                    color = if (listing.status == ListingStatus.ACTIVE) SoftMint else MintSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (listing.status == ListingStatus.ACTIVE) "Aktif" else "Separa Ditempah",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Baki Tersedia: ${String.format("%.1f", listing.availableQtyKg)} kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                    Text(
                        text = "Jumlah Iklan: ${listing.qtyKg} kg (${listing.reservedQtyKg} kg ditempah)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RM${String.format("%.2f", listing.pricePerKg)}/kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenLight
                    )
                    Text(
                        text = "Gred: ${listing.grade}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = ForestGreenLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${listing.location} • Peladang: ${listing.farmerName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (listing.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listing.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isBuyer) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOfferClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("submit_offer_button_${listing.id}")
                ) {
                    Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Hantar Tawaran Harga", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun CreateListingDialog(
    stockBlack: PepperStockSummary,
    stockWhite: PepperStockSummary,
    onDismiss: () -> Unit,
    onConfirm: (PepperType, Double, Double, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(PepperType.BLACK) }
    var qtyString by remember { mutableStateOf("") }
    var priceString by remember { mutableStateOf("27.50") }
    var grade by remember { mutableStateOf("FAQ (Sarawak)") }
    var desc by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val availableForType = if (selectedType == PepperType.BLACK) stockBlack.availableStockKg else stockWhite.availableStockKg
    val qty = qtyString.toDoubleOrNull() ?: 0.0
    val price = priceString.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terbitkan Penyenaraian Jualan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Pilih Jenis Lada:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { selectedType = PepperType.BLACK },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == PepperType.BLACK) ForestGreenPrimary else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Lada Hitam")
                    }
                    Button(
                        onClick = { selectedType = PepperType.WHITE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == PepperType.WHITE) ForestGreenPrimary else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Lada Putih")
                    }
                }

                Surface(color = MintSurface, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "Baki Stok Tersedia Anda: ${String.format("%.1f", availableForType)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { qtyString = it },
                    label = { Text("Kuantiti Ingin Dijual (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("listing_qty_input")
                )

                OutlinedTextField(
                    value = priceString,
                    onValueChange = { priceString = it },
                    label = { Text("Harga Dituntut (RM/kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("listing_price_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Penerangan Kualiti / Lokasi") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (qty > 0 && price > 0) {
                    Text(
                        text = "Anggaran Nilai Kasar: RM${String.format("%.2f", qty * price)}",
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        fontSize = 13.sp
                    )
                }

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = AlertRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (qty <= 0) {
                        errorMsg = "Kuantiti mestilah melebihi 0 kg."
                    } else if (qty > availableForType) {
                        errorMsg = "Kuantiti melebihi stok tersedia (${availableForType} kg)!"
                    } else if (price <= 0) {
                        errorMsg = "Harga mestilah sah."
                    } else {
                        onConfirm(selectedType, qty, price, grade, desc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("submit_create_listing_button")
            ) {
                Text("Terbitkan Iklan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun SubmitOfferDialog(
    listing: Listing,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, String) -> Unit
) {
    var qtyString by remember { mutableStateOf("${listing.availableQtyKg.coerceAtMost(50.0)}") }
    var priceString by remember { mutableStateOf("${listing.pricePerKg}") }
    var message by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val qty = qtyString.toDoubleOrNull() ?: 0.0
    val price = priceString.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kemukakan Tawaran Pembeli", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Peladang: ${listing.farmerName} • ${listing.pepperType.displayNameBm}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Harga Diminta: RM${listing.pricePerKg}/kg (Tersedia: ${listing.availableQtyKg} kg)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { qtyString = it },
                    label = { Text("Kuantiti Diminta (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("offer_qty_input")
                )

                OutlinedTextField(
                    value = priceString,
                    onValueChange = { priceString = it },
                    label = { Text("Harga Tawaran Anda (RM/kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("offer_price_input")
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Mesej kepada Peladang (Pilihan)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (qty > 0 && price > 0) {
                    Text(
                        text = "Jumlah Nilai Tawaran: RM${String.format("%.2f", qty * price)}",
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        fontSize = 13.sp
                    )
                }

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = AlertRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (qty <= 0 || qty > listing.availableQtyKg) {
                        errorMsg = "Kuantiti tidak sah (Maksimum ${listing.availableQtyKg} kg)."
                    } else if (price <= 0) {
                        errorMsg = "Sila masukkan harga yang sah."
                    } else {
                        onConfirm(qty, price, message)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("send_buyer_offer_button")
            ) {
                Text("Hantar Tawaran")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
