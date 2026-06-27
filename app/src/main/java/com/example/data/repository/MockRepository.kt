package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object MockRepository {

    // Current User Session
    private val _currentUser = MutableStateFlow<User?>(
        User(
            fullName = "Kaan Demir",
            email = "kaan@okul.edu.tr",
            role = "Öğrenci",
            school = "Atatürk Fen Lisesi",
            city = "İstanbul"
        )
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Events State
    private val _events = MutableStateFlow<List<Event>>(
        listOf(
            Event(
                id = "e1",
                name = "Ankara Teknoloji Zirvesi 2024",
                scope = "Türkiye Geneli",
                status = "Gerçekleşti",
                theme = "Yazılım & Teknoloji",
                date = "15 Ekim 2024",
                summary = "Bölgesel düzeyde lise öğrencilerinin katılım sağladığı büyük teknoloji zirvesi.",
                details = "Zirve kapsamında yapay zeka panelleri, kodlama atölyeleri ve mentorluk oturumları düzenlenecektir.",
                quota = 200,
                approvalStatus = "Onaylandı",
                city = "Ankara"
            ),
            Event(
                id = "e2",
                name = "Geleceğin Şehirleri Hackathonu",
                scope = "İl",
                status = "Ön Duyuru",
                theme = "Bilim & İnovasyon",
                date = "1-3 Kasım 2024",
                summary = "Sürdürülebilir akıllı şehir projelerinin yarıştığı 48 saatlik hackathon.",
                details = "Akıllı ulaşım, temiz enerji ve atık yönetimi konularında yazılım ve donanım prototipleri geliştirilecektir.",
                quota = 100,
                approvalStatus = "Beklemede",
                city = "İstanbul"
            ),
            Event(
                id = "e3",
                name = "Sürdürülebilir Tarım İnovasyon Kampı",
                scope = "İlçe",
                status = "Gerçekleşti",
                theme = "Sanat & Tasarım",
                date = "20 Kasım 2024",
                summary = "Ekolojik tarım teknikleri ve teknoloji entegrasyonu üzerine eğitim kampı.",
                details = "Akıllı sulama, dikey tarım ve IoT teknolojilerinin tarımda kullanımı uygulamalı olarak anlatılacaktır.",
                quota = 50,
                approvalStatus = "İptal Edildi",
                city = "İzmir"
            )
        )
    )
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // Projects State
    private val _projects = MutableStateFlow<List<Project>>(
        listOf(
            Project(
                id = "p1",
                name = "Otonom Tarım Robotu Projesi",
                eventName = "Ankara Teknoloji Zirvesi 2024",
                theme = "Robotik",
                teamName = "Tarım Gençlik",
                category = "Tarım Teknolojileri",
                cities = listOf("İstanbul", "Ankara"),
                githubUrl = "https://github.com/example/otonom-tarim",
                demoUrl = "https://demo.com/tarim-robotu",
                description = "Güneş enerjisiyle çalışan ve yabani otları otonom tespit eden robot prototipi.",
                ethicsCheck = true,
                approvalStatus = "Onaylandı"
            ),
            Project(
                id = "p2",
                name = "Güneş Enerjili Sulama Sistemi",
                eventName = "Geleceğin Şehirleri Hackathonu",
                theme = "Yapay Zeka",
                teamName = "Güneş Gücü",
                category = "Çevre",
                cities = listOf("İstanbul"),
                githubUrl = "https://github.com/example/solar-irrigation",
                description = "Akıllı nem sensörleri yardımıyla sadece ihtiyaç anında sulama yapan yeşil enerji projesi.",
                ethicsCheck = true,
                approvalStatus = "Beklemede"
            ),
            Project(
                id = "p3",
                name = "Robotik Kol Projesi",
                eventName = "Ankara Teknoloji Zirvesi 2024",
                theme = "Robotik",
                teamName = "Genç Maker",
                category = "Sanayi 4.0",
                cities = listOf("Ankara"),
                githubUrl = "https://github.com/example/robotic-arm",
                description = "Yapay zeka nesne tanımlama özellikli 4 eksenli endüstriyel robotik kol.",
                ethicsCheck = true,
                approvalStatus = "Beklemede"
            )
        )
    )
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    // Groups State
    private val _groups = MutableStateFlow<List<Group>>(
        listOf(
            Group(
                id = "g1",
                name = "Robotik Takımı",
                description = "Yarışma hazırlıkları ve otonom sistem projeleri çalışma alanı.",
                memberCount = 24,
                iconName = "robot_2",
                backgroundType = "red"
            ),
            Group(
                id = "g2",
                name = "Yapay Zeka Çalışma Grubu",
                description = "Makine öğrenmesi modelleri ve veri analizi üzerine tartışmalar.",
                memberCount = 15,
                iconName = "neurology",
                backgroundType = "blue"
            )
        )
    )
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    // Announcements State
    private val _announcements = MutableStateFlow<List<Announcement>>(
        listOf(
            Announcement(
                id = "a1",
                groupId = "g1",
                title = "Bölgesel Yarışma Kayıtları Açıldı!",
                content = "Arkadaşlar merhaba, bu yılki VEX Robotics bölgesel turnuvası için kayıt süreci an itibariyle başlamıştır. Katılacak ekiplerin cuma gününe kadar formları doldurması gerekmektedir.",
                authorName = "Ahmet Yılmaz",
                authorRole = "Takım Kaptanı",
                timeAgo = "2 Saat Önce"
            ),
            Announcement(
                id = "a2",
                groupId = "g1",
                title = "Yeni Sensör Kiti Siparişi",
                content = "Mekanik ekip için talep edilen LiDAR ve ultrasonik sensör setleri sipariş edildi. Tahmini teslimat süresi önümüzdeki hafta içi olacaktır. Prototip çalışmalarını buna göre planlayalım.",
                authorName = "Elif Kaya",
                authorRole = "Mentör Öğretmen",
                timeAgo = "Dün, 14:30"
            ),
            Announcement(
                id = "a3",
                groupId = "g1",
                title = "Atölye Temizliği ve Düzeni",
                content = "Son çalışmalardan sonra atölyede ciddi bir dağınıklık mevcut. Lütfen çalışma bittikten sonra kullanılan aletleri yerlerine kaldıralım. Bu cuma genel temizlik yapılacaktır.",
                authorName = "Caner Bölük",
                authorRole = "Mekanik Sorumlusu",
                timeAgo = "12 Eki"
            )
        )
    )
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Tasks State
    private val _tasks = MutableStateFlow<List<Task>>(
        listOf(
            Task(
                id = "t1",
                groupId = "g1",
                title = "Sensör Testi",
                description = "Ultrasonik sensörlerin mesafe ölçüm doğruluğunu kontrol et.",
                priority = "Yüksek Öncelik",
                category = "Mekanik",
                assigneeName = "Ahmet Y.",
                dueDate = "12 Kas",
                status = "Yapılacak"
            ),
            Task(
                id = "t2",
                groupId = "g1",
                title = "Otonom Sürüş Kodu",
                description = "Engelden kaçma algoritmasını C++ ile yaz ve simülatörde test et.",
                priority = "",
                category = "Yazılım",
                assigneeName = "Zeynep K.",
                dueDate = "10 Kas",
                status = "Yapılacak"
            ),
            Task(
                id = "t3",
                groupId = "g1",
                title = "Malzeme Siparişi",
                description = "Gerekli 3B baskı filamentlerini ve vidaları sipariş et.",
                priority = "",
                category = "Mekanik",
                assigneeName = "Can T.",
                dueDate = "15 Kas",
                status = "Yapılacak"
            ),
            Task(
                id = "t4",
                groupId = "g1",
                title = "Şasi Montajı",
                description = "Alüminyum profilleri birleştirip tekerlekleri tak.",
                priority = "",
                category = "Mekanik",
                assigneeName = "Burak M.",
                dueDate = "11 Kas",
                status = "Yapılıyor"
            ),
            Task(
                id = "t5",
                groupId = "g1",
                title = "Motor Seçimi",
                description = "Tork ve güç gereksinimlerine göre en uygun fırçasız motoru belirle.",
                priority = "Yüksek Öncelik",
                category = "Mekanik",
                assigneeName = "Ahmet Y.",
                dueDate = "28 Eki",
                status = "Tamamlandı"
            )
        )
    )
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // Authentication Functions
    fun login(email: String, role: String): Boolean {
        val fullName = when(role) {
            "Öğretmen" -> "Zeynep Kaya"
            "Okul Müdürü" -> "Mustafa Şahin"
            "Koordinatör" -> "Ahmet Yılmaz"
            else -> "Kaan Demir"
        }
        val school = when(role) {
            "Öğretmen" -> "Kadıköy Anadolu Lisesi"
            "Okul Müdürü" -> "İstanbul Erkek Lisesi"
            "Koordinatör" -> "GençTek Genel Merkezi"
            else -> "Atatürk Fen Lisesi"
        }
        _currentUser.value = User(
            fullName = fullName,
            email = email,
            role = role,
            school = school,
            city = "İstanbul"
        )
        return true
    }

    fun setDemoUserRole(role: String) {
        val email = when(role) {
            "Öğrenci" -> "kaan@okul.edu.tr"
            "Öğretmen" -> "zeynep@okul.edu.tr"
            "Okul Müdürü" -> "mustafa.mudur@okul.edu.tr"
            "Koordinatör" -> "ahmet.koordinator@genctek.org"
            else -> "demo@okul.edu.tr"
        }
        val fullName = when(role) {
            "Öğrenci" -> "Kaan Demir"
            "Öğretmen" -> "Zeynep Kaya"
            "Okul Müdürü" -> "Mustafa Şahin (Müdür)"
            "Koordinatör" -> "Ahmet Yılmaz (Koordinatör)"
            else -> "Ziyaretçi"
        }
        val school = when(role) {
            "Öğrenci" -> "Atatürk Fen Lisesi"
            "Öğretmen" -> "Kadıköy Anadolu Lisesi"
            "Okul Müdürü" -> "İstanbul Erkek Lisesi"
            "Koordinatör" -> "GençTek Genel Merkezi"
            else -> "GençTek Atlas"
        }
        _currentUser.value = User(
            fullName = fullName,
            email = email,
            role = role,
            school = school,
            city = "İstanbul"
        )
    }

    fun register(fullName: String, email: String, role: String, school: String, city: String): Boolean {
        _currentUser.value = User(
            fullName = fullName,
            email = email,
            role = role,
            school = school,
            city = city
        )
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    // Add Functions
    fun addEvent(event: Event) {
        val finalId = event.id.ifEmpty { UUID.randomUUID().toString() }
        val newEvent = event.copy(id = finalId)
        val list = _events.value.toMutableList()
        list.add(0, newEvent)
        _events.value = list
        if (FirebaseRepository.isEnabled()) {
            FirebaseRepository.addEvent(newEvent)
        }
    }

    fun addProject(project: Project) {
        val finalId = project.id.ifEmpty { UUID.randomUUID().toString() }
        val newProject = project.copy(id = finalId)
        val list = _projects.value.toMutableList()
        list.add(0, newProject)
        _projects.value = list
        if (FirebaseRepository.isEnabled()) {
            FirebaseRepository.addProject(newProject)
        }
    }

    fun addGroup(group: Group) {
        val finalId = group.id.ifEmpty { UUID.randomUUID().toString() }
        val newGroup = group.copy(id = finalId)
        val list = _groups.value.toMutableList()
        list.add(newGroup)
        _groups.value = list
        if (FirebaseRepository.isEnabled()) {
            FirebaseRepository.addGroup(newGroup)
        }
    }

    fun addAnnouncement(announcement: Announcement) {
        val finalId = announcement.id.ifEmpty { UUID.randomUUID().toString() }
        val newAnnouncement = announcement.copy(id = finalId)
        val list = _announcements.value.toMutableList()
        list.add(0, newAnnouncement)
        _announcements.value = list
        if (FirebaseRepository.isEnabled()) {
            FirebaseRepository.addAnnouncement(newAnnouncement)
        }
    }

    fun addTask(task: Task) {
        val finalId = task.id.ifEmpty { UUID.randomUUID().toString() }
        val newTask = task.copy(id = finalId)
        val list = _tasks.value.toMutableList()
        list.add(0, newTask)
        _tasks.value = list
        if (FirebaseRepository.isEnabled()) {
            FirebaseRepository.addTask(newTask)
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: String) {
        val list = _tasks.value.map {
            if (it.id == taskId) {
                val updated = it.copy(status = newStatus)
                if (FirebaseRepository.isEnabled()) {
                    FirebaseRepository.addTask(updated) // Firebase updates node
                }
                updated
            } else {
                it
            }
        }
        _tasks.value = list
    }

    // Start Real-Time Firebase Database Listeners
    fun startSyncing() {
        if (!FirebaseRepository.isEnabled()) return

        FirebaseRepository.syncEvents { list ->
            if (list.isNotEmpty()) {
                _events.value = list
            } else {
                // Seed empty Firebase database with starting events
                _events.value.forEach { event ->
                    FirebaseRepository.addEvent(event)
                }
            }
        }

        FirebaseRepository.syncProjects { list ->
            if (list.isNotEmpty()) {
                _projects.value = list
            } else {
                // Seed empty Firebase database with starting projects
                _projects.value.forEach { project ->
                    FirebaseRepository.addProject(project)
                }
            }
        }

        FirebaseRepository.syncGroups { list ->
            if (list.isNotEmpty()) {
                _groups.value = list
            } else {
                // Seed empty Firebase database with starting groups
                _groups.value.forEach { group ->
                    FirebaseRepository.addGroup(group)
                }
            }
        }

        FirebaseRepository.syncAnnouncements { list ->
            if (list.isNotEmpty()) {
                _announcements.value = list
            } else {
                // Seed empty Firebase database with starting announcements
                _announcements.value.forEach { announcement ->
                    FirebaseRepository.addAnnouncement(announcement)
                }
            }
        }

        FirebaseRepository.syncTasks { list ->
            if (list.isNotEmpty()) {
                _tasks.value = list
            } else {
                // Seed empty Firebase database with starting tasks
                _tasks.value.forEach { task ->
                    FirebaseRepository.addTask(task)
                }
            }
        }
    }
}
