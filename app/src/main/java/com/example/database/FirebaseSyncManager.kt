package com.example.database

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class FirebaseSyncManager(private val context: Context) {

    private val _isFirebaseEnabled = MutableStateFlow(false)
    val isFirebaseEnabled: StateFlow<Boolean> = _isFirebaseEnabled

    private val _syncStatus = MutableStateFlow("Unconfigured")
    val syncStatus: StateFlow<String> = _syncStatus

    private val _firebaseConfig = MutableStateFlow<FirebaseConfig?>(null)
    val firebaseConfig: StateFlow<FirebaseConfig?> = _firebaseConfig

    private var firebaseApp: FirebaseApp? = null
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    data class FirebaseConfig(
        val apiKey: String,
        val appId: String,
        val projectId: String
    )

    init {
        // Try to load initial configuration if present in SharedPreferences
        loadConfigAndInitialize()
    }

    private fun loadConfigAndInitialize() {
        val prefs = context.getSharedPreferences("firebase_prefs", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: ""
        val appId = prefs.getString("app_id", "") ?: ""
        val projectId = prefs.getString("project_id", "") ?: ""

        if (apiKey.isNotEmpty() && appId.isNotEmpty() && projectId.isNotEmpty()) {
            val config = FirebaseConfig(apiKey, appId, projectId)
            _firebaseConfig.value = config
            initializeFirebase(config)
        } else {
            // Also attempt default initialization if developer config is present or standard google-services was somehow applied
            try {
                val app = FirebaseApp.getInstance()
                firebaseApp = app
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                _isFirebaseEnabled.value = true
                _syncStatus.value = "Local Cloud Ready (Auto-Init)"
            } catch (e: Exception) {
                _isFirebaseEnabled.value = false
                _syncStatus.value = "Offline Mode (Provide config in Settings)"
            }
        }
    }

    fun saveConfig(apiKey: String, appId: String, projectId: String): Boolean {
        if (apiKey.trim().isEmpty() || appId.trim().isEmpty() || projectId.trim().isEmpty()) {
            return false
        }
        val prefs = context.getSharedPreferences("firebase_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("api_key", apiKey.trim())
            .putString("app_id", appId.trim())
            .putString("project_id", projectId.trim())
            .apply()

        val config = FirebaseConfig(apiKey.trim(), appId.trim(), projectId.trim())
        _firebaseConfig.value = config
        return initializeFirebase(config)
    }

    fun clearConfig() {
        val prefs = context.getSharedPreferences("firebase_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _firebaseConfig.value = null
        _isFirebaseEnabled.value = false
        _syncStatus.value = "Unconfigured"
        firebaseApp = null
        auth = null
        firestore = null
    }

    private fun initializeFirebase(config: FirebaseConfig): Boolean {
        return try {
            val options = FirebaseOptions.Builder()
                .setApiKey(config.apiKey)
                .setApplicationId(config.appId)
                .setProjectId(config.projectId)
                .build()

            // Firebase allows re-initializing apps using unique names
            val apps = FirebaseApp.getApps(context)
            firebaseApp = if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context, options)
            } else {
                FirebaseApp.getInstance()
            }

            auth = FirebaseAuth.getInstance(firebaseApp!!)
            firestore = FirebaseFirestore.getInstance(firebaseApp!!)
            _isFirebaseEnabled.value = true
            _syncStatus.value = "Connected to Cloud"
            true
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Initialization failed", e)
            _isFirebaseEnabled.value = false
            _syncStatus.value = "Failed: ${e.localizedMessage}"
            false
        }
    }

    suspend fun syncWatchlist(localWatchlist: List<String>): List<String> {
        val currentAuth = auth ?: return localWatchlist
        val curFirestore = firestore ?: return localWatchlist

        if (!_isFirebaseEnabled.value) return localWatchlist

        try {
            _syncStatus.value = "Syncing..."
            
            // Sign in anonymously if not already signed in
            var currentUser = currentAuth.currentUser
            if (currentUser == null) {
                val authResult = currentAuth.signInAnonymously().await()
                currentUser = authResult.user
            }

            val uid = currentUser?.uid ?: return localWatchlist
            val docRef = curFirestore.collection("users").document(uid)

            // 1. Fetch cloud watchlist
            val cloudSnapshot = docRef.get().await()
            val cloudWatchlist = cloudSnapshot.get("watchlist") as? List<String> ?: emptyList()

            // 2. Perform dynamic bidirectional merge
            val mergedWatchlist = (localWatchlist + cloudWatchlist).distinct()

            // 3. Write merged list back to cloud
            docRef.set(mapOf("watchlist" to mergedWatchlist)).await()

            _syncStatus.value = "Synced with Cloud (${mergedWatchlist.size} items)"
            return mergedWatchlist
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Sync failed", e)
            _syncStatus.value = "Sync Failed: ${e.localizedMessage}"
            return localWatchlist
        }
    }
}
