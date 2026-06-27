package com.example.ui.screens.explore

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Event
import com.example.ui.viewmodel.MainViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import android.webkit.WebSettings

@Composable
fun ExploreScreen(
    viewModel: MainViewModel,
    onAddRequested: () -> Unit,
    onEventInspect: (Event) -> Unit
) {
    val events by viewModel.events.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val darkTheme = isSystemInDarkTheme()
    
    // Filter selection states
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var selectedTheme by remember { mutableStateOf<String?>(null) }
    var selectedFormat by remember { mutableStateOf<String?>(null) }

    // Map properties
    var mapZoom by remember { mutableFloatStateOf(1.0f) }
    var mapMode by remember { mutableStateOf("leaflet") } // "leaflet", "schematic"

    // Dropdown visibility
    var showCityDropdown by remember { mutableStateOf(false) }
    var showThemeDropdown by remember { mutableStateOf(false) }
    var showFormatDropdown by remember { mutableStateOf(false) }

    val cities = listOf("Tümü", "İstanbul", "Ankara", "İzmir")
    val themes = listOf("Tümü", "Yazılım & Teknoloji", "Bilim & İnovasyon", "Sanat & Tasarım")
    val formats = listOf("Tümü", "Zirve", "Hackathon", "Kamp")

    // Filter events based on search query and selections
    val filteredEvents = events.filter { event ->
        val matchesSearch = event.name.contains(searchQuery, ignoreCase = true) ||
                event.summary.contains(searchQuery, ignoreCase = true)
        
        val matchesCity = selectedCity == null || selectedCity == "Tümü" || event.city.equals(selectedCity, ignoreCase = true)
        val matchesTheme = selectedTheme == null || selectedTheme == "Tümü" || event.theme.equals(selectedTheme, ignoreCase = true)
        val matchesFormat = selectedFormat == null || selectedFormat == "Tümü" || event.name.contains(selectedFormat!!, ignoreCase = true)

        matchesSearch && matchesCity && matchesTheme && matchesFormat
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Search and Filters Sticky Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Search Input field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Etkinlik veya proje ara...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Filter Row with Dropdowns
                    Box {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChipButton(
                                    label = selectedCity ?: "Şehir",
                                    isActive = selectedCity != null && selectedCity != "Tümü",
                                    onClick = { showCityDropdown = true }
                                )
                            }
                            item {
                                FilterChipButton(
                                    label = selectedTheme ?: "Tema",
                                    isActive = selectedTheme != null && selectedTheme != "Tümü",
                                    onClick = { showThemeDropdown = true }
                                )
                            }
                            item {
                                FilterChipButton(
                                    label = selectedFormat ?: "Format",
                                    isActive = selectedFormat != null && selectedFormat != "Tümü",
                                    onClick = { showFormatDropdown = true }
                                )
                            }
                            item {
                                FilterChipButton(
                                    label = "Tümü",
                                    isActive = selectedCity != null || selectedTheme != null || selectedFormat != null,
                                    onClick = {
                                        selectedCity = null
                                        selectedTheme = null
                                        selectedFormat = null
                                    },
                                    leadingIcon = Icons.Default.FilterList
                                )
                            }
                        }

                        // Dropdowns Menu triggers
                        DropdownMenu(
                            expanded = showCityDropdown,
                            onDismissRequest = { showCityDropdown = false }
                        ) {
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        selectedCity = city
                                        showCityDropdown = false
                                    }
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showThemeDropdown,
                            onDismissRequest = { showThemeDropdown = false }
                        ) {
                            themes.forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text(theme) },
                                    onClick = {
                                        selectedTheme = theme
                                        showThemeDropdown = false
                                    }
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFormatDropdown,
                            onDismissRequest = { showFormatDropdown = false }
                        ) {
                            formats.forEach { format ->
                                DropdownMenuItem(
                                    text = { Text(format) },
                                    onClick = {
                                        selectedFormat = format
                                        showFormatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                
                // Map Mode Selector Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Gençtek Türkiye Atlası",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val activeColor = MaterialTheme.colorScheme.primary
                            val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            
                            // Leaflet Option
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (mapMode == "leaflet") activeColor else Color.Transparent)
                                    .clickable { mapMode = "leaflet" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Coğrafi (Leaflet)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mapMode == "leaflet") MaterialTheme.colorScheme.onPrimary else inactiveTextColor
                                )
                            }
                            
                            // Schematic Option
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (mapMode == "schematic") activeColor else Color.Transparent)
                                    .clickable { mapMode = "schematic" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Şematik",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mapMode == "schematic") MaterialTheme.colorScheme.onPrimary else inactiveTextColor
                                )
                            }
                        }
                    }
                }

                // Interactive Map View Container (Leaflet / Schematic Canvas)
                item {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 2.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pulseScale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pulseAlpha"
                    )

                    if (mapMode == "leaflet") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            databaseEnabled = true
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                            allowFileAccess = true
                                            allowContentAccess = true
                                        }
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                val themeName = if (darkTheme) "dark" else "light"
                                                view?.evaluateJavascript("setMapTheme('$themeName')", null)
                                                selectedCity?.let { city ->
                                                    view?.evaluateJavascript("focusCityFromAndroid('$city')", null)
                                                }
                                            }
                                        }
                                        addJavascriptInterface(object {
                                            @JavascriptInterface
                                            fun onCitySelected(cityName: String) {
                                                post {
                                                    selectedCity = if (cityName.equals("Tümü", ignoreCase = true)) null else cityName
                                                }
                                            }
                                        }, "AndroidBridge")
                                        loadUrl("file:///android_asset/turkey_map.html")
                                    }
                                },
                                update = { webView ->
                                    val themeName = if (darkTheme) "dark" else "light"
                                    webView.evaluateJavascript("setMapTheme('$themeName')", null)
                                    selectedCity?.let { city ->
                                        webView.evaluateJavascript("focusCityFromAndroid('$city')", null)
                                    }
                                }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            // Drawing custom modern minimalist outline map of Turkey on Canvas
                            val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            val mapLandColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            val pinColor = MaterialTheme.colorScheme.primary
                            val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
                            
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(mapZoom) {
                                        detectTapGestures { offset ->
                                            val mapWidth = size.width.toFloat()
                                            val mapHeight = size.height.toFloat()

                                            val scaledWidth = 320.dp.toPx() * mapZoom
                                            val scaledHeight = 130.dp.toPx() * mapZoom
                                            val startX = (mapWidth - scaledWidth) / 2
                                            val startY = (mapHeight - scaledHeight) / 2

                                            val locationInfo = listOf(
                                                Triple("İstanbul", 0.25f, 0.3f),
                                                Triple("Ankara", 0.48f, 0.45f),
                                                Triple("İzmir", 0.18f, 0.6f)
                                            )

                                            var clickedCity: String? = null
                                            var minDistance = Float.MAX_VALUE
                                            val clickRadiusThreshold = 24.dp.toPx() // Click area threshold around the pin

                                            locationInfo.forEach { (cityName, pctX, pctY) ->
                                                val pinX = startX + (scaledWidth * pctX)
                                                val pinY = startY + (scaledHeight * pctY)
                                                
                                                val dx = offset.x - pinX
                                                val dy = offset.y - pinY
                                                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                                                
                                                if (distance < clickRadiusThreshold && distance < minDistance) {
                                                    minDistance = distance
                                                    clickedCity = cityName
                                                }
                                            }

                                            if (clickedCity != null) {
                                                selectedCity = if (selectedCity == clickedCity) null else clickedCity
                                            }
                                        }
                                    }
                            ) {
                                val mapWidth = size.width
                                val mapHeight = size.height
                                
                                // Draw grid lines for high-tech digital aesthetic
                                val lineSpacing = 40f
                                var x = 0f
                                while (x < mapWidth) {
                                    drawLine(gridLineColor, Offset(x, 0f), Offset(x, mapHeight), strokeWidth = 1f)
                                    x += lineSpacing
                                }
                                var y = 0f
                                while (y < mapHeight) {
                                    drawLine(gridLineColor, Offset(0f, y), Offset(mapWidth, y), strokeWidth = 1f)
                                    y += lineSpacing
                                }

                                // Minimalist schematic Turkey bounds (drawn as a beautiful sleek segmented rectangle)
                                val scaledWidth = 320.dp.toPx() * mapZoom
                                val scaledHeight = 130.dp.toPx() * mapZoom
                                val startX = (mapWidth - scaledWidth) / 2
                                val startY = (mapHeight - scaledHeight) / 2

                                drawRoundRect(
                                    color = mapLandColor,
                                    topLeft = Offset(startX, startY),
                                    size = Size(scaledWidth, scaledHeight),
                                    cornerRadius = CornerRadius(24f, 24f)
                                )
                                drawRoundRect(
                                    color = gridLineColor.copy(alpha = 0.8f),
                                    topLeft = Offset(startX, startY),
                                    size = Size(scaledWidth, scaledHeight),
                                    cornerRadius = CornerRadius(24f, 24f),
                                    style = Stroke(width = 2f)
                                )

                                // Render pindrops representing event density (İstanbul, Ankara, İzmir coordinates)
                                val locations = listOf(
                                    Triple("İstanbul", 0.25f, 0.3f),
                                    Triple("Ankara", 0.48f, 0.45f),
                                    Triple("İzmir", 0.18f, 0.6f)
                                )

                                locations.forEach { (cityName, pctX, pctY) ->
                                    val pinX = startX + (scaledWidth * pctX)
                                    val pinY = startY + (scaledHeight * pctY)
                                    val isSelected = selectedCity == cityName
                                    
                                    if (isSelected) {
                                        // Live pulsating glow
                                        drawCircle(
                                            color = pinColor.copy(alpha = pulseAlpha),
                                            radius = 24f * mapZoom * pulseScale,
                                            center = Offset(pinX, pinY)
                                        )
                                    }
                                    
                                    // Radar ring animation simulation (static outer circles)
                                    drawCircle(
                                        color = if (isSelected) pinColor else pinColor.copy(alpha = 0.15f),
                                        radius = 18f * mapZoom,
                                        center = Offset(pinX, pinY)
                                    )
                                    drawCircle(
                                        color = if (isSelected) pinColor else pinColor.copy(alpha = 0.3f),
                                        radius = 10f * mapZoom,
                                        center = Offset(pinX, pinY)
                                    )
                                    drawCircle(
                                        color = if (isSelected) onPrimaryColor else pinColor,
                                        radius = 5f * mapZoom,
                                        center = Offset(pinX, pinY)
                                    )
                                }
                            }

                            // Map overlays zoom & location buttons
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = { if (mapZoom < 1.6f) mapZoom += 0.15f },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Zoom in", modifier = Modifier.size(20.dp))
                                }
                                FloatingActionButton(
                                    onClick = { if (mapZoom > 0.7f) mapZoom -= 0.15f },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Zoom out", modifier = Modifier.size(20.dp))
                                }
                                FloatingActionButton(
                                    onClick = { mapZoom = 1.0f },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    elevation = FloatingActionButtonDefaults.elevation(3.dp)
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = "My location", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Section Label with Interactive Filter Status and Clear Action
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (selectedCity != null && selectedCity != "Tümü") "$selectedCity Aktiviteleri" else "Yaklaşan Etkinlikler & Projeler",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedCity != null && selectedCity != "Tümü") {
                            TextButton(
                                onClick = { selectedCity = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Temizle", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                // Lists of Events (Filtered)
                if (filteredEvents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = "No events",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Aradığınız kriterlere uygun etkinlik bulunamadı.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    items(filteredEvents) { event ->
                        EventCardItem(event = event, onInspect = { onEventInspect(event) })
                    }
                }

                // Daha Fazla Göster Button
                if (filteredEvents.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = { /* No-op, visual expanded state default */ },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(48.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Daha Fazla Göster",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddRequested,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 72.dp)
                .size(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Hızlı İşlem Ekle", modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun FilterChipButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (leadingIcon == null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun EventCardItem(
    event: Event,
    onInspect: () -> Unit
) {
    val bitmap = remember(event.imageFile) {
        if (!event.imageFile.isNullOrEmpty()) {
            try {
                val decodedBytes = android.util.Base64.decode(event.imageFile, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = event.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                // Header: Title & Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                // Approval Status badge mapping
                val (bgColor, textColor, icon) = when (event.approvalStatus) {
                    "Onaylandı" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle)
                    "Beklemede" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.Pending)
                    else -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Cancel)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = event.approvalStatus,
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.approvalStatus,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Meta items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.city,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.date,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer row: Tag & Inspect Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.theme.substringBefore(" &"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                TextButton(
                    onClick = onInspect,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = if (event.approvalStatus == "İptal Edildi") "Detaylar" else "İncele",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
}
