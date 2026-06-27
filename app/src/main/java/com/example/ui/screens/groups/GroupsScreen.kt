package com.example.ui.screens.groups

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Group
import com.example.data.model.Task
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GroupsScreen(
    viewModel: MainViewModel
) {
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    var joinCodeInput by remember { mutableStateOf("") }
    
    // Dialog triggers
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }

    // Dialog form states
    var newGroupName by remember { mutableStateOf("") }
    var newGroupDesc by remember { mutableStateOf("") }
    var newGroupIcon by remember { mutableStateOf("robot_2") }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("Normal") }
    var newTaskCategory by remember { mutableStateOf("Yazılım") }
    var newTaskAssignee by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf("") }

    var newAnnTitle by remember { mutableStateOf("") }
    var newAnnContent by remember { mutableStateOf("") }

    val groups by viewModel.groups.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedGroup == null) {
            // ==========================================
            // GROUP DIRECTORY LIST
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Text(
                    text = "Gruplar",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Join & Create Workspace Section
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
                        // Invite Code Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = joinCodeInput,
                                onValueChange = { joinCodeInput = it },
                                placeholder = { Text("Davet Kodu ile Katıl") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (viewModel.joinGroupWithCode(joinCodeInput)) {
                                        joinCodeInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text("Katıl", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Create Workspace Button
                        Button(
                            onClick = { showCreateGroupDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Yeni Grup Oluştur", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Groups list
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Kayıtlı Olduğun Gruplar",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    groups.forEach { group ->
                        GroupCardRow(group = group, onClick = { selectedGroup = group })
                    }
                }
            }
        } else {
            // ==========================================
            // GROUP DETAILS (ANNOUNCEMENTS & TASK BOARD)
            // ==========================================
            val activeGroup = selectedGroup!!
            var selectedDetailsTab by remember { mutableIntStateOf(0) } // 0: Duyurular, 1: Görev Panosu
            
            val announcements by viewModel.announcements.collectAsState()
            val tasks by viewModel.tasks.collectAsState()

            val activeAnnouncements = announcements.filter { it.groupId == activeGroup.id }
            val activeTasks = tasks.filter { it.groupId == activeGroup.id }

            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Group TopBar
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { selectedGroup = null }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeGroup.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Daha Fazla")
                            }
                        }

                        // Tab selectors
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                        ) {
                            listOf("Duyurular", "Görev Panosu").forEachIndexed { idx, label ->
                                val isSelected = selectedDetailsTab == idx
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDetailsTab = idx }
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.5f)
                                            .height(2.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(1.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab Contents
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (selectedDetailsTab == 0) {
                        // duyurular view
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (activeAnnouncements.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Henüz duyuru yayınlanmamış.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                items(activeAnnouncements) { ann ->
                                    AnnouncementCard(ann = ann)
                                }
                            }
                        }

                        // FAB to add Announcement
                        FloatingActionButton(
                            onClick = { showAddAnnouncementDialog = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = "Duyuru Ekle")
                        }

                    } else {
                        // KANBAN TASK BOARD
                        val todoTasks = activeTasks.filter { it.status == "Yapılacak" }
                        val inProgressTasks = activeTasks.filter { it.status == "Yapılıyor" }
                        val doneTasks = activeTasks.filter { it.status == "Tamamlandı" }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Column counts summary row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TaskColHeader(title = "Yapılacak", count = todoTasks.size)
                                TaskColHeader(title = "Yapılıyor", count = inProgressTasks.size)
                                TaskColHeader(title = "Tamamlandı", count = doneTasks.size)
                            }

                            // Horizontal scrolling Column lists
                            LazyRow(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    KanbanColumn(
                                        title = "Yapılacak",
                                        tasks = todoTasks,
                                        onShiftStatus = { task, next -> viewModel.updateTaskStatus(task.id, next) }
                                    )
                                }
                                item {
                                    KanbanColumn(
                                        title = "Yapılıyor",
                                        tasks = inProgressTasks,
                                        onShiftStatus = { task, next -> viewModel.updateTaskStatus(task.id, next) }
                                    )
                                }
                                item {
                                    KanbanColumn(
                                        title = "Tamamlandı",
                                        tasks = doneTasks,
                                        onShiftStatus = { task, next -> viewModel.updateTaskStatus(task.id, next) }
                                    )
                                }
                            }
                        }

                        // FAB to add Task
                        FloatingActionButton(
                            onClick = { showAddTaskDialog = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = "Görev Ekle")
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & OVERLAYS
    // ==========================================

    // CREATE GROUP DIALOG
    if (showCreateGroupDialog) {
        Dialog(onDismissRequest = { showCreateGroupDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Yeni Çalışma Alanı / Grup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Grup Adı") },
                        placeholder = { Text("Örn: Robotik Takımı") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newGroupDesc,
                        onValueChange = { newGroupDesc = it },
                        label = { Text("Açıklama") },
                        placeholder = { Text("Takım hedefleri...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newGroupIcon,
                        onValueChange = { newGroupIcon = it },
                        label = { Text("İkon Simgesi (robot_2, neurology)") },
                        placeholder = { Text("robot_2") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateGroupDialog = false }) { Text("İptal") }
                        Button(onClick = {
                            if (newGroupName.isNotEmpty()) {
                                viewModel.createGroup(newGroupName, newGroupDesc, newGroupIcon)
                                newGroupName = ""
                                newGroupDesc = ""
                                showCreateGroupDialog = false
                            }
                        }) { Text("Oluştur") }
                    }
                }
            }
        }
    }

    // ADD ANNOUNCEMENT DIALOG
    if (showAddAnnouncementDialog) {
        Dialog(onDismissRequest = { showAddAnnouncementDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Duyuru Yayınla", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    
                    OutlinedTextField(
                        value = newAnnTitle,
                        onValueChange = { newAnnTitle = it },
                        label = { Text("Başlık") },
                        placeholder = { Text("Duyuru başlığı...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAnnContent,
                        onValueChange = { newAnnContent = it },
                        label = { Text("İçerik") },
                        placeholder = { Text("Duyuru içeriği...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddAnnouncementDialog = false }) { Text("İptal") }
                        Button(onClick = {
                            if (newAnnTitle.isNotEmpty() && newAnnContent.isNotEmpty()) {
                                selectedGroup?.let {
                                    viewModel.addAnnouncement(
                                        groupId = it.id,
                                        title = newAnnTitle,
                                        content = newAnnContent,
                                        authorName = "Zeynep Kaya",
                                        authorRole = "Mentör Öğretmen"
                                    )
                                }
                                newAnnTitle = ""
                                newAnnContent = ""
                                showAddAnnouncementDialog = false
                            }
                        }) { Text("Yayınla") }
                    }
                }
            }
        }
    }

    // ADD TASK DIALOG
    if (showAddTaskDialog) {
        Dialog(onDismissRequest = { showAddTaskDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Görev Ekle", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Görev Adı") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text("Detaylar / Açıklama") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = newTaskPriority,
                        onValueChange = { newTaskPriority = it },
                        label = { Text("Öncelik (Yüksek Öncelik, Normal)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTaskCategory,
                        onValueChange = { newTaskCategory = it },
                        label = { Text("Kategori (Yazılım, Mekanik)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTaskAssignee,
                        onValueChange = { newTaskAssignee = it },
                        label = { Text("Atanan Kişi") },
                        placeholder = { Text("Ahmet Y.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTaskDueDate,
                        onValueChange = { newTaskDueDate = it },
                        label = { Text("Bitiş Tarihi") },
                        placeholder = { Text("12 Kas") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddTaskDialog = false }) { Text("İptal") }
                        Button(onClick = {
                            if (newTaskTitle.isNotEmpty()) {
                                selectedGroup?.let {
                                    viewModel.addNewTask(
                                        groupId = it.id,
                                        title = newTaskTitle,
                                        description = newTaskDesc,
                                        priority = if (newTaskPriority.contains("Yüksek", true)) "Yüksek Öncelik" else "",
                                        category = newTaskCategory,
                                        assigneeName = newTaskAssignee,
                                        dueDate = newTaskDueDate
                                    )
                                }
                                newTaskTitle = ""
                                newTaskDesc = ""
                                showAddTaskDialog = false
                            }
                        }) { Text("Oluştur") }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupCardRow(
    group: Group,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Group custom icon container based on theme type
                val iconBg = if (group.backgroundType == "red") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                val iconTint = if (group.backgroundType == "red") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconBg, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (group.iconName == "neurology") Icons.Default.Memory else Icons.Default.SmartToy,
                        contentDescription = group.name,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, size14(), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${group.memberCount} Üye",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun size14() = Modifier.size(14.dp)

@Composable
fun AnnouncementCard(ann: com.example.data.model.Announcement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ann.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ann.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ann.authorName.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(ann.authorName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(ann.authorRole, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(ann.timeAgo, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TaskColHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                .size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(count.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun KanbanColumn(
    title: String,
    tasks: List<Task>,
    onShiftStatus: (Task, String) -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    KanbanTaskCard(task = task, currentColumn = title, onShift = { next -> onShiftStatus(task, next) })
                }
            }
        }
    }
}

@Composable
fun KanbanTaskCard(
    task: Task,
    currentColumn: String,
    onShift: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(
            width = 1.dp,
            color = if (task.priority.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Priority Tag
            if (task.priority.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(task.priority, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(task.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(task.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(8.dp))

            // Task meta assignees
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(task.assigneeName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(task.dueDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shift status buttons to simulate Kanban board drag flow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (currentColumn != "Yapılacak") {
                    OutlinedButton(
                        onClick = {
                            val prev = if (currentColumn == "Tamamlandı") "Yapılıyor" else "Yapılacak"
                            onShift(prev)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("← Geri", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (currentColumn != "Tamamlandı") {
                    Button(
                        onClick = {
                            val next = if (currentColumn == "Yapılacak") "Yapılıyor" else "Tamamlandı"
                            onShift(next)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("İleri →", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
