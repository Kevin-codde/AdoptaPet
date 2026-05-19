// app/src/test/java/com/adoptapet/app/repository/PetRepositoryTest.kt
// Pruebas unitarias JUnit para PetRepository y HomePresenter

package com.adoptapet.app.repository

import com.adoptapet.app.data.local.PetDao
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.ui.contracts.HomeContract
import com.adoptapet.app.ui.presenter.HomePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pruebas unitarias para [PetRepository] y [HomePresenter].
 *
 * Cubre:
 * 1. Repository: getAllPetsLocal retorna el Flow del DAO
 * 2. Repository: deletePet llama a deleteById en el DAO
 * 3. Repository: searchPetsLocal delega al DAO correctamente
 * 4. HomePresenter: loadPets muestra estado vacío cuando no hay mascotas
 * 5. HomePresenter: loadPets muestra lista cuando hay mascotas
 * 6. HomePresenter: searchPets delega al repositorio correctamente
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PetRepositoryAndHomePresenterTest {

    private lateinit var mockDao: PetDao
    private lateinit var repository: PetRepository
    private lateinit var mockView: HomeContract.View
    private lateinit var homePresenter: HomePresenter

    private val testDispatcher = StandardTestDispatcher()

    // Datos de prueba
    private val samplePets = listOf(
        Pet(id = "1", name = "Max", type = "Perro", age = "2 años",
            description = "Muy amigable", contactInfo = "555-1234", ownerId = "uid1"),
        Pet(id = "2", name = "Luna", type = "Gato", age = "1 año",
            description = "Cariñosa", contactInfo = "555-5678", ownerId = "uid2")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockDao = mock()
        mockView = mock()

        // Configurar el DAO mock para retornar datos básicos
        whenever(mockDao.getAllPets()).thenReturn(flowOf(samplePets))
        whenever(mockDao.searchPets(any())).thenReturn(flowOf(emptyList()))

        repository = PetRepository(mockDao)
        homePresenter = HomePresenter(mockView, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        homePresenter.onDestroy()
    }

    // ─── Tests de PetRepository ───────────────────────────────────────────────

    /**
     * Test 1: getAllPetsLocal retorna el Flow del DAO directamente.
     */
    @Test
    fun `getAllPetsLocal retorna flow del DAO`() = runTest {
        // Act
        val flow = repository.getAllPetsLocal()

        // Assert: el flow no es null (existe)
        assert(flow != null)
        // Verificar que se llamó al DAO
        verify(mockDao).getAllPets()
    }

    /**
     * Test 2: deletePet llama a deleteById en el DAO con el ID correcto.
     */
    @Test
    fun `deletePet llama deleteById en el DAO con el ID correcto`() = runTest {
        // Arrange
        val petId = "pet_id_123"

        // Act
        repository.deletePet(petId)

        // Assert
        verify(mockDao).deleteById(petId)
    }

    /**
     * Test 3: searchPetsLocal delega al DAO con la query correcta.
     */
    @Test
    fun `searchPetsLocal delega al DAO con la query correcta`() = runTest {
        // Arrange
        val query = "perro"

        // Act
        repository.searchPetsLocal(query)

        // Assert
        verify(mockDao).searchPets(query)
    }

    // ─── Tests de HomePresenter ───────────────────────────────────────────────

    /**
     * Test 4: loadPets muestra la lista cuando el DAO tiene mascotas.
     */
    @Test
    fun `loadPets muestra lista de mascotas cuando el DAO tiene datos`() = runTest {
        // El DAO ya está configurado para retornar samplePets en setUp

        // Act
        homePresenter.loadPets()
        advanceUntilIdle()

        // Assert: la View recibe los datos
        verify(mockView).showPets(samplePets)
        verify(mockView).showEmptyState(false)
    }

    /**
     * Test 5: loadPets muestra estado vacío cuando no hay mascotas.
     */
    @Test
    fun `loadPets muestra estado vacio cuando el DAO no tiene mascotas`() = runTest {
        // Arrange: DAO retorna lista vacía
        whenever(mockDao.getAllPets()).thenReturn(flowOf(emptyList()))
        val repository2 = PetRepository(mockDao)
        val presenter2 = HomePresenter(mockView, repository2)

        // Act
        presenter2.loadPets()
        advanceUntilIdle()

        // Assert
        verify(mockView).showEmptyState(true)

        presenter2.onDestroy()
    }

    /**
     * Test 6: searchPets con query vacío recarga todas las mascotas.
     * Una búsqueda en blanco debe volver a mostrar la lista completa.
     */
    @Test
    fun `searchPets con query vacio recarga todas las mascotas`() = runTest {
        // Act: búsqueda vacía
        homePresenter.searchPets("")
        advanceUntilIdle()

        // Assert: se llama a getAllPets (no a searchPets) para mostrar todo
        verify(mockDao).getAllPets()
    }

    /**
     * Test 7: onPetSelected delega la navegación a la View.
     */
    @Test
    fun `onPetSelected llama navigateToDetail en la View`() {
        // Arrange
        val pet = samplePets[0]

        // Act
        homePresenter.onPetSelected(pet)

        // Assert
        verify(mockView).navigateToDetail(pet)
    }
}
