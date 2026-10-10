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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ladaroots.data.model.DemandRequest
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.data.model.UserRole
import com.example.ladaroots.data.repository.DemandMarketInsight
import com.example.ladaroots.ui.components.PepperTypeBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SuccessGreen

@Composable
fun DemandMarketScreen(
    currentRole: UserRole,
    insights: Map<PepperType, DemandMarketInsight>,
    demands: List<DemandRequest>,
    onCreateDemand: (PepperType, Double, Double, String, String, String) -> Unit,
    onNavigateToListings: () -> Unit
) {
    var showCreateDemandDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Permintaan-ke-Tuaian",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = "Wawasan pasaran dua hala (Pembeli vs Peladang)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Demand vs Supply Insight Cards for Black & White Pepper
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    insights[PepperType.BLACK]?.let { insight ->
                        DemandInsightCard(insight = insight, onAction = onNavigateToListings)
                    }
                    insights[PepperType.WHITE]?.let { insight ->
                        DemandInsightCard(insight = insight, onAction = onNavigateToListings)
                    }
                }
            }

            // Commercial Buyer Demand Requests Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Permintaan Terbuka Pembeli (${demands.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }
            }

            if (demands.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Tiada permintaan terbuka dari pembeli setakat ini.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(demands) { demand ->
                    DemandCard(demand = demand)
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // FAB for Buyer to publish a Demand Request
        if (currentRole == UserRole.BUYER) {
            FloatingActionButton(
                onClick = { showCreateDemandDialog = true },
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("create_demand_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Cipta Permintaan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showCreateDemandDialog) {
        CreateDemandDialog(
            onDismiss = { showCreateDemandDialog = false },
            onConfirm = { type, qty, price, date, loc, notes ->
                onCreateDemand(type, qty, price, date, loc, notes)
                showCreateDemandDialog = false
            }
        )
    }
}

@Composable
fun DemandInsightCard(
    insight: DemandMarketInsight,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                PepperTypeBadge(pepperType = insight.pepperType)
                Text(
                    text = "${insight.coveragePercent}% Liputan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (insight.coveragePercent >= 100) SuccessGreen else HarvestGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (insight.coveragePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (insight.coveragePercent >= 100) SuccessGreen else HarvestGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row: Open Demand vs Listed Supply
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MintSurface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Permintaan Pembeli", fontSize = 11.sp, color = ForestGreenLight)
                    Text(
                        text = "${String.format("%.1f", insight.openDemandKg)} kg",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }
                Column {
                    Text(text = "Bekalan Diiklan", fontSize = 11.sp, color = ForestGreenLight)
                    Text(
                        text = "${String.format("%.1f", insight.listedSupplyKg)} kg",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Jurang Permintaan", fontSize = 11.sp, color = if (insight.unmetDemandKg > 0) AlertRed else ForestGreenLight)
                    Text(
                        text = "${String.format("%.1f", insight.unmetDemandKg)} kg",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (insight.unmetDemandKg > 0) AlertRed else SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Natural BM explanation text
            Text(
                text = "💬 ${insight.summaryBm}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (insight.unmetDemandKg > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onAction,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terbitkan Stok Tersedia untuk Memenuhi Permintaan", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DemandCard(demand: DemandRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PepperTypeBadge(pepperType = demand.pepperType)
                Text(
                    text = "Sasaran: RM${demand.targetPricePerKg}/kg",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${demand.targetQtyKg} kg diperlukan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Oleh: ${demand.buyerName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = ForestGreenLight)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Perlu sebelum: ${demand.requiredByDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = ForestGreenLight)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = demand.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (demand.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Catatan: ${demand.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CreateDemandDialog(
    onDismiss: () -> Unit,
    onConfirm: (PepperType, Double, Double, String, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(PepperType.BLACK) }
    var qtyString by remember { mutableStateOf("200") }
    var priceString by remember { mutableStateOf("28.00") }
    var dateString by remember { mutableStateOf("30 Okt 2026") }
    var location by remember { mutableStateOf("Kuching / Sarikei") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terbitkan Permintaan Belian", fontWeight = FontWeight.Bold) },
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

                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { qtyString = it },
                    label = { Text("Kuantiti Sasaran (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("demand_qty_input")
                )

                OutlinedTextField(
                    value = priceString,
                    onValueChange = { priceString = it },
                    label = { Text("Sasaran Harga Pembelian (RM/kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("demand_price_input")
                )

                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Diperlukan Sebelum Tarikh") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Spesifikasi Kualiti / Keperluan") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = AlertRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyString.toDoubleOrNull() ?: 0.0
                    val price = priceString.toDoubleOrNull() ?: 0.0
                    if (qty <= 0 || price <= 0) {
                        errorMsg = "Sila lengkapkan kuantiti dan harga yang sah."
                    } else {
                        onConfirm(selectedType, qty, price, dateString, location, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("submit_demand_request_button")
            ) {
                Text("Terbitkan Permintaan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
