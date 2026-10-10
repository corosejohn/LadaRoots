package com.example.ladaroots.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ladaroots.data.model.Order
import com.example.ladaroots.data.model.OrderStatus
import com.example.ladaroots.data.model.UserRole
import com.example.ladaroots.ui.components.OrderStatusBadge
import com.example.ladaroots.ui.components.PepperTypeBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SuccessGreen

@Composable
fun OrdersScreen(
    currentRole: UserRole,
    orders: List<Order>,
    onCompleteOrder: (String) -> Unit,
    onCancelOrder: (String, String) -> Unit,
    onUpdateStage: (String, OrderStatus) -> Unit
) {
    var orderToComplete by remember { mutableStateOf<Order?>(null) }
    var orderToCancel by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Senarai Pesanan Komersial",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreenPrimary
        )
        Text(
            text = "Pesanan yang terikat dengan tawaran sah dan tempahan stok terjamin.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (orders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = ForestGreenLight, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tiada pesanan aktif setakat ini. Pesanan dicipta secara automatik apabila tawaran pembeli diterima.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        isFarmer = currentRole == UserRole.FARMER,
                        onCompleteClick = { orderToComplete = order },
                        onCancelClick = { orderToCancel = order },
                        onAdvanceStage = { nextStage -> onUpdateStage(order.id, nextStage) }
                    )
                }
            }
        }
    }

    orderToComplete?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToComplete = null },
            title = { Text("Selesaikan Pesanan & Serah Lada?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pengesahan penyerahan fizikal lada kepada pembeli:")
                    Text("• Pembeli: ${order.buyerName}", fontWeight = FontWeight.SemiBold)
                    Text("• Kuantiti: ${order.qtyKg} kg ${order.pepperType.displayNameBm}")
                    Text("• Jumlah Bayaran: RM${String.format("%.2f", order.totalAmount)}", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                    Surface(color = MintSurface, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "ℹ️ Sistem akan mengurangkan stok fizikal dan melepaskan tempahan secara kekal.",
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
                        onCompleteOrder(order.id)
                        orderToComplete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("confirm_complete_order_button")
                ) {
                    Text("Sahkan Selesai")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToComplete = null }) { Text("Batal") }
            }
        )
    }

    orderToCancel?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToCancel = null },
            title = { Text("Batalkan Pesanan Ini?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Membatalkan pesanan ${order.id} akan melepaskan ${order.qtyKg} kg ${order.pepperType.displayNameBm} kembali ke dalam baki stok tersedia peladang.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelOrder(order.id, "Dibatalkan atas persetujuan bersama")
                        orderToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    modifier = Modifier.testTag("confirm_cancel_order_button")
                ) {
                    Text("Batalkan Pesanan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToCancel = null }) { Text("Kembali") }
            }
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    isFarmer: Boolean,
    onCompleteClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAdvanceStage: (OrderStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ForestGreenPrimary
                )
                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    PepperTypeBadge(pepperType = order.pepperType)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", order.qtyKg)} kg @ RM${order.pricePerKg}/kg",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RM ${String.format("%.2f", order.totalAmount)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                    Text(
                        text = "Jumlah Perjanjian",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Pembeli: ${order.buyerName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Lokasi: ${order.pickupNotes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progression Actions for Farmer
            if (isFarmer && order.status != OrderStatus.COMPLETED && order.status != OrderStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (order.status) {
                        OrderStatus.CONFIRMED -> {
                            Button(
                                onClick = { onAdvanceStage(OrderStatus.PREPARING) },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("advance_order_preparing")
                            ) {
                                Text("Mula Sediakan Lada", fontSize = 12.sp)
                            }
                        }
                        OrderStatus.PREPARING -> {
                            Button(
                                onClick = { onAdvanceStage(OrderStatus.READY_FOR_PICKUP) },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("advance_order_ready")
                            ) {
                                Text("Sedia Diambil / Dihantar", fontSize = 12.sp)
                            }
                        }
                        OrderStatus.READY_FOR_PICKUP -> {
                            Button(
                                onClick = onCompleteClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("complete_order_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Selesaikan Pesanan", fontSize = 12.sp)
                            }
                        }
                        else -> {}
                    }

                    OutlinedButton(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("cancel_order_button")
                    ) {
                        Text("Batal", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
