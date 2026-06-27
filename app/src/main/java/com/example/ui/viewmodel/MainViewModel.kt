package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel : ViewModel() {

    // Auth flows
    val currentUser: StateFlow<User?> = MockRepository.currentUser
    
    // Core data flows
    val events: StateFlow<List<Event>> = MockRepository.events
    val projects: StateFlow<List<Project>> = MockRepository.projects
    val groups: StateFlow<List<Group>> = MockRepository.groups
    val announcements: StateFlow<List<Announcement>> = MockRepository.announcements
    val tasks: StateFlow<List<Task>> = MockRepository.tasks

    // Local screen visual states (Loading, Empty, Error simulation states)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isConnectionError = MutableStateFlow(false)
    val isConnectionError: StateFlow<Boolean> = _isConnectionError.asStateFlow()

    // Firebase Diagnostic States
    private val _diagnosticSteps = MutableStateFlow<List<com.example.data.repository.DiagnosticStep>>(emptyList())
    val diagnosticSteps: StateFlow<List<com.example.data.repository.DiagnosticStep>> = _diagnosticSteps.asStateFlow()

    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

    fun clearDiagnostics() {
        _diagnosticSteps.value = emptyList()
    }

    fun triggerConnectionError(show: Boolean) {
        _isConnectionError.value = show
    }

    fun login(email: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(600) // Simulated network lag
            MockRepository.login(email, role)
            _isLoading.value = false
        }
    }

    fun register(fullName: String, email: String, role: String, school: String, city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(600)
            MockRepository.register(fullName, email, role, school, city)
            _isLoading.value = false
        }
    }

    fun logout() {
        MockRepository.logout()
    }

    fun setDemoUserRole(role: String) {
        MockRepository.setDemoUserRole(role)
    }

    fun updateEventApprovalStatus(eventId: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(200)
            // Call mock repository to update local and sync to Firebase
            val list = MockRepository.events.value.map {
                if (it.id == eventId) {
                    val updated = it.copy(approvalStatus = status)
                    if (com.example.data.repository.FirebaseRepository.isEnabled()) {
                        com.example.data.repository.FirebaseRepository.addEvent(updated)
                    }
                    updated
                } else {
                    it
                }
            }
            // Update Mock repository state
            val mockEventsField = MockRepository::class.java.getDeclaredField("_events")
            mockEventsField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val flow = mockEventsField.get(MockRepository) as MutableStateFlow<List<Event>>
            flow.value = list
            _isLoading.value = false
        }
    }

    fun updateProjectApprovalStatus(projectId: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(200)
            val list = MockRepository.projects.value.map {
                if (it.id == projectId) {
                    val updated = it.copy(approvalStatus = status)
                    if (com.example.data.repository.FirebaseRepository.isEnabled()) {
                        com.example.data.repository.FirebaseRepository.addProject(updated)
                    }
                    updated
                } else {
                    it
                }
            }
            val mockProjectsField = MockRepository::class.java.getDeclaredField("_projects")
            mockProjectsField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val flow = mockProjectsField.get(MockRepository) as MutableStateFlow<List<Project>>
            flow.value = list
            _isLoading.value = false
        }
    }

    fun addEvent(
        name: String,
        scope: String,
        status: String,
        theme: String,
        date: String,
        summary: String,
        details: String,
        url: String,
        quota: Int? = null,
        isLocalOnly: Boolean = false,
        imageFile: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(500)
            val newEvent = Event(
                id = UUID.randomUUID().toString(),
                name = name.ifEmpty { "Yeni Etkinlik" },
                scope = scope.ifEmpty { "İl" },
                status = status.ifEmpty { "Ön Duyuru" },
                theme = theme.ifEmpty { "Yazılım & Teknoloji" },
                date = date.ifEmpty { "15 Ekim 2024" },
                summary = summary,
                details = details,
                url = url,
                quota = quota,
                isLocalOnly = isLocalOnly,
                approvalStatus = "Beklemede", // Always "Beklemede" initially for newly submitted events
                city = currentUser.value?.city ?: "İstanbul",
                imageFile = imageFile
            )
            MockRepository.addEvent(newEvent)
            _isLoading.value = false
        }
    }

    fun addProject(
        name: String,
        eventName: String,
        theme: String,
        teamName: String,
        category: String,
        description: String,
        githubUrl: String,
        demoUrl: String,
        ethicsCheck: Boolean,
        screenShotFile: String? = null,
        documentFile: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(500)
            val newProject = Project(
                id = UUID.randomUUID().toString(),
                name = name.ifEmpty { "Yeni Proje" },
                eventName = eventName.ifEmpty { "Ankara Teknoloji Zirvesi 2024" },
                theme = theme.ifEmpty { "Yapay Zeka" },
                teamName = teamName.ifEmpty { "Takım" },
                category = category.ifEmpty { "Genel" },
                cities = listOf("İstanbul"),
                githubUrl = githubUrl,
                demoUrl = demoUrl,
                description = description,
                ethicsCheck = ethicsCheck,
                screenShotFile = screenShotFile,
                documentFile = documentFile,
                approvalStatus = "Beklemede"
            )
            MockRepository.addProject(newProject)
            _isLoading.value = false
        }
    }

    fun addNewTask(
        groupId: String,
        title: String,
        description: String,
        priority: String,
        category: String,
        assigneeName: String,
        dueDate: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(300)
            val newTask = Task(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                title = title.ifEmpty { "Yeni Görev" },
                description = description,
                priority = priority,
                category = category,
                assigneeName = assigneeName.ifEmpty { "Atanmamış" },
                dueDate = dueDate.ifEmpty { "12 Kas" },
                status = "Yapılacak"
            )
            MockRepository.addTask(newTask)
            _isLoading.value = false
        }
    }

    fun addAnnouncement(
        groupId: String,
        title: String,
        content: String,
        authorName: String,
        authorRole: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(300)
            val newAnnouncement = Announcement(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                title = title,
                content = content,
                authorName = authorName,
                authorRole = authorRole,
                timeAgo = "Az Önce"
            )
            MockRepository.addAnnouncement(newAnnouncement)
            _isLoading.value = false
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: String) {
        MockRepository.updateTaskStatus(taskId, newStatus)
    }

    fun createGroup(name: String, description: String, iconName: String) {
        viewModelScope.launch {
            val newGroup = Group(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                memberCount = 1,
                iconName = iconName.ifEmpty { "robot_2" },
                backgroundType = if (iconName == "robot_2") "red" else "blue"
            )
            MockRepository.addGroup(newGroup)
        }
    }

    fun joinGroupWithCode(code: String): Boolean {
        return if (code.trim().isNotEmpty()) {
            // Join success mock
            true
        } else {
            false
        }
    }

    fun runFirebaseDiagnostics(context: android.content.Context) {
        viewModelScope.launch {
            _isDiagnosing.value = true
            _diagnosticSteps.value = listOf(
                com.example.data.repository.DiagnosticStep(
                    name = "Tanılama Başlatılıyor",
                    status = "Çalışıyor...",
                    details = "Firebase bağlantı ve servis testleri hazırlanıyor..."
                )
            )
            val results = com.example.data.repository.FirebaseRepository.runDiagnostics(context)
            _diagnosticSteps.value = results
            _isDiagnosing.value = false
        }
    }
}
