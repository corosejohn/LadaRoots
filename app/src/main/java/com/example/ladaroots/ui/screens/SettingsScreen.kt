package com.example.ladaroots.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ladaroots.data.model.UserProfile
import com.example.ladaroots.data.model.UserRole
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SoftMint

@Composable
fun SettingsScreen(
    currentRole: UserRole,
    currentUser: UserProfile,
    isOffline: Boolean,
    onSwitchRole: (UserRole) -> Unit,
    onToggleOffline: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tetapan & Profil Akaun",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
            Text(
                text = "Konfigurasi peranan demo, rangkaian dan sistem LadaRoots.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Active Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currentUser.avatarEmoji, fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = currentUser.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Peranan: ${currentRole.labelBm}", fontSize = 12.sp, color = ForestGreenPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = "Lokasi: ${currentUser.location}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Telefon: ${currentUser.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val nextRole = if (currentRole == UserRole.FARMER) UserRole.BUYER else UserRole.FARMER
                            onSwitchRole(nextRole)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("switch_role_button")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tukar ke Mod: ${if (currentRole == UserRole.FARMER) "Pembeli (Buyer)" else "Peladang (Farmer)"}",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Connectivity Simulator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Simulasi Luar Talian (Offline)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = if (isOffline) "Mod luar talian aktif - rekod disimpan setempat" else "Mod dalam talian aktif",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isOffline,
                        onCheckedChange = { onToggleOffline() },
                        colors = SwitchDefaults.colors(checkedThumbColor = ForestGreenPrimary),
                        modifier = Modifier.testTag("offline_mode_switch")
                    )
                }
            }
        }

        // System Constants Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Maklumat Sistem LadaRoots", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreenPrimary)
                    Text(text = "• Bahasa Utama: Bahasa Melayu (Malaysia)", fontSize = 12.sp)
                    Text(text = "• Mata Wang: Malaysian Ringgit (RM)", fontSize = 12.sp)
                    Text(text = "• Unit Kuantiti: kg & tan metrik (1 tan = 1,000 kg)", fontSize = 12.sp)
                    Text(text = "• Zon Masa: Asia/Kuala_Lumpur (GMT+8)", fontSize = 12.sp)
                    Text(text = "• Rujukan Harga: Lembaga Lada Malaysia (MPB)", fontSize = 12.sp)
                    Text(text = "• Kawalan Transaksi: Tempahan Atomik (Room ACID)", fontSize = 12.sp)
                }
            }
        }
    }
}
