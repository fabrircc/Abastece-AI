package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class FuelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFirebaseSafely()
    }

    private fun initializeFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = try {
                    FirebaseApp.initializeApp(this)
                } catch (t: Throwable) {
                    Log.w("FuelApplication", "Default FirebaseApp.initializeApp failed, trying explicit options", t)
                    null
                }

                if (app == null) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:214838312519:android:860ce083d5b6588c48fb62")
                        .setApiKey("AIzaSyAqHAsBdXQzKbUsChOl4637EVlv2YjV4Is")
                        .setProjectId("abastece-ai-1ff31")
                        .setStorageBucket("abastece-ai-1ff31.firebasestorage.app")
                        .setGcmSenderId("214838312519")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                    Log.d("FuelApplication", "Firebase initialized with explicit options")
                } else {
                    Log.d("FuelApplication", "Firebase initialized successfully")
                }
            }
        } catch (e: Throwable) {
            Log.e("FuelApplication", "Error initializing Firebase: ${e.message}", e)
        }
    }
}
