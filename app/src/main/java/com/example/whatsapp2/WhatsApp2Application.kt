package com.example.whatsapp2

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class WhatsApp2Application : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configurar Firestore UMA ÚNICA VEZ quando o app inicia
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)  // Habilitar cache local para sincronização offline
            .build()
        firestore.firestoreSettings = settings
    }
}
