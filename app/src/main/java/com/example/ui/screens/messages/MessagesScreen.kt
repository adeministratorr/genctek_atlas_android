package com.example.ui.screens.messages

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AtlasButton
import com.example.ui.components.AtlasEmptyState
import com.example.ui.components.AtlasTextField
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val time: String,
    val isMe: Boolean
)

data class Conversation(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val isOnline: Boolean,
    val initials: String
)

private fun getAvatarColorForName(name: String): Color {
    val hash = name.hashCode()
    val colors = listOf(
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        Color(0xFF673AB7), // Deep Purple
        Color(0xFFE91E63), // Pink
        Color(0xFFFF9800), // Orange
        Color(0xFF03A9F4), // Light Blue
        Color(0xFF4CAF50), // Green
        Color(0xFF9C27B0)  // Purple
    )
    val index = kotlin.math.abs(hash % colors.size)
    return colors[index]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    viewModel: MainViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedConversationId by remember { mutableStateOf<String?>("1") }
    var inputMessageText by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Adaptive Layout Screen Width Check
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600
    
    // Mock conversations
    val conversations = remember {
        listOf(
            Conversation("1", "Elif Koordinatör", "Yarınki bilim şenliği hazırlıkları ne durumda?", "14:32", 2, true, "EK"),
            Conversation("2", "Yapay Zeka Proje Grubu", "Mert: GitHub reposunu güncelledim dostlar.", "Dün", 0, false, "YZ"),
            Conversation("3", "Ahmet Öğretmen", "Proje dosyanızı inceledim, çok başarılı.", "25 Haz", 0, true, "AÖ"),
            Conversation("4", "Sistem Duyuruları", "Atlas platformuna hoş geldiniz!", "20 Haz", 0, false, "SD")
        )
    }
    
    // Mock messages for selected conversation
    val messagesMap = remember {
        mutableStateMapOf(
            "1" to mutableStateListOf(
                ChatMessage("m1", "Elif Koordinatör", "Merhaba, GençTek Atlas projeniz onaylandı!", "10:15", false),
                ChatMessage("m2", "Ben", "Çok teşekkürler Elif Hanım! Ekip olarak çok heyecanlıyız.", "10:18", true),
                ChatMessage("m3", "Elif Koordinatör", "Süper! Yarınki bilim şenliği hazırlıkları ne durumda?", "14:32", false)
            ),
            "2" to mutableStateListOf(
                ChatMessage("m4", "Ayşe", "Selam takım, tasarımı Figma üzerinden paylaştım.", "Dün 16:10", false),
                ChatMessage("m5", "Mert", "Mert: GitHub reposunu güncelledim dostlar.", "Dün 18:22", false)
            ),
            "3" to mutableStateListOf(
                ChatMessage("m6", "Ahmet Öğretmen", "Proje dosyanızı inceledim, çok başarılı.", "25 Haz", false),
                ChatMessage("m7", "Ben", "Geri bildirimleriniz için çok teşekkür ederiz hocam.", "25 Haz", true)
            ),
            "4" to mutableStateListOf(
                ChatMessage("m8", "Sistem", "Atlas platformuna hoş geldiniz! Projelerinizi ekleyip yönetebilirsiniz.", "20 Haz", false)
            )
        )
    }
    
    val currentMessages = if (selectedConversationId != null) {
        messagesMap[selectedConversationId!!] ?: remember { mutableStateListOf() }
    } else {
        remember { mutableStateListOf() }
    }
    val currentConversation = conversations.find { it.id == selectedConversationId }
    
    val filteredConversations = conversations.filter {
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    // Real-time messages sync with Firebase Realtime Database
    LaunchedEffect(selectedConversationId) {
        val convId = selectedConversationId
        if (convId != null && com.example.data.repository.FirebaseRepository.isEnabled()) {
            com.example.data.repository.FirebaseRepository.syncMessages(convId) { firebaseMessages ->
                if (firebaseMessages.isNotEmpty()) {
                    val currentList = messagesMap[convId]
                    if (currentList != null) {
                        currentList.clear()
                        currentList.addAll(firebaseMessages)
                    } else {
                        messagesMap[convId] = androidx.compose.runtime.mutableStateListOf<ChatMessage>().apply {
                            addAll(firebaseMessages)
                        }
                    }
                }
            }
        }
    }

    val onSendMessage: (ChatMessage) -> Unit = { newMsg ->
        currentMessages.add(newMsg)
        if (com.example.data.repository.FirebaseRepository.isEnabled() && selectedConversationId != null) {
            com.example.data.repository.FirebaseRepository.sendMessage(selectedConversationId!!, newMsg)
        }
        coroutineScope.launch {
            if (currentMessages.isNotEmpty()) {
                scrollState.animateScrollToItem(currentMessages.size - 1)
            }
        }
    }
    
    // If screen size goes compact and we have selectedConversationId, we show the chat topbar instead of general list topbar
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Only show main topBar if:
            // 1) It's expanded screen
            // 2) Or it's compact screen but no conversation is active
            if (isExpanded || selectedConversationId == null) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mesajlar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        // Main content body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isExpanded) {
                // Side-by-side Layout (Tablet, Landscape etc.)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Pane: Conversation List
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        ConversationListHeader(searchQuery = searchQuery, onQueryChange = { searchQuery = it })
                        ConversationList(
                            conversations = filteredConversations,
                            selectedId = selectedConversationId,
                            onConversationSelect = { selectedConversationId = it }
                        )
                    }
                    
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    
                    // Right Pane: Active Chat Bubble window
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        if (currentConversation != null) {
                            ChatHeader(
                                currentConversation = currentConversation,
                                isExpanded = true,
                                onBackRequested = { selectedConversationId = null },
                                onActionClicked = { msg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                },
                                showMoreMenu = showMoreMenu,
                                onMoreMenuDismiss = { showMoreMenu = false },
                                onMoreMenuClick = { showMoreMenu = true },
                                currentMessages = currentMessages
                            )
                            
                            ChatMessagesList(
                                currentMessages = currentMessages,
                                currentConversation = currentConversation,
                                scrollState = scrollState,
                                selectedConversationId = selectedConversationId ?: ""
                            )
                            
                            ChatInputBar(
                                value = inputMessageText,
                                onValueChange = { inputMessageText = it },
                                onSend = onSendMessage,
                                onAttachmentRequested = { showAttachmentSheet = true }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                AtlasEmptyState(
                                    title = "Mesaj Seçin",
                                    description = "Sohbet etmek için soldaki konuşmalardan birini seçin.",
                                    icon = Icons.Default.ChatBubbleOutline
                                )
                            }
                        }
                    }
                }
            } else {
                // Mobile Master-Detail navigation
                if (selectedConversationId == null) {
                    // Mobile List View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        ConversationListHeader(searchQuery = searchQuery, onQueryChange = { searchQuery = it })
                        ConversationList(
                            conversations = filteredConversations,
                            selectedId = selectedConversationId,
                            onConversationSelect = { selectedConversationId = it }
                        )
                    }
                } else {
                    // Mobile Chat View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        if (currentConversation != null) {
                            ChatHeader(
                                currentConversation = currentConversation,
                                isExpanded = false,
                                onBackRequested = { selectedConversationId = null },
                                onActionClicked = { msg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                },
                                showMoreMenu = showMoreMenu,
                                onMoreMenuDismiss = { showMoreMenu = false },
                                onMoreMenuClick = { showMoreMenu = true },
                                currentMessages = currentMessages
                            )
                            
                            ChatMessagesList(
                                currentMessages = currentMessages,
                                currentConversation = currentConversation,
                                scrollState = scrollState,
                                selectedConversationId = selectedConversationId ?: ""
                            )
                            
                            ChatInputBar(
                                value = inputMessageText,
                                onValueChange = { inputMessageText = it },
                                onSend = onSendMessage,
                                onAttachmentRequested = { showAttachmentSheet = true }
                            )
                        }
                    }
                }
            }
            
            // Attachment Sheet Modal popup
            if (showAttachmentSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAttachmentSheet = false },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Dosya veya İçerik Gönder",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttachmentOption(
                                title = "Fotoğraf",
                                icon = Icons.Default.Photo,
                                color = Color(0xFF4CAF50),
                                onClick = {
                                    showAttachmentSheet = false
                                    onSendMessage(
                                        ChatMessage(
                                            id = "attach_${System.currentTimeMillis()}",
                                            senderName = "Ben",
                                            text = "📸 [Fotoğraf] Yarınki proje sunum posteri",
                                            time = "Şimdi",
                                            isMe = true
                                        )
                                    )
                                }
                            )
                            
                            AttachmentOption(
                                title = "Belge",
                                icon = Icons.Default.Description,
                                color = Color(0xFF2196F3),
                                onClick = {
                                    showAttachmentSheet = false
                                    onSendMessage(
                                        ChatMessage(
                                            id = "attach_${System.currentTimeMillis()}",
                                            senderName = "Ben",
                                            text = "📄 Proje_Önerisi_Taslak_v2.pdf",
                                            time = "Şimdi",
                                            isMe = true
                                        )
                                    )
                                }
                            )
                            
                            AttachmentOption(
                                title = "Konum",
                                icon = Icons.Default.LocationOn,
                                color = Color(0xFFFF9800),
                                onClick = {
                                    showAttachmentSheet = false
                                    onSendMessage(
                                        ChatMessage(
                                            id = "attach_${System.currentTimeMillis()}",
                                            senderName = "Ben",
                                            text = "📍 Konum paylaşıldı (GençTek Şenlik Alanı)",
                                            time = "Şimdi",
                                            isMe = true
                                        )
                                    )
                                }
                            )
                            
                            AttachmentOption(
                                title = "Kişi",
                                icon = Icons.Default.Person,
                                color = Color(0xFF9C27B0),
                                onClick = {
                                    showAttachmentSheet = false
                                    onSendMessage(
                                        ChatMessage(
                                            id = "attach_${System.currentTimeMillis()}",
                                            senderName = "Ben",
                                            text = "👤 Mert Bilgiç (Yapay Zeka Uzmanı) - İletişim paylaşıldı",
                                            time = "Şimdi",
                                            isMe = true
                                        )
                                    )
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationListHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Mesajlarda ara...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle", modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
fun ConversationList(
    conversations: List<Conversation>,
    selectedId: String?,
    onConversationSelect: (String) -> Unit
) {
    if (conversations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Konuşma bulunamadı",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(conversations) { conv ->
                val isSelected = conv.id == selectedId
                val avatarBgColor = remember(conv.name) { getAvatarColorForName(conv.name) }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .clickable { onConversationSelect(conv.id) }
                        .padding(all = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Initials Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else avatarBgColor
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conv.initials,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else Color.White
                            ),
                            fontSize = 15.sp
                        )
                        
                        if (conv.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = conv.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (conv.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = conv.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = conv.lastMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (conv.unreadCount > 0) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (conv.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (conv.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conv.unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHeader(
    currentConversation: Conversation,
    isExpanded: Boolean,
    onBackRequested: () -> Unit,
    onActionClicked: (String) -> Unit,
    showMoreMenu: Boolean,
    onMoreMenuDismiss: () -> Unit,
    onMoreMenuClick: () -> Unit,
    currentMessages: MutableList<ChatMessage>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .shadow(elevation = 2.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isExpanded) {
            IconButton(
                onClick = onBackRequested,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        val headerAvatarColor = remember(currentConversation.name) { getAvatarColorForName(currentConversation.name) }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(headerAvatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentConversation.initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currentConversation.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (currentConversation.isOnline) Color(0xFF4CAF50) else Color.Gray)
                )
                Text(
                    text = if (currentConversation.isOnline) "Aktif" else "Çevrimdışı",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (currentConversation.isOnline) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    fontSize = 11.sp
                )
            }
        }
        
        IconButton(onClick = {
            onActionClicked("${currentConversation.name} aranıyor...")
        }) {
            Icon(Icons.Default.Phone, contentDescription = "Ara", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = {
            onActionClicked("${currentConversation.name} ile görüntülü arama başlatılıyor...")
        }) {
            Icon(Icons.Default.Videocam, contentDescription = "Görüntülü Ara", tint = MaterialTheme.colorScheme.primary)
        }
        Box {
            IconButton(onClick = onMoreMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Daha Fazla", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = onMoreMenuDismiss
            ) {
                DropdownMenuItem(
                    text = { Text("Sohbeti Temizle") },
                    onClick = {
                        onMoreMenuDismiss()
                        currentMessages.clear()
                        onActionClicked("Sohbet temizlendi.")
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Kişiyi Engelle") },
                    onClick = {
                        onMoreMenuDismiss()
                        onActionClicked("Kişi engellendi (simülasyon).")
                    },
                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Sessize Al") },
                    onClick = {
                        onMoreMenuDismiss()
                        onActionClicked("Sohbet sessize alındı.")
                    },
                    leadingIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
fun ChatMessagesList(
    currentMessages: List<ChatMessage>,
    currentConversation: Conversation,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    selectedConversationId: String
) {
    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(currentMessages) { msg ->
            val alignment = if (msg.isMe) Alignment.End else Alignment.Start
            val containerColor = if (msg.isMe) MaterialTheme.colorScheme.primary
                                 else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (msg.isMe) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalAlignment = alignment
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!msg.isMe) {
                        val senderAvatarColor = remember(msg.senderName) { getAvatarColorForName(msg.senderName) }
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp, bottom = 2.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(senderAvatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentConversation.initials,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (msg.isMe) 16.dp else 4.dp,
                                    bottomEnd = if (msg.isMe) 4.dp else 16.dp
                                )
                            )
                            .background(containerColor)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        Column {
                            if (!msg.isMe && selectedConversationId == "2") {
                                Text(
                                    text = msg.senderName,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = contentColor,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
                
                Text(
                    text = msg.time,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(
                        start = if (msg.isMe) 0.dp else 38.dp,
                        end = if (msg.isMe) 8.dp else 0.dp,
                        top = 4.dp
                    )
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (ChatMessage) -> Unit,
    onAttachmentRequested: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onAttachmentRequested,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Bir mesaj yazın...", fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (value.trim().isNotEmpty()) {
                            onSend(
                                ChatMessage(
                                    id = "m_new_${System.currentTimeMillis()}",
                                    senderName = "Ben",
                                    text = value,
                                    time = "Şimdi",
                                    isMe = true
                                )
                            )
                            onValueChange("")
                        }
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (value.trim().isNotEmpty()) {
                        onSend(
                            ChatMessage(
                                    id = "m_new_${System.currentTimeMillis()}",
                                    senderName = "Ben",
                                    text = value,
                                    time = "Şimdi",
                                    isMe = true
                                )
                        )
                        onValueChange("")
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gönder",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AttachmentOption(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
