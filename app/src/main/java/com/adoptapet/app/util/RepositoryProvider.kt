// app/src/main/java/com/adoptapet/app/util/RepositoryProvider.kt
// Proveedor centralizado de repositorios (evita duplicar instanciación en Fragments)

package com.adoptapet.app.util

import android.content.Context
import com.adoptapet.app.data.local.AppDatabase
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.data.repository.PetRepository

/**
 * Objeto singleton para proveer instancias de repositorios.
 * Centraliza la creación y evita pasar Context a los Presenters.
 *
 * Uso en Fragments:
 * ```kotlin
 * val petRepo = RepositoryProvider.petRepository(requireContext())
 * val authRepo = RepositoryProvider.authRepository()
 * ```
 */
object RepositoryProvider {

    /**
     * Retorna una instancia de [PetRepository] con la base de datos local.
     */
    fun petRepository(context: Context): PetRepository {
        val db = AppDatabase.getDatabase(context)
        return PetRepository(db.petDao())
    }

    /**
     * Retorna una instancia de [AuthRepository].
     * No requiere Context porque Firebase se inicializa en Application.
     */
    fun authRepository(): AuthRepository = AuthRepository()
}
