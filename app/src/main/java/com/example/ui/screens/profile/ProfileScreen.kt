package com.example.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogoutRequested: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val events by viewModel.events.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val diagnosticSteps by viewModel.diagnosticSteps.collectAsState()
    val isDiagnosing by viewModel.isDiagnosing.collectAsState()
    var selectedDashboardTab by remember { mutableIntStateOf(0) } // 0: Profil, 1: Öğretmen Paneli, 2: Koordinatör Paneli, 3: Genel Analiz

    if (diagnosticSteps.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { 
                if (!isDiagnosing) {
                    viewModel.clearDiagnostics()
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Firebase Tanılama Testi",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Uygulama ile Firebase arasındaki bağlantıyı ve veri okuma/yazma başarısını kontrol eden test adımları:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(diagnosticSteps) { step ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (step.status) {
                                        "Başarılı" -> Color(0xFFE8F5E9)
                                        "Başarısız" -> Color(0xFFFFEBEE)
                                        "Atlandı" -> Color(0xFFF5F5F5)
                                        else -> Color(0xFFFFF3E0)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (step.status == "Çalışıyor...") {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFFFF9800)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = when (step.status) {
                                                "Başarılı" -> Icons.Default.CheckCircle
                                                "Başarısız" -> Icons.Default.Error
                                                "Atlandı" -> Icons.Default.Block
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = step.status,
                                            tint = when (step.status) {
                                                "Başarılı" -> Color(0xFF2E7D32)
                                                "Başarısız" -> Color(0xFFC62828)
                                                "Atlandı" -> Color(0xFF616161)
                                                else -> Color(0xFFEF6C00)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Column {
                                        Text(
                                            text = step.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = when (step.status) {
                                                "Başarılı" -> Color(0xFF1B5E20)
                                                "Başarısız" -> Color(0xFFB71C1C)
                                                "Atlandı" -> Color(0xFF212121)
                                                else -> Color(0xFFE65100)
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = step.details,
                                            fontSize = 11.sp,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isDiagnosing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Testler yapılıyor, lütfen bekleyin...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        val hasFailures = diagnosticSteps.any { step -> step.status == "Başarısız" }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (hasFailures) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (hasFailures) {
                                    "⚠️ Bazı testler başarısız oldu! Lütfen yukarıdaki hata detaylarını kontrol edin ve Firebase kurulum adımlarını tekrarlayın."
                                } else {
                                    "✅ Tebrikler! Tüm testler başarıyla tamamlandı. Firebase veritabanı bağlantınız ve veri okuma/yazma servisleriniz kusursuz çalışıyor!"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasFailures) Color(0xFFC62828) else Color(0xFF2E7D32)
                             )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearDiagnostics() },
                    enabled = !isDiagnosing
                ) {
                    Text("Tamam")
                }
            }
        )
    }

    val user = currentUser ?: com.example.data.model.User(
        fullName = "Kaan Demir",
        email = "kaan@okul.edu.tr",
        role = "Öğrenci",
        school = "Atatürk Fen Lisesi",
        city = "İstanbul"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Dashboard Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.fullName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${user.role} • ${user.school}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Bildirimler", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Sub tabs selector
                ScrollableTabRow(
                    selectedTabIndex = selectedDashboardTab,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    listOf("Profilim", "Öğretmen Paneli", "Koordinatör Paneli", "Genel Analiz").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedDashboardTab == index,
                            onClick = { selectedDashboardTab = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        }

        // Dashboard Content area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedDashboardTab) {
                0 -> {
                    // ==========================================
                    // GENERAL PROFILE OPTIONS
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Hesap Bilgileri", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                
                                ProfileFieldRow(label = "E-posta", value = user.email, icon = Icons.Default.Email)
                                ProfileFieldRow(label = "Okul", value = user.school, icon = Icons.Default.School)
                                ProfileFieldRow(label = "Şehir", value = user.city, icon = Icons.Default.LocationCity)
                                ProfileFieldRow(label = "Rol", value = user.role, icon = Icons.Default.VerifiedUser)
                            }
                        }

                        // ⚡ JÜRI HIZLI DEMO PANELİ
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Jüri Hızlı Demo Paneli",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Text(
                                    text = "Uygulamanın farklı rollere göre davranışını (Öğrenci, Öğretmen, Okul Müdürü, Koordinatör) anında test edin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DemoRoleButton(
                                            roleName = "Öğrenci",
                                            isActive = user.role == "Öğrenci",
                                            icon = Icons.Default.Person,
                                            onClick = {
                                                viewModel.setDemoUserRole("Öğrenci")
                                                selectedDashboardTab = 0
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        DemoRoleButton(
                                            roleName = "Öğretmen",
                                            isActive = user.role == "Öğretmen",
                                            icon = Icons.Default.School,
                                            onClick = {
                                                viewModel.setDemoUserRole("Öğretmen")
                                                selectedDashboardTab = 1
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DemoRoleButton(
                                            roleName = "Okul Müdürü",
                                            isActive = user.role == "Okul Müdürü",
                                            icon = Icons.Default.Business,
                                            onClick = {
                                                viewModel.setDemoUserRole("Okul Müdürü")
                                                selectedDashboardTab = 2
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        DemoRoleButton(
                                            roleName = "Koordinatör",
                                            isActive = user.role == "Koordinatör",
                                            icon = Icons.Default.SupervisorAccount,
                                            onClick = {
                                                viewModel.setDemoUserRole("Koordinatör")
                                                selectedDashboardTab = 3
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // FIREBASE CONNECTOR CARD
                        // ==========================================
                        val context = LocalContext.current
                        val clipboardManager = LocalClipboardManager.current
                        var copyFeedbackVisible by remember { mutableStateOf(false) }
                        
                        val isFirebaseConnected = remember {
                            com.example.data.repository.FirebaseRepository.isEnabled() || try {
                                val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
                                resId != 0 && context.getString(resId).isNotEmpty()
                            } catch (e: Exception) {
                                false
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isFirebaseConnected) Color(0xFF4CAF50).copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Cloud,
                                            contentDescription = null,
                                            tint = if (isFirebaseConnected) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Firebase Entegrasyonu",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    
                                    // Status pill
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isFirebaseConnected) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        color = if (isFirebaseConnected) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                                        shape = CircleShape
                                                    )
                                            )
                                            Text(
                                                text = if (isFirebaseConnected) "Bağlı" else "Demo Modu",
                                                color = if (isFirebaseConnected) Color(0xFF2E7D32) else Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                if (isFirebaseConnected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Firebase projeniz başarıyla bağlandı! google-services.json dosyası aktif.",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Mevcut Firebase projenizi bu uygulamaya bağlamak için aşağıdaki adımları takip edin.",
                                            fontSize = 12.sp,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.runFirebaseDiagnostics(context) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFirebaseConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Analytics,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Bağlantıyı ve Veritabanını Test Et (Tanılayıcı)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Application ID Section
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Uygulama Kimliği (Package Name)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "com.aistudio.genctekatlas.kxmpzq",
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString("com.aistudio.genctekatlas.kxmpzq"))
                                                copyFeedbackVisible = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (copyFeedbackVisible) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = "Kopyala",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    if (copyFeedbackVisible) {
                                        Text(
                                            text = "Uygulama kimliği panoya kopyalandı!",
                                            color = Color(0xFF4CAF50),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                        LaunchedEffect(copyFeedbackVisible) {
                                            kotlinx.coroutines.delay(2000)
                                            copyFeedbackVisible = false
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Step-by-step installation instructions
                                Text(
                                    text = "Nasıl Bağlanır?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FirebaseStepRow(number = "1", text = "Firebase Konsolu'na gidin (console.firebase.google.com) ve projenizi açın.")
                                    FirebaseStepRow(number = "2", text = "Yeni bir Android uygulaması ekleyin. Yukarıda kopyaladığınız kimliği girin.")
                                    FirebaseStepRow(number = "3", text = "İndirdiğiniz 'google-services.json' dosyasını sol menüdeki dosya gezginini kullanarak '/app/' klasörü altına yükleyin.")
                                    FirebaseStepRow(number = "4", text = "Dosyayı yükledikten sonra üst menüdeki 'Compile' butonuna tıklayarak uygulamayı derleyin. Entegrasyon otomatik olarak tamamlanacaktır.")
                                }
                            }
                        }

                        Button(
                            onClick = onLogoutRequested,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Oturumu Kapat", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                1 -> {
                    // ==========================================
                    // ÖĞRETMEN PANELİ (TEACHER DASHBOARD)
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Hoş geldiniz, bugünkü özetiniz aşağıdadır.", color = MaterialTheme.colorScheme.onSurfaceVariant)

                        // KPI bento section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("14", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    Text("Toplam Grup", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("28", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Aktif Görev", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }

                        // Performance summaries bento item
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("+12% bu hafta", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Performans Özeti", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Öğrencilerinizin %85'i görevlerini zamanında tamamladı.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }

                        // Horizontal Student submissions List
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kendi Öğrencilerimin Gönderileri", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                TextButton(onClick = {}) { Text("Tümünü Gör", color = MaterialTheme.colorScheme.primary) }
                            }

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StudentProjectCardMock(title = "Güneş Enerjisi Modeli", subject = "Fizik", authors = "Ayşe Yılmaz & Ekibi", colorAccent = MaterialTheme.colorScheme.primary)
                                StudentProjectCardMock(title = "Akıllı Sulama Sistemi", subject = "Robotik", authors = "Caner Kaya", colorAccent = MaterialTheme.colorScheme.secondary)
                                StudentProjectCardMock(title = "Yerel Flora İncelemesi", subject = "Biyoloji", authors = "Zeynep & Ali", colorAccent = MaterialTheme.colorScheme.tertiary)
                            }
                        }

                        // Live Pending project/event requests
                        val pendingProjects = projects.filter { it.approvalStatus == "Beklemede" }
                        val pendingEvents = events.filter { it.approvalStatus == "Beklemede" }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Onay Bekleyen Başvurular", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            
                            if (pendingProjects.isEmpty() && pendingEvents.isEmpty()) {
                                Text(
                                    text = "Onay bekleyen yeni etkinlik veya proje başvurusu bulunmamaktadır.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                pendingEvents.forEach { ev ->
                                    PendingApprovalItem(
                                        title = ev.name,
                                        subtitle = "${ev.theme} • Etkinlik Başvurusu",
                                        initials = ev.name.take(2).uppercase(),
                                        color = MaterialTheme.colorScheme.primary,
                                        onApprove = { viewModel.updateEventApprovalStatus(ev.id, "Onaylandı") },
                                        onReject = { viewModel.updateEventApprovalStatus(ev.id, "Reddedildi") }
                                    )
                                }
                                pendingProjects.forEach { prj ->
                                    PendingApprovalItem(
                                        title = prj.name,
                                        subtitle = "${prj.teamName} • Proje Başvurusu",
                                        initials = prj.name.take(2).uppercase(),
                                        color = MaterialTheme.colorScheme.secondary,
                                        onApprove = { viewModel.updateProjectApprovalStatus(prj.id, "Onaylandı") },
                                        onReject = { viewModel.updateProjectApprovalStatus(prj.id, "Reddedildi") }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ==========================================
                    // KOORDİNATÖR PANELİ (COORDINATOR DASHBOARD)
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text("Marmara Bölgesi Özeti", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Ekim 2023 dönemi güncel verileri", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }

                        // KPI Grid 2x2
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiSquareCard(title = "Aktif Proje", value = "1,248", icon = Icons.Default.FolderOpen, modifier = Modifier.weight(1f))
                            KpiSquareCard(title = "Öğrenci Katılımı", value = "4,592", icon = Icons.Default.Group, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiSquareCard(title = "Tamamlanan Etkinlik", value = "312", icon = Icons.Default.EventAvailable, modifier = Modifier.weight(1f))
                            KpiSquareCard(title = "Bölge Sıralaması", value = "#2", icon = Icons.Default.EmojiEvents, modifier = Modifier.weight(1f))
                        }

                        // Theme distributions progress bars
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tema Dağılımı", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }

                                ThemeProgressRow(theme = "Yapay Zeka (AI)", pct = 0.45f, valueStr = "45%", color = MaterialTheme.colorScheme.primary)
                                ThemeProgressRow(theme = "Robotik & Kodlama", pct = 0.30f, valueStr = "30%", color = MaterialTheme.colorScheme.secondary)
                                ThemeProgressRow(theme = "Sürdürülebilirlik", pct = 0.15f, valueStr = "15%", color = MaterialTheme.colorScheme.tertiary)
                                ThemeProgressRow(theme = "Diğer", pct = 0.10f, valueStr = "10%", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                3 -> {
                    // ==========================================
                    // GENEL ANALİZ (GENERAL ANALYTICS)
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text("Genel Analiz", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Sistem üzerindeki güncel istatistikler ve metrikler.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }

                        // Pending submissions callout card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PendingActions, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bekleyen Kayıt Sayısı", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                    Text("142", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("İncele", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Cities activities distribution list
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Şehirlere Göre Etkinlikler", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }

                                ThemeProgressRow(theme = "İstanbul", pct = 0.80f, valueStr = "45 Etkinlik", color = MaterialTheme.colorScheme.primary)
                                ThemeProgressRow(theme = "Ankara", pct = 0.50f, valueStr = "28 Etkinlik", color = MaterialTheme.colorScheme.secondaryContainer)
                                ThemeProgressRow(theme = "İzmir", pct = 0.30f, valueStr = "16 Etkinlik", color = MaterialTheme.colorScheme.tertiaryContainer)
                            }
                        }

                        // Leaderboard leaderboard active schools
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("En Aktif Okullar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }

                                LeaderboardRankRow(rankNum = "1", school = "Atatürk Fen Lisesi", location = "İstanbul", points = "120 Puan", rankBg = MaterialTheme.colorScheme.primaryContainer, rankText = MaterialTheme.colorScheme.onPrimaryContainer)
                                LeaderboardRankRow(themeText = "Ankara Anadolu Lisesi", valueStr = "Ankara", points = "95 Puan", indexStr = "2")
                                ThemeLeaderboardItem(schoolName = "İzmir Fen Lisesi", location = "İzmir", points = "82 Puan", rankNum = "3")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileFieldRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun StudentProjectCardMock(
    title: String,
    subject: String,
    authors: String,
    colorAccent: Color
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(colorAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                // Drawing generic schematic blueprint representing mock cover photo
                Icon(Icons.Default.Science, contentDescription = null, tint = colorAccent, modifier = Modifier.size(32.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(colorAccent, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(subject, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Column(modifier = Modifier.padding(10.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(authors, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun PendingApprovalItem(
    title: String,
    subtitle: String,
    initials: String,
    color: Color,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onApprove,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Onayla", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                OutlinedButton(
                    onClick = onReject,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336)),
                    border = BorderStroke(1.dp, Color(0xFFF44336)),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Reddet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DemoRoleButton(
    roleName: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = roleName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun KpiSquareCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ThemeProgressRow(
    theme: String,
    pct: Float,
    valueStr: String,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(theme, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(valueStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = pct,
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun LeaderboardRankRow(
    rankNum: String,
    school: String,
    location: String,
    points: String,
    rankBg: Color,
    rankText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(rankBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(rankNum, color = rankText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(school, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(points, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun LeaderboardRankRow(
    themeText: String,
    valueStr: String,
    points: String,
    indexStr: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(indexStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(themeText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(valueStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(points, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ThemeLeaderboardItem(
    schoolName: String,
    location: String,
    points: String,
    rankNum: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(rankNum, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(schoolName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(points, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FirebaseStepRow(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
