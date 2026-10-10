package com.example.ladaroots.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ladaroots.ai.AiActionProposal
import com.example.ladaroots.ai.ChatMessage
import com.example.ladaroots.ai.VoiceManager
import com.example.ladaroots.data.model.PepperStockSummary
import com.example.ladaroots.ui.viewmodel.ScreenDestination
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SoftMint

@Composable
fun HomeAiScreen(
    messages: List<ChatMessage>,
    stockBlack: PepperStockSummary,
    stockWhite: PepperStockSummary,
    onSendMessage: (String) -> Unit,
    onExecuteProposal: (AiActionProposal) -> Unit,
    onNavigate: (ScreenDestination) -> Unit
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceManager(context) }
    var inputText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var showVoiceExamples by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    voiceManager.onSpeechResult = { text ->
        isRecording = false
        onSendMessage(text)
    }
    voiceManager.onSpeechStateChanged = { recording ->
        isRecording = recording
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Farm Greeting Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = "Ladang Lada Sarawak",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = "LadaRoots Pembantu Pintar",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pengurusan tuaian, stok & pasaran lada Sarawak.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Compact Stock Glance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MintSurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Stok Tersedia Anda",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreenLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hitam: ${String.format("%.1f", stockBlack.availableStockKg)} kg",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                                Text(
                                    text = " • ",
                                    color = ForestGreenLight
                                )
                                Text(
                                    text = "Putih: ${String.format("%.1f", stockWhite.availableStockKg)} kg",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigate(ScreenDestination.INVENTORY) },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreenPrimary)
                        ) {
                            Text(text = "Inventori", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Quick Action Chips Row
            item {
                Column {
                    Text(
                        text = "Tindakan Pantas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        item {
                            QuickActionChip(label = "🌱 Rekod Harvest") {
                                onSendMessage("Saya nak rekod hasil tuaian lada baru.")
                            }
                        }
                        item {
                            QuickActionChip(label = "💰 Jual Lada") {
                                onSendMessage("Saya nak jual lada hitam.")
                            }
                        }
                        item {
                            QuickActionChip(label = "📦 Semak Stok") {
                                onSendMessage("Berapa baki stok lada saya sekarang?")
                            }
                        }
                        item {
                            QuickActionChip(label = "📈 Semak Permintaan") {
                                onSendMessage("Ada pembeli mencari stok lada sekarang?")
                            }
                        }
                        item {
                            QuickActionChip(label = "🤝 Lihat Tawaran") {
                                onNavigate(ScreenDestination.OFFERS)
                            }
                        }
                    }
                }
            }

            // Conversation Messages
            items(messages) { msg ->
                MessageItem(
                    message = msg,
                    onSpeak = { voiceManager.speak(msg.text) },
                    onExecuteProposal = onExecuteProposal,
                    onNavigate = onNavigate
                )
            }
        }

        // Voice Simulation / Quick Voice phrases row toggle
        AnimatedVisibility(visible = showVoiceExamples) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contoh Suara (Ujian Pantas BM):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        IconButton(
                            onClick = { showVoiceExamples = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            SampleVoiceChip("Saya baru dapat 120 kilo lada hitam") {
                                onSendMessage("Saya baru dapat 120 kilo lada hitam")
                                showVoiceExamples = false
                            }
                        }
                        item {
                            SampleVoiceChip("Saya ada 0.5 tan lada putih") {
                                onSendMessage("Saya ada 0.5 tan lada putih")
                                showVoiceExamples = false
                            }
                        }
                        item {
                            SampleVoiceChip("Saya nak jual 80 kg lada hitam harga 28 ringgit") {
                                onSendMessage("Saya nak jual 80 kg lada hitam harga 28 ringgit sekilo")
                                showVoiceExamples = false
                            }
                        }
                    }
                }
            }
        }

        // Input Bar Area with Mic, Text Input, and Send
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Microphone Interaction Button with pulsing indicator
                val transition = rememberInfiniteTransition(label = "mic_pulse")
                val scale by transition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = if (isRecording) 1.25f else 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "mic_scale"
                )

                IconButton(
                    onClick = {
                        if (isRecording) {
                            voiceManager.stopListening()
                            isRecording = false
                        } else {
                            voiceManager.startListening()
                            showVoiceExamples = true
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) AlertRed else ForestGreenPrimary)
                        .scale(scale)
                        .testTag("voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Berhenti Rakam" else "Rakam Suara",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ketik atau sebut mesej...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) ForestGreenPrimary else Color.Gray.copy(alpha = 0.3f))
                        .testTag("chat_send_button"),
                    enabled = inputText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Hantar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(label: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenLight.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ForestGreenPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SampleVoiceChip(label: String, onClick: () -> Unit) {
    Surface(
        color = SoftMint,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = "🎙️ \"$label\"",
            fontSize = 11.sp,
            color = ForestGreenPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    onSpeak: () -> Unit,
    onExecuteProposal: (AiActionProposal) -> Unit,
    onNavigate: (ScreenDestination) -> Unit
) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ForestGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌿", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) ForestGreenPrimary else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    // Text-to-Speech audio button for assistant responses
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Dengar Mesej",
                                    tint = ForestGreenLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Structured Action Proposal Review Card
            if (message.proposal !is AiActionProposal.None) {
                Spacer(modifier = Modifier.height(8.dp))
                ActionProposalCard(
                    proposal = message.proposal,
                    onExecuteProposal = onExecuteProposal,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
fun ActionProposalCard(
    proposal: AiActionProposal,
    onExecuteProposal: (AiActionProposal) -> Unit,
    onNavigate: (ScreenDestination) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SoftMint),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenLight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            when (proposal) {
                is AiActionProposal.RecordHarvestProposal -> {
                    Text(
                        text = "📝 Sahkan Rekod Tuaian",
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Jenis: ${proposal.pepperType.displayNameBm}", fontSize = 12.sp)
                    Text(text = "• Kuantiti Input: ${proposal.quantity} ${proposal.unit}", fontSize = 12.sp)
                    Text(text = "• Kuantiti Bersih: ${proposal.normalizedKg} kg", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForestGreenPrimary)
                    if (proposal.notes.isNotBlank()) {
                        Text(text = "• Nota: ${proposal.notes}", fontSize = 11.sp, color = Color.DarkGray)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onExecuteProposal(proposal) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("confirm_harvest_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Ya, Simpan", fontSize = 12.sp)
                        }
                    }
                }

                is AiActionProposal.CreateListingProposal -> {
                    Text(
                        text = "📢 Sahkan Iklan Jualan",
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Jenis: ${proposal.pepperType.displayNameBm}", fontSize = 12.sp)
                    Text(text = "• Kuantiti: ${proposal.quantityKg} kg", fontSize = 12.sp)
                    Text(text = "• Harga Ditawar: RM${proposal.askingPricePerKg}/kg", fontSize = 12.sp)
                    Text(text = "• Anggaran Nilai: RM${String.format("%.2f", proposal.estimatedTotal)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForestGreenPrimary)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onExecuteProposal(proposal) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("confirm_listing_button")
                        ) {
                            Text(text = "Ya, Terbitkan", fontSize = 12.sp)
                        }
                    }
                }

                is AiActionProposal.NavigateToInventory -> {
                    Button(
                        onClick = { onNavigate(ScreenDestination.INVENTORY) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Buka Skrin Inventori", fontSize = 12.sp)
                    }
                }

                is AiActionProposal.NavigateToOffers -> {
                    Button(
                        onClick = { onNavigate(ScreenDestination.OFFERS) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Buka Skrin Tawaran", fontSize = 12.sp)
                    }
                }

                is AiActionProposal.NavigateToDemand -> {
                    Button(
                        onClick = { onNavigate(ScreenDestination.DEMAND_MARKET) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Buka Pasaran Permintaan", fontSize = 12.sp)
                    }
                }

                AiActionProposal.None -> {}
            }
        }
    }
}
