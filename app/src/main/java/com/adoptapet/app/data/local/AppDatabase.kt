// app/src/main/java/com/adoptapet/app/data/local/AppDatabase.kt
// Clase principal de Room Database

package com.adoptapet.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adoptapet.app.data.model.Pet

/**
 * Base de datos local Room para AdoptaPet.
 * Implementa el patrón Singleton para evitar múltiples instancias.
 * Versión 1: entidad Pet.
 */
@Database(
    entities = [Pet::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Provee acceso al DAO de mascotas */
    abstract fun petDao(): PetDao

    companion object {
        // Volatile garantiza que INSTANCE sea visible para todos los hilos
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna la instancia singleton de la base de datos.
         * Crea una nueva instancia si aún no existe, usando double-checked locking.
         *
         * @param context ApplicationContext para evitar memory leaks
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "adoptapet_database"
                )
                    // Permite destruir y recrear la DB en migraciones faltantes
                    // (en producción se implementarían migraciones explícitas)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
