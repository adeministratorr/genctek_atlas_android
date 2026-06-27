package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import com.example.ui.screens.messages.ChatMessage
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FirebaseRepository {
    private const val TAG = "FirebaseRepository"
    private var isFirebaseInitialized = false
    private var database: FirebaseDatabase? = null

    fun initialize(context: Context) {
        if (isFirebaseInitialized) return

        try {
            // Check if default app is configured (via google-services.json)
            val defaultApp = try {
                FirebaseApp.getInstance()
            } catch (e: Exception) {
                null
            }

            if (defaultApp != null) {
                database = FirebaseDatabase.getInstance()
                isFirebaseInitialized = true
                Log.d(TAG, "Firebase initialized successfully via google-services.json!")
                return
            }

            // Fallback: Dynamically initialize using .env credentials in BuildConfig
            val apiKey = BuildConfig.FIREBASE_API_KEY
            val applicationId = BuildConfig.FIREBASE_APPLICATION_ID
            val projectId = BuildConfig.FIREBASE_PROJECT_ID
            val databaseUrl = BuildConfig.FIREBASE_DATABASE_URL

            if (apiKey.isNotEmpty() && apiKey != "your_firebase_api_key_here" &&
                applicationId.isNotEmpty() && applicationId != "your_firebase_app_id_here" &&
                projectId.isNotEmpty() && projectId != "your_firebase_project_id_here") {

                val builder = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(applicationId)
                    .setProjectId(projectId)

                if (databaseUrl.isNotEmpty() && databaseUrl != "your_database_url_here") {
                    builder.setDatabaseUrl(databaseUrl)
                }

                FirebaseApp.initializeApp(context, builder.build())
                
                database = if (databaseUrl.isNotEmpty() && databaseUrl != "your_database_url_here") {
                    FirebaseDatabase.getInstance(databaseUrl)
                } else {
                    FirebaseDatabase.getInstance()
                }
                
                isFirebaseInitialized = true
                Log.d(TAG, "Firebase dynamically initialized successfully via .env keys!")
            } else {
                Log.w(TAG, "Firebase credentials in .env are not fully configured. Running in Demo/Offline mode.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }

    fun isEnabled(): Boolean {
        return isFirebaseInitialized && database != null
    }

    // Generic helper to watch a node
    private fun <T> syncNode(path: String, clazz: Class<T>, onUpdate: (List<T>) -> Unit) {
        val ref = database?.getReference(path) ?: return
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<T>()
                for (child in snapshot.children) {
                    try {
                        val item = child.getValue(clazz)
                        if (item != null) {
                            list.add(item)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing $path item", e)
                    }
                }
                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Sync cancelled for $path: ${error.message}")
            }
        })
    }

    // Generic helper to push to a node
    private fun writeNode(path: String, id: String, value: Any) {
        val ref = database?.getReference("$path/$id") ?: return
        ref.setValue(value)
            .addOnSuccessListener { Log.d(TAG, "Successfully wrote to $path/$id") }
            .addOnFailureListener { e -> Log.e(TAG, "Failed writing to $path/$id", e) }
    }

    // ==========================================
    // SYNC METHODS
    // ==========================================

    fun syncEvents(onUpdate: (List<Event>) -> Unit) {
        syncNode("events", Event::class.java, onUpdate)
    }

    fun addEvent(event: Event) {
        writeNode("events", event.id, event)
    }

    fun syncProjects(onUpdate: (List<Project>) -> Unit) {
        syncNode("projects", Project::class.java, onUpdate)
    }

    fun addProject(project: Project) {
        writeNode("projects", project.id, project)
    }

    fun syncGroups(onUpdate: (List<Group>) -> Unit) {
        syncNode("groups", Group::class.java, onUpdate)
    }

    fun addGroup(group: Group) {
        writeNode("groups", group.id, group)
    }

    fun syncAnnouncements(onUpdate: (List<Announcement>) -> Unit) {
        syncNode("announcements", Announcement::class.java, onUpdate)
    }

    fun addAnnouncement(announcement: Announcement) {
        writeNode("announcements", announcement.id, announcement)
    }

    fun syncTasks(onUpdate: (List<Task>) -> Unit) {
        syncNode("tasks", Task::class.java, onUpdate)
    }

    fun addTask(task: Task) {
        writeNode("tasks", task.id, task)
    }

    // ==========================================
    // REAL-TIME MESSAGING SYNC
    // ==========================================

    fun syncMessages(conversationId: String, onUpdate: (List<ChatMessage>) -> Unit): ValueEventListener? {
        val ref = database?.getReference("messages/$conversationId") ?: return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    try {
                        // Manual mapping to handle custom classes perfectly
                        val id = child.child("id").getValue(String::class.java) ?: ""
                        val senderName = child.child("senderName").getValue(String::class.java) ?: ""
                        val text = child.child("text").getValue(String::class.java) ?: ""
                        val time = child.child("time").getValue(String::class.java) ?: ""
                        val isMe = child.child("isMe").getValue(Boolean::class.java) ?: false
                        
                        if (id.isNotEmpty()) {
                            list.add(ChatMessage(id, senderName, text, time, isMe))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing ChatMessage", e)
                    }
                }
                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Messages sync cancelled for $conversationId", error.toException())
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeMessagesListener(conversationId: String, listener: ValueEventListener) {
        database?.getReference("messages/$conversationId")?.removeEventListener(listener)
    }

    fun sendMessage(conversationId: String, message: ChatMessage) {
        writeNode("messages/$conversationId", message.id, message)
    }

    suspend fun runDiagnostics(context: Context): List<DiagnosticStep> {
        val steps = mutableListOf<DiagnosticStep>()

        // Step 1: Firebase App Initialization Check
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                steps.add(DiagnosticStep(
                    name = "Firebase Uygulama Başlatma",
                    status = "Başarılı",
                    details = "FirebaseApp başarıyla başlatıldı. Aktif uygulama sayısı: ${apps.size}. Proje ID: ${apps.first().options.projectId}"
                ))
            } else {
                steps.add(DiagnosticStep(
                    name = "Firebase Uygulama Başlatma",
                    status = "Başarısız",
                    details = "Herhangi bir aktif FirebaseApp bulunamadı."
                ))
            }
        } catch (e: Exception) {
            steps.add(DiagnosticStep(
                name = "Firebase Uygulama Başlatma",
                status = "Başarısız",
                details = "Hata oluştu: ${e.message}"
            ))
        }

        // Step 2: Realtime Database Connection and Read/Write Check
        if (isEnabled()) {
            steps.add(DiagnosticStep(
                name = "Realtime Database Yapılandırması",
                status = "Başarılı",
                details = "Bağlantı aktif. URL: ${database?.reference?.toString() ?: "Varsayılan"}"
            ))

            steps.add(DiagnosticStep(
                name = "Realtime Database Okuma/Yazma Testi",
                status = "Çalışıyor...",
                details = "Yazma ve okuma istekleri gönderiliyor..."
            ))

            val rdbResult = testRealtimeDatabase()
            if (rdbResult == "Okuma-Yazma Başarılı") {
                steps.removeAt(steps.lastIndex)
                steps.add(DiagnosticStep(
                    name = "Realtime Database Okuma/Yazma Testi",
                    status = "Başarılı",
                    details = "Veri başarıyla yazıldı, geri okundu ve temizlendi."
                ))
            } else {
                steps.removeAt(steps.lastIndex)
                steps.add(DiagnosticStep(
                    name = "Realtime Database Okuma/Yazma Testi",
                    status = "Başarısız",
                    details = rdbResult
                ))
            }
        } else {
            steps.add(DiagnosticStep(
                name = "Realtime Database Yapılandırması",
                status = "Atlandı",
                details = "Firebase bağlantısı etkinleştirilmedi veya yapılandırma eksik."
            ))
        }

        // Step 3: Cloud Firestore Configuration and Read/Write Check
        try {
            val firestore = FirebaseFirestore.getInstance()
            steps.add(DiagnosticStep(
                name = "Cloud Firestore Yapılandırması",
                status = "Başarılı",
                details = "Firestore örneği başarıyla alındı."
            ))

            val firestoreResult = testFirestore(firestore)
            if (firestoreResult == "Okuma-Yazma Başarılı") {
                steps.add(DiagnosticStep(
                    name = "Cloud Firestore Okuma/Yazma Testi",
                    status = "Başarılı",
                    details = "Test belgesi başarıyla yazıldı, geri okundu ve temizlendi."
                ))
            } else {
                steps.add(DiagnosticStep(
                    name = "Cloud Firestore Okuma/Yazma Testi",
                    status = "Başarısız",
                    details = firestoreResult
                ))
            }
        } catch (e: Exception) {
            steps.add(DiagnosticStep(
                name = "Cloud Firestore Yapılandırması",
                status = "Başarısız",
                details = "Firestore başlatılamadı veya kütüphane eksik. Hata: ${e.localizedMessage ?: e.message}"
            ))
        }

        return steps
    }

    private suspend fun testRealtimeDatabase(): String {
        val db = database ?: return "Veritabanı referansı null."
        val ref = db.getReference("diagnostics_connection_test")
        return suspendCancellableCoroutine { continuation ->
            val testData = mapOf("timestamp" to System.currentTimeMillis(), "status" to "running")
            ref.setValue(testData)
                .addOnSuccessListener {
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val value = snapshot.child("status").getValue(String::class.java)
                            if (value == "running") {
                                ref.removeValue().addOnCompleteListener {
                                    if (continuation.isActive) continuation.resume("Okuma-Yazma Başarılı")
                                }
                            } else {
                                if (continuation.isActive) continuation.resume("Okuma başarısız: Veri uyuşmazlığı.")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            if (continuation.isActive) continuation.resume("Okuma hatası: ${error.message}")
                        }
                    })
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume("Yazma hatası: ${e.message}")
                }
        }
    }

    private suspend fun testFirestore(firestore: FirebaseFirestore): String {
        val docRef = firestore.collection("diagnostics_connection_test").document("test_doc")
        return suspendCancellableCoroutine { continuation ->
            val testData = mapOf("timestamp" to System.currentTimeMillis(), "status" to "running")
            docRef.set(testData)
                .addOnSuccessListener {
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.getString("status") == "running") {
                                docRef.delete().addOnCompleteListener {
                                    if (continuation.isActive) continuation.resume("Okuma-Yazma Başarılı")
                                }
                            } else {
                                if (continuation.isActive) continuation.resume("Okuma başarısız: Belge bulunamadı veya veri uyuşmazlığı.")
                            }
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume("Okuma hatası: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume("Yazma hatası: ${e.message}")
                }
        }
    }
}

data class DiagnosticStep(
    val name: String,
    val status: String, // "Başarılı", "Başarısız", "Atlandı", "Çalışıyor..."
    val details: String
)
