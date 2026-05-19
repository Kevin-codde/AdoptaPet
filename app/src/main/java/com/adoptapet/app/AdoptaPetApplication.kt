// app/src/main/java/com/adoptapet/app/AdoptaPetApplication.kt
// Clase Application: Punto de entrada global para inicializar dependencias

package com.adoptapet.app

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Clase Application de AdoptaPet.
 * Inicializa Firebase y cualquier dependencia global al arrancar la app.
 */
class AdoptaPetApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
    }
}
