// app/src/androidTest/java/com/adoptapet/app/local/PetDaoTest.kt
// Prueba de instrumentación para PetDao con base de datos Room en memoria

package com.adoptapet.app.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adoptapet.app.data.local.AppDatabase
import com.adoptapet.app.data.local.PetDao
import com.adoptapet.app.data.model.Pet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de instrumentación para [PetDao].
 * Usa una base de datos Room en memoria para no contaminar datos reales.
 *
 * Se ejecutan en un dispositivo/emulador Android (no en JVM pura).
 * Correr con: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class PetDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: PetDao

    // Mascotas de prueba
    private val petA = Pet(
        id = "test-1",
        name = "Firulais",
        type = "Perro",
        age = "3 años",
        description = "Muy juguetón",
        contactInfo = "555-0001",
        ownerId = "owner-uid-1",
        ownerName = "Carlos"
    )

    private val petB = Pet(
        id = "test-2",
        name = "Mishi",
        type = "Gato",
        age = "2 años",
        description = "Tranquilo y cariñoso",
        contactInfo = "555-0002",
        ownerId = "owner-uid-2",
        ownerName = "Ana"
    )

    private val petC = Pet(
        id = "test-3",
        name = "Rex",
        type = "Perro",
        age = "1 año",
        description = "Energético",
        contactInfo = "555-0003",
        ownerId = "owner-uid-1",   // mismo dueño que petA
        ownerName = "Carlos"
    )

    @Before
    fun createDatabase() {
        // Base de datos en memoria: se destruye al terminar el test
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.petDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    /**
     * Test 1: Insertar y recuperar todas las mascotas.
     */
    @Test
    fun insertAndGetAllPets() = runBlocking {
        dao.insertAll(listOf(petA, petB, petC))

        val all = dao.getAllPets().first()

        assertEquals(3, all.size)
    }

    /**
     * Test 2: Obtener mascotas por propietario.
     * Solo deben retornarse las mascotas del owner-uid-1 (petA y petC).
     */
    @Test
    fun getPetsByOwner_returnsOnlyOwnerPets() = runBlocking {
        dao.insertAll(listOf(petA, petB, petC))

        val ownerPets = dao.getPetsByOwner("owner-uid-1").first()

        assertEquals(2, ownerPets.size)
        assert(ownerPets.all { it.ownerId == "owner-uid-1" })
    }

    /**
     * Test 3: Eliminar una mascota por ID.
     */
    @Test
    fun deleteById_removesPetFromDatabase() = runBlocking {
        dao.insertAll(listOf(petA, petB))

        dao.deleteById("test-1")

        val all = dao.getAllPets().first()
        assertEquals(1, all.size)
        assertEquals("test-2", all[0].id)
    }

    /**
     * Test 4: Búsqueda por nombre parcial.
     */
    @Test
    fun searchPets_byName_returnsMatchingPets() = runBlocking {
        dao.insertAll(listOf(petA, petB, petC))

        // Buscar "firu" debe encontrar "Firulais"
        val results = dao.searchPets("firu").first()

        assertEquals(1, results.size)
        assertEquals("Firulais", results[0].name)
    }

    /**
     * Test 5: Búsqueda por tipo de mascota.
     */
    @Test
    fun searchPets_byType_returnsAllMatches() = runBlocking {
        dao.insertAll(listOf(petA, petB, petC))

        // Buscar "perro" debe retornar petA y petC
        val results = dao.searchPets("perro").first()

        assertEquals(2, results.size)
    }

    /**
     * Test 6: getPetById retorna null si no existe.
     */
    @Test
    fun getPetById_nonExistent_returnsNull() = runBlocking {
        val result = dao.getPetById("id-inexistente")
        assertNull(result)
    }

    /**
     * Test 7: OnConflictStrategy.REPLACE — actualiza la mascota si el ID ya existe.
     */
    @Test
    fun insert_withSameId_replacesExistingPet() = runBlocking {
        dao.insert(petA)

        // Actualizar petA con nueva descripción
        val updated = petA.copy(description = "Descripción actualizada")
        dao.insert(updated)

        val result = dao.getPetById("test-1")
        assertEquals("Descripción actualizada", result?.description)

        // Debe seguir siendo 1 registro
        val all = dao.getAllPets().first()
        assertEquals(1, all.size)
    }

    /**
     * Test 8: deleteAll elimina todos los registros.
     */
    @Test
    fun deleteAll_clearsTable() = runBlocking {
        dao.insertAll(listOf(petA, petB, petC))

        dao.deleteAll()

        val all = dao.getAllPets().first()
        assertEquals(0, all.size)
    }
}
