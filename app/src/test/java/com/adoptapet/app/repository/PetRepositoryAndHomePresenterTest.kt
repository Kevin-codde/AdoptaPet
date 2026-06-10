// app/src/test/java/com/adoptapet/app/repository/PetRepositoryAndHomePresenterTest.kt
package com.adoptapet.app.repository

import com.adoptapet.app.data.local.PetDao
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.ui.contracts.HomeContract
import com.adoptapet.app.ui.presenter.HomePresenter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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

@OptIn(ExperimentalCoroutinesApi::class)
class PetRepositoryAndHomePresenterTest {

    private lateinit var mockDao: PetDao
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockStorage: FirebaseStorage
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
        mockFirestore = mock()
        mockStorage = mock()

        // Configurar el DAO mock para retornar datos básicos por defecto
        whenever(mockDao.getAllPets()).thenReturn(flowOf(samplePets))
        whenever(mockDao.searchPets(any())).thenReturn(flowOf(emptyList()))

        // Pasamos los mocks de Firebase para proteger el entorno JVM
        repository = PetRepository(mockDao, mockFirestore, mockStorage)
        homePresenter = HomePresenter(mockView, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        homePresenter.onDestroy()
    }

    // ─── Tests de PetRepository ───────────────────────────────────────────────

    @Test
    fun `getAllPetsLocal retorna flow del DAO`() = runTest {
        val flow = repository.getAllPetsLocal()
        assert(flow != null)
        verify(mockDao).getAllPets()
    }

    @Test
    fun `deletePet llama deleteById en el DAO con el ID correcto`() = runTest {
        // Arrange
        val petId = "pet_id_123"
        val mockCollection: CollectionReference = mock()
        val mockDocument: DocumentReference = mock()

        // Simulamos de forma segura la estructura encadenada de Firebase Firestore
        whenever(mockFirestore.collection(any())).thenReturn(mockCollection)
        whenever(mockCollection.document(any())).thenReturn(mockDocument)
        // Tasks.forResult(null) simula que el .await() remoto terminó exitosamente e inmediatamente
        whenever(mockDocument.delete()).thenReturn(Tasks.forResult(null))

        // Act
        repository.deletePet(petId)

        // Assert
        verify(mockDao).deleteById(petId)
    }

    @Test
    fun `searchPetsLocal delega al DAO con la query correcta`() = runTest {
        val query = "perro"
        repository.searchPetsLocal(query)
        verify(mockDao).searchPets(query)
    }

    // ─── Tests de HomePresenter ───────────────────────────────────────────────

    @Test
    fun `loadPets muestra lista de mascotas cuando el DAO tiene datos`() = runTest {
        homePresenter.loadPets()
        advanceUntilIdle()

        verify(mockView).showPets(samplePets)
        verify(mockView).showEmptyState(false)
    }

    @Test
    fun `loadPets muestra estado vacio cuando el DAO no tiene mascotas`() = runTest {
        // Arrange: Reconfiguramos el comportamiento del DAO únicamente para este test
        whenever(mockDao.getAllPets()).thenReturn(flowOf(emptyList()))

        // Act: Usamos el homePresenter común creado en el setUp
        homePresenter.loadPets()
        advanceUntilIdle()

        // Assert
        verify(mockView).showEmptyState(true)
    }

    @Test
    fun `searchPets con query vacio recarga todas las mascotas`() = runTest {
        homePresenter.searchPets("")
        advanceUntilIdle()

        verify(mockDao).getAllPets()
    }

    @Test
    fun `onPetSelected llama navigateToDetail en la View`() {
        val pet = samplePets[0]
        homePresenter.onPetSelected(pet)
        verify(mockView).navigateToDetail(pet)
    }
}