package com.example.data.model

data class User(
    val fullName: String = "",
    val email: String = "",
    val role: String = "Öğrenci", // "Öğrenci" or "Öğretmen"
    val school: String = "",
    val city: String = ""
)

data class Event(
    val id: String = "",
    val name: String = "",
    val scope: String = "İl", // "İl", "İlçe", "Okul", "Türkiye Geneli"
    val status: String = "Ön Duyuru", // "Ön Duyuru", "Gerçekleşti"
    val theme: String = "Yazılım & Teknoloji", // "Yapay Zeka & Teknoloji", "Bilim & İnovasyon", "Sanat & Tasarım"
    val date: String = "",
    val summary: String = "",
    val details: String = "",
    val url: String = "",
    val quota: Int? = null,
    val isLocalOnly: Boolean = false,
    val approvalStatus: String = "Onaylandı", // "Onaylandı", "Beklemede", "İptal Edildi"
    val city: String = "İstanbul",
    val imageFile: String? = null
)

data class Project(
    val id: String = "",
    val name: String = "",
    val eventName: String = "",
    val theme: String = "", // "Yapay Zeka", "Robotik", etc.
    val teamName: String = "",
    val category: String = "",
    val cities: List<String> = emptyList(),
    val githubUrl: String = "",
    val demoUrl: String = "",
    val description: String = "",
    val ethicsCheck: Boolean = false,
    val screenShotFile: String? = null,
    val documentFile: String? = null,
    val approvalStatus: String = "Beklemede" // "Onaylandı", "Beklemede"
)

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val memberCount: Int = 0,
    val iconName: String = "robot_2", // "robot", "neurology", etc.
    val backgroundType: String = "blue" // "red", "blue" for decorative bg
)

data class Announcement(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val timeAgo: String = ""
)

data class Task(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val description: String = "",
    val priority: String = "", // "Yüksek Öncelik", ""
    val category: String = "", // "Yazılım", "Mekanik"
    val assigneeName: String = "",
    val dueDate: String = "",
    var status: String = "Yapılacak" // "Yapılacak", "Yapılıyor", "Tamamlandı"
)
