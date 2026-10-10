package com.example.ladaroots.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ladaroots.data.model.HarvestRecord
import com.example.ladaroots.data.model.InventoryMovement
import com.example.ladaroots.data.model.MovementType
import com.example.ladaroots.data.model.PepperStockSummary
import com.example.ladaroots.data.model.PepperType
import com.example.ladaroots.ui.components.PepperStockCard
import com.example.ladaroots.ui.components.PepperTypeBadge
import com.example.ladaroots.ui.viewmodel.ScreenDestination
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SuccessGreen

@Composable
fun InventoryScreen(
    stockBlack: PepperStockSummary,
    stockWhite: PepperStockSummary,
    harvests: List<HarvestRecord>,
    movements: List<InventoryMovement>,
    onAddHarvest: (PepperType, Double, String, String) -> Unit,
    onNavigate: (ScreenDestination) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddHarvestDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info & Formula explanation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pengurusan Inventori",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = "Stok Tersedia = Fizikal − Ditempah (Tempahan Sah)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onNavigate(ScreenDestination.ORDERS) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenLight),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Pesanan", fontSize = 12.sp)
                    }
                }
            }

            // Pepper Stock Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PepperStockCard(summary = stockBlack)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PepperStockCard(summary = stockWhite)
                    }
                }
            }

            // Tabs: Sejarah Tuaian vs Pergerakan Lejar
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = ForestGreenPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ForestGreenPrimary
                        )
                    },
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Rekod Tuaian (${harvests.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Lejar Pergerakan (${movements.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            if (selectedTab == 0) {
                if (harvests.isEmpty()) {
                    item {
                        EmptyInventoryState(
                            message = "Belum ada rekod tuaian disimpan. Ketik butang + di bawah untuk mula merekodkan hasil lada anda."
                        )
                    }
                } else {
                    items(harvests) { harvest ->
                        HarvestItemCard(harvest = harvest)
                    }
                }
            } else {
                if (movements.isEmpty()) {
                    item {
                        EmptyInventoryState(message = "Tiada rekod transaksi pergerakan stok setakat ini.")
                    }
                } else {
                    items(movements) { mov ->
                        MovementItemCard(movement = mov)
                    }
                }
            }

            // Extra padding at bottom for FAB
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddHarvestDialog = true },
            containerColor = ForestGreenPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_harvest_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tuaian")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Rekod Tuaian", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    if (showAddHarvestDialog) {
        AddHarvestDialog(
            onDismiss = { showAddHarvestDialog = false },
            onConfirm = { type, qty, unit, notes ->
                onAddHarvest(type, qty, unit, notes)
                showAddHarvestDialog = false
            }
        )
    }
}

@Composable
fun HarvestItemCard(harvest: HarvestRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_pepper_types),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PepperTypeBadge(pepperType = harvest.pepperType)
                    Text(
                        text = harvest.harvestDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format("%.1f", harvest.qtyKg)} kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                    if (harvest.originalUnit == "tan") {
                        Text(
                            text = " (${harvest.originalQty} tan)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (harvest.notes.isNotBlank()) {
                    Text(
                        text = harvest.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun MovementItemCard(movement: InventoryMovement) {
    val isInflow = movement.movementType == MovementType.HARVEST_IN || movement.movementType == MovementType.RESERVATION_RELEASE
    val col = if (isInflow) SuccessGreen else AlertRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(col.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isInflow) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = col,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = movement.movementType.labelBm,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${if (isInflow) "+" else "-"}${String.format("%.1f", movement.qtyKg)} kg",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = col
                    )
                }
                Text(
                    text = "${movement.pepperType.displayNameBm} • ${movement.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddHarvestDialog(
    onDismiss: () -> Unit,
    onConfirm: (PepperType, Double, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(PepperType.BLACK) }
    var qtyString by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("kg") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val qty = qtyString.toDoubleOrNull() ?: 0.0
    val normalizedKg = if (selectedUnit == "tan") qty * 1000.0 else qty

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Rekod Hasil Tuaian Lada", fontWeight = FontWeight.Bold) },
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
                        Text(text = "Lada Hitam", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { selectedType = PepperType.WHITE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == PepperType.WHITE) ForestGreenPrimary else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Lada Putih", fontSize = 12.sp)
                    }
                }

                Text(text = "Kuantiti & Unit:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = qtyString,
                        onValueChange = { qtyString = it },
                        label = { Text("Kuantiti") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("harvest_qty_input")
                    )

                    Row {
                        OutlinedButton(
                            onClick = { selectedUnit = "kg" },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedUnit == "kg") SoftMint else Color.Transparent
                            )
                        ) {
                            Text("kg")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedButton(
                            onClick = { selectedUnit = "tan" },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedUnit == "tan") SoftMint else Color.Transparent
                            )
                        ) {
                            Text("tan")
                        }
                    }
                }

                if (qty > 0 && selectedUnit == "tan") {
                    Text(
                        text = "⚡ Dinormalkan kepada: ${String.format("%.1f", normalizedKg)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Nota / Lokasi Petak Ladang") },
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
                    if (qty <= 0) {
                        errorMsg = "Sila masukkan kuantiti yang sah."
                    } else {
                        onConfirm(selectedType, qty, selectedUnit, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("submit_harvest_button")
            ) {
                Text("Simpan ke Stok")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun EmptyInventoryState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = ForestGreenLight,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
