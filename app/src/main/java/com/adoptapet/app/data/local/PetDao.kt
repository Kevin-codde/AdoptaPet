// app/src/main/java/com/adoptapet/app/data/local/PetDao.kt
// DAO de Room para operaciones locales sobre mascotas

package com.adoptapet.app.data.local

import androidx.room.*
import com.adoptapet.app.data.model.Pet
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad [Pet] en Room.
 * Provee operaciones CRUD y consultas especializadas usando Coroutines + Flow.
 */
@Dao
interface PetDao {

    /**
     * Inserta o reemplaza una lista de mascotas en la base de datos local.
     * Usado para sincronizar el caché con Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pets: List<Pet>)

    /**
     * Inserta o reemplaza una sola mascota.
     * Usado al publicar una nueva mascota.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: Pet)

    /**
     * Elimina una mascota por su ID.
     */
    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun deleteById(petId: String)

    /**
     * Retorna todas las mascotas ordenadas por fecha de creación descendente.
     * Emite actualizaciones automáticamente cuando la DB cambia (Flow).
     */
    @Query("SELECT * FROM pets ORDER BY createdAt DESC")
    fun getAllPets(): Flow<List<Pet>>

    /**
     * Retorna todas las mascotas publicadas por un usuario específico.
     */
    @Query("SELECT * FROM pets WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getPetsByOwner(ownerId: String): Flow<List<Pet>>

    /**
     * Busca mascotas por nombre o tipo (búsqueda insensible a mayúsculas).
     */
    @Query("SELECT * FROM pets WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' OR LOWER(type) LIKE '%' || LOWER(:query) || '%' ORDER BY createdAt DESC")
    fun searchPets(query: String): Flow<List<Pet>>

    /**
     * Obtiene una mascota por su ID.
     */
    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPetById(petId: String): Pet?

    /**
     * Elimina todas las mascotas (para reset de caché).
     */
    @Query("DELETE FROM pets")
    suspend fun deleteAll()
}
