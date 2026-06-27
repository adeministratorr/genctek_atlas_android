package com.example.ui.screens.form

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: MainViewModel,
    initialTab: Int = 0,
    onBackRequested: () -> Unit
) {
    var selectedFormTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0: Etkinlik Ekle, 1: Proje Ekle
    var showSuccessDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Helper to read Uri as Base64 (and compress if it's an image)
    fun compressImageUriToBase64(uri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (originalBitmap == null) return null
            
            // Resize if it's too big
            val maxDimension = 800
            val width = originalBitmap.width
            val height = originalBitmap.height
            val resizedBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val (newWidth, newHeight) = if (ratio > 1) {
                    Pair(maxDimension, (maxDimension / ratio).toInt())
                } else {
                    Pair((maxDimension * ratio).toInt(), maxDimension)
                }
                android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }
            
            val outputStream = java.io.ByteArrayOutputStream()
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            
            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()
            
            base64
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper for any file to Base64
    fun fileUriToBase64(uri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper to get file name
    fun uriToFileName(uri: android.net.Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "dosya"
    }

    var eventImageBase64 by remember { mutableStateOf<String?>(null) }
    var eventImageName by remember { mutableStateOf<String?>(null) }

    var projectScreenshotBase64 by remember { mutableStateOf<String?>(null) }
    var projectScreenshotName by remember { mutableStateOf<String?>(null) }
    var projectDocumentBase64 by remember { mutableStateOf<String?>(null) }
    var projectDocumentName by remember { mutableStateOf<String?>(null) }

    val eventImagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            eventImageBase64 = compressImageUriToBase64(it)
            eventImageName = uriToFileName(it)
        }
    }

    val projectScreenshotPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            projectScreenshotBase64 = compressImageUriToBase64(it)
            projectScreenshotName = uriToFileName(it)
        }
    }

    val projectDocumentPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            projectDocumentBase64 = fileUriToBase64(it)
            projectDocumentName = uriToFileName(it)
        }
    }

    // Etkinlik Form states
    var eventName by remember { mutableStateOf("") }
    var eventScope by remember { mutableStateOf("") }
    var eventStatus by remember { mutableStateOf("") }
    var eventTheme by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventSummary by remember { mutableStateOf("") }
    var eventDetails by remember { mutableStateOf("") }
    var eventUrl by remember { mutableStateOf("") }
    var eventQuota by remember { mutableStateOf("") }
    var eventLocalOnly by remember { mutableStateOf(false) }
    var eventSchoolsList by remember { mutableStateOf(listOf(Pair("Atatürk Fen Lisesi", "12"))) }
    var newSchoolName by remember { mutableStateOf("") }
    var newSchoolParticipants by remember { mutableStateOf("") }

    // Proje Form states
    var projectName by remember { mutableStateOf("") }
    var projectEvent by remember { mutableStateOf("") }
    var projectTheme by remember { mutableStateOf("") }
    var projectTeam by remember { mutableStateOf("") }
    var projectCategory by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    var projectGithub by remember { mutableStateOf("") }
    var projectDemo by remember { mutableStateOf("") }
    var projectEthicsChecked by remember { mutableStateOf(false) }
    var projectCities by remember { mutableStateOf(listOf("İstanbul", "Ankara")) }
    var newCityInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedFormTab == 0) "Etkinlik Ekle" else "Proje Ekle",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackRequested) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            // Tab Selector to switch between forms
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                listOf("Etkinlik Ekle", "Proje Ekle").forEachIndexed { index, title ->
                    val isSelected = selectedFormTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFormTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Scrollable forms
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedFormTab == 0) {
                    // ==========================================
                    // ETKİNLİK EKLE FORM
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Temel Bilgiler",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Lütfen etkinlikle ilgili temel bilgileri doldurun. * işaretli alanlar zorunludur.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Event Name
                            OutlinedTextField(
                                value = eventName,
                                onValueChange = { eventName = it },
                                label = { Text("Etkinlik Adı *") },
                                placeholder = { Text("Örn: Teknoloji Zirvesi 2024") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Scope
                            OutlinedTextField(
                                value = eventScope,
                                onValueChange = { eventScope = it },
                                label = { Text("Kapsam * (İl, İlçe, Okul, Türkiye Geneli)") },
                                placeholder = { Text("Kapsam seçin (Örn: İl)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Status (Ön Duyuru, Gerçekleşti)
                            OutlinedTextField(
                                value = eventStatus,
                                onValueChange = { eventStatus = it },
                                label = { Text("Durum * (Ön Duyuru, Gerçekleşti)") },
                                placeholder = { Text("Ön Duyuru veya Gerçekleşti") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Theme
                            OutlinedTextField(
                                value = eventTheme,
                                onValueChange = { eventTheme = it },
                                label = { Text("Tema *") },
                                placeholder = { Text("Yazılım & Teknoloji, Bilim, Sanat") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Date
                            OutlinedTextField(
                                value = eventDate,
                                onValueChange = { eventDate = it },
                                label = { Text("Tarih *") },
                                placeholder = { Text("YYYY-MM-DD (Örn: 2024-11-20)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Tarih Seç") }
                            )

                            // Summary (Max 500)
                            Column {
                                OutlinedTextField(
                                    value = eventSummary,
                                    onValueChange = { if (it.length <= 500) eventSummary = it },
                                    label = { Text("Özet Açıklama") },
                                    placeholder = { Text("Etkinlik hakkında kısaca bilgi verin...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    maxLines = 3,
                                    minLines = 3
                                )
                                Text(
                                    text = "${eventSummary.length} / 500",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    textAlign = TextAlign.End
                                )
                            }

                            // Details
                            OutlinedTextField(
                                value = eventDetails,
                                onValueChange = { eventDetails = it },
                                label = { Text("Detaylı Açıklama") },
                                placeholder = { Text("Etkinliğin detaylarını girin...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                minLines = 4
                            )

                            // URL
                            OutlinedTextField(
                                value = eventUrl,
                                onValueChange = { eventUrl = it },
                                label = { Text("Bağlantı URL") },
                                placeholder = { Text("https://...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Dynamic Logic Panel
                    if (eventStatus.equals("Ön Duyuru", ignoreCase = true)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Ön Duyuru Ayarları",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                OutlinedTextField(
                                    value = eventQuota,
                                    onValueChange = { eventQuota = it },
                                    label = { Text("Kontenjan Sınırı") },
                                    placeholder = { Text("Kişi sayısı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { eventLocalOnly = !eventLocalOnly }
                                ) {
                                    Checkbox(
                                        checked = eventLocalOnly,
                                        onCheckedChange = { eventLocalOnly = it }
                                    )
                                    Text(
                                        text = "Sadece bu il/ilçe başvursun",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    } else if (eventStatus.equals("Gerçekleşti", ignoreCase = true)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Gerçekleşen Etkinlik Detayları",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                
                                Text(
                                    text = "Katılan Okullar",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )

                                // Add School input row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newSchoolName,
                                        onValueChange = { newSchoolName = it },
                                        label = { Text("Okul Adı") },
                                        placeholder = { Text("Örn: Atatürk FL") },
                                        modifier = Modifier.weight(2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = newSchoolParticipants,
                                        onValueChange = { newSchoolParticipants = it },
                                        label = { Text("Kişi") },
                                        placeholder = { Text("0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (newSchoolName.isNotEmpty() && newSchoolParticipants.isNotEmpty()) {
                                                eventSchoolsList = eventSchoolsList + Pair(newSchoolName, newSchoolParticipants)
                                                newSchoolName = ""
                                                newSchoolParticipants = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add School", tint = Color.White)
                                    }
                                }

                                // Schools list table
                                eventSchoolsList.forEach { (sch, count) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(sch, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text("Katılımcı: $count", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { eventSchoolsList = eventSchoolsList.filterNot { it.first == sch } }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Media & Coordinates Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Konum ve Medya",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            // Coordinate display map mockup
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Map Pin",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Harita Koordinatı (41.0082, 28.9784)",
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp
                                )
                            }

                            // Image Active Uploader with Preview
                            val eventImageBitmap = remember(eventImageBase64) {
                                if (!eventImageBase64.isNullOrEmpty()) {
                                    try {
                                        val decodedBytes = android.util.Base64.decode(eventImageBase64, android.util.Base64.DEFAULT)
                                        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else null
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceBright)
                                    .border(2.dp, if (eventImageBitmap != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { eventImagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (eventImageBitmap != null) {
                                    Image(
                                        bitmap = eventImageBitmap.asImageBitmap(),
                                        contentDescription = "Event Image Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    )
                                    IconButton(
                                        onClick = {
                                            eventImageBase64 = null
                                            eventImageName = null
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White)
                                    }
                                    Column(
                                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                                    ) {
                                        Text(
                                            text = eventImageName ?: "Seçilen Resim",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Değiştirmek için tıklayın",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Add Photo",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Afiş / Görsel Seçin", fontWeight = FontWeight.Bold)
                                        Text("Dosyayı seçmek için tıklayın", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // ==========================================
                    // PROJE EKLE FORM
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Temel Bilgiler",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Project Name
                            OutlinedTextField(
                                value = projectName,
                                onValueChange = { projectName = it },
                                label = { Text("Proje Adı *") },
                                placeholder = { Text("Örn: Akıllı Tarım Robotu") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Associated Event
                            OutlinedTextField(
                                value = projectEvent,
                                onValueChange = { projectEvent = it },
                                label = { Text("Bağlı Olduğu Etkinlik") },
                                placeholder = { Text("Etkinlik adı") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Theme Selector
                                OutlinedTextField(
                                    value = projectTheme,
                                    onValueChange = { projectTheme = it },
                                    label = { Text("Tema") },
                                    placeholder = { Text("Örn: Robotik") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                // Category
                                OutlinedTextField(
                                    value = projectCategory,
                                    onValueChange = { projectCategory = it },
                                    label = { Text("Kategori") },
                                    placeholder = { Text("Örn: Tarım") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Team Name
                            OutlinedTextField(
                                value = projectTeam,
                                onValueChange = { projectTeam = it },
                                label = { Text("Takım Adı") },
                                placeholder = { Text("Takımınızın adı") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Cities Tag Editor Mockup
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Katılımcı İller", style = MaterialTheme.typography.labelLarge)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerLow,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    projectCities.forEach { city ->
                                        AssistChip(
                                            onClick = { projectCities = projectCities.filterNot { it == city } },
                                            label = { Text(city) },
                                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Sil", modifier = Modifier.size(12.dp)) }
                                        )
                                    }
                                    
                                    BasicTextField(
                                        value = newCityInput,
                                        onValueChange = {
                                            if (it.endsWith(" ") || it.endsWith(",")) {
                                                val clean = it.trim().removeSuffix(",")
                                                if (clean.isNotEmpty() && !projectCities.contains(clean)) {
                                                    projectCities = projectCities + clean
                                                }
                                                newCityInput = ""
                                            } else {
                                                newCityInput = it
                                            }
                                        },
                                        modifier = Modifier.width(80.dp),
                                        decorationBox = { innerTextField ->
                                            if (newCityInput.isEmpty() && projectCities.isEmpty()) {
                                                Text("İl ekleyin...", color = Color.Gray, fontSize = 13.sp)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Code Links Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Kod ve Canlı Linkler",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // GitHub url
                            OutlinedTextField(
                                value = projectGithub,
                                onValueChange = { projectGithub = it },
                                label = { Text("GitHub Bağlantısı") },
                                placeholder = { Text("https://github.com/...") },
                                leadingIcon = { Icon(Icons.Default.Code, contentDescription = "GitHub") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Demo url
                            OutlinedTextField(
                                value = projectDemo,
                                onValueChange = { projectDemo = it },
                                label = { Text("Demo Bağlantısı") },
                                placeholder = { Text("https://demo.com/...") },
                                leadingIcon = { Icon(Icons.Default.Language, contentDescription = "Demo") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Project Detailed Description
                            Column {
                                OutlinedTextField(
                                    value = projectDescription,
                                    onValueChange = { if (it.length <= 1000) projectDescription = it },
                                    label = { Text("Proje Açıklaması *") },
                                    placeholder = { Text("Projenizi detaylandırın...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    minLines = 4
                                )
                                Text(
                                    text = "${projectDescription.length} / 1000",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    // Screenshot and Document Active Uploader
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Proje Görseli ve Belgeleri",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Screenshot Uploader
                            Text("Ekran Görüntüsü / Görsel Yükle", style = MaterialTheme.typography.labelLarge)
                            
                            val projectScreenshotBitmap = remember(projectScreenshotBase64) {
                                if (!projectScreenshotBase64.isNullOrEmpty()) {
                                    try {
                                        val decodedBytes = android.util.Base64.decode(projectScreenshotBase64, android.util.Base64.DEFAULT)
                                        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else null
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceBright)
                                    .border(2.dp, if (projectScreenshotBitmap != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { projectScreenshotPicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (projectScreenshotBitmap != null) {
                                    Image(
                                        bitmap = projectScreenshotBitmap.asImageBitmap(),
                                        contentDescription = "Project Screenshot Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    )
                                    IconButton(
                                        onClick = {
                                            projectScreenshotBase64 = null
                                            projectScreenshotName = null
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White)
                                    }
                                    Column(
                                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                                    ) {
                                        Text(
                                            text = projectScreenshotName ?: "Seçilen Görsel",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Add Screenshot",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Uygulama Görseli Seçin", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Tıklayıp galeri veya kameradan seçin", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Prompt Document Uploader
                            Text("Prompt Belgesi (.md/.txt) Yükleme", style = MaterialTheme.typography.labelLarge)
                            
                            if (projectDocumentBase64 != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerLow,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = "Doc", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(projectDocumentName ?: "belge.md", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text("Yüklendi (Base64)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Tamamlandı", tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                projectDocumentBase64 = null
                                                projectDocumentName = null
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceBright, shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                                        .clickable { projectDocumentPicker.launch("*/*") }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.UploadFile, contentDescription = "Upload", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Bir dosya seçmek için tıklayın (.md, .txt, .pdf)", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // AI Ethics critical checkbox Area
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = "Critical", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kritik Alan",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { projectEthicsChecked = !projectEthicsChecked },
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = projectEthicsChecked,
                                    onCheckedChange = { projectEthicsChecked = it }
                                )
                                Column(modifier = Modifier.padding(start = 4.dp)) {
                                    Text(
                                        text = "Etik ve AI Kullanım Kontrol Listesini onaylıyorum.",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Yapay zeka çıktılarının doğruluğunu kontrol ettiğimi, kodları test ettiğimi, promptları arşivlediğimi ve telif haklarına uyduğumu beyan ederim.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Global Bottom Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackRequested,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("İptal Et", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (selectedFormTab == 0) {
                                viewModel.addEvent(
                                    eventName, eventScope, eventStatus, eventTheme,
                                    eventDate, eventSummary, eventDetails, eventUrl,
                                    imageFile = eventImageBase64
                                )
                            } else {
                                viewModel.addProject(
                                    projectName, projectEvent, projectTheme, projectTeam,
                                    projectCategory, projectDescription, projectGithub, projectDemo,
                                    projectEthicsChecked,
                                    screenShotFile = projectScreenshotBase64,
                                    documentFile = projectDocumentBase64
                                )
                            }
                            showSuccessDialog = true
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedFormTab == 0) "Başvuruyu Gönder" else "Başvuruyu Tamamla",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Success dialog modal
    if (showSuccessDialog) {
        Dialog(onDismissRequest = {
            showSuccessDialog = false
            onBackRequested()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Başarılı!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Kaydınız başarıyla alındı ve onay için gönderildi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onBackRequested()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Geri Dön", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
