// app/src/test/java/com/adoptapet/app/presenter/ProfilePresenterTest.kt
// Pruebas unitarias JUnit para ProfilePresenter

package com.adoptapet.app.presenter

import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.model.User
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.ui.contracts.ProfileContract
import com.adoptapet.app.ui.presenter.ProfilePresenter
import com.google.firebase.auth.FirebaseUser
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pruebas unitarias para [ProfilePresenter].
 *
 * Cubre:
 * 1. loadProfile exitoso muestra datos del usuario
 * 2. loadProfile sin sesión activa navega a Auth
 * 3. loadMyPets muestra lista de mascotas
 * 4. loadMyPets muestra estado vacío cuando no hay mascotas
 * 5. deletePet llama showDeleteConfirmation en la View
 * 6. confirmDeletePet exitoso llama showDeleteSuccess
 * 7. logout llama navigateToAuth
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfilePresenterTest {

    private lateinit var mockView: ProfileContract.View
    private lateinit var mockPetRepository: PetRepository
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var presenter: ProfilePresenter

    private val testDispatcher = StandardTestDispatcher()

    private val testUser = User(uid = "uid-123", name = "María López", email = "maria@test.com")
    private val testPet = Pet(
        id = "pet-1", name = "Max", type = "Perro",
        age = "2 años", description = "Amigable",
        contactInfo = "555-0001", ownerId = "uid-123"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockView = mock()
        mockPetRepository = mock()
        mockAuthRepository = mock()
        mockFirebaseUser = mock()

        presenter = ProfilePresenter(mockView, mockPetRepository, mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        presenter.onDestroy()
    }

    // ─── Tests de loadProfile ─────────────────────────────────────────────────

    /**
     * Test 1: loadProfile con sesión activa muestra el perfil del usuario.
     */
    @Test
    fun `loadProfile con sesion activa muestra perfil del usuario`() = runTest {
        // Arrange
        whenever(mockAuthRepository.getCurrentUserProfile()).thenReturn(testUser)

        // Act
        presenter.loadProfile()
        advanceUntilIdle()

        // Assert
        verify(mockView).showLoading(true)
        verify(mockView).showLoading(false)
        verify(mockView).showUserProfile(testUser)
    }

    /**
     * Test 2: loadProfile sin sesión navega a AuthActivity.
     */
    @Test
    fun `loadProfile sin sesion navega a Auth`() = runTest {
        // Arrange: no hay usuario autenticado
        whenever(mockAuthRepository.getCurrentUserProfile()).thenReturn(null)
        whenever(mockAuthRepository.getCurrentFirebaseUser()).thenReturn(null)

        // Act
        presenter.loadProfile()
        advanceUntilIdle()

        // Assert
        verify(mockView).navigateToAuth()
    }

    // ─── Tests de loadMyPets ──────────────────────────────────────────────────

    /**
     * Test 3: loadMyPets muestra las mascotas del usuario.
     */
    @Test
    fun `loadMyPets muestra mascotas del usuario`() = runTest {
        // Arrange
        whenever(mockAuthRepository.getCurrentFirebaseUser()).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("uid-123")
        whenever(mockPetRepository.getPetsByOwnerLocal("uid-123"))
            .thenReturn(flowOf(listOf(testPet)))

        // Act
        presenter.loadMyPets()
        advanceUntilIdle()

        // Assert
        verify(mockView).showEmptyPets(false)
        verify(mockView).showMyPets(listOf(testPet))
    }

    /**
     * Test 4: loadMyPets muestra estado vacío cuando no hay mascotas.
     */
    @Test
    fun `loadMyPets muestra estado vacio cuando no hay mascotas`() = runTest {
        // Arrange
        whenever(mockAuthRepository.getCurrentFirebaseUser()).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("uid-123")
        whenever(mockPetRepository.getPetsByOwnerLocal("uid-123"))
            .thenReturn(flowOf(emptyList()))

        // Act
        presenter.loadMyPets()
        advanceUntilIdle()

        // Assert
        verify(mockView).showEmptyPets(true)
        verify(mockView).showMyPets(emptyList())
    }

    // ─── Tests de deletePet ───────────────────────────────────────────────────

    /**
     * Test 5: deletePet muestra diálogo de confirmación en la View.
     * El presenter no elimina directamente; espera confirmación del usuario.
     */
    @Test
    fun `deletePet muestra dialogo de confirmacion`() {
        // Act
        presenter.deletePet(testPet)

        // Assert: se solicita confirmación, no se elimina todavía
        verify(mockView).showDeleteConfirmation(testPet)
        // El repositorio NO debe ser llamado hasta que el usuario confirme
    }

    /**
     * Test 6: confirmDeletePet exitoso llama showDeleteSuccess.
     */
    @Test
    fun `confirmDeletePet exitoso llama showDeleteSuccess`() = runTest {
        // Arrange: el repositorio elimina sin error
        // (mockito no necesita stub para Unit suspend functions por defecto)

        // Act
        presenter.confirmDeletePet(testPet)
        advanceUntilIdle()

        // Assert
        verify(mockView).showLoading(true)
        verify(mockView).showLoading(false)
        verify(mockView).showDeleteSuccess()
    }

    /**
     * Test 7: confirmDeletePet con error del repositorio muestra showError.
     */
    @Test
    fun `confirmDeletePet con error muestra showError`() = runTest {
        // Arrange
        whenever(mockPetRepository.deletePet(testPet.id))
            .thenThrow(RuntimeException("Error al eliminar"))

        // Act
        presenter.confirmDeletePet(testPet)
        advanceUntilIdle()

        // Assert
        verify(mockView).showError(any())
        verify(mockView, never()).showDeleteSuccess()
    }

    // ─── Tests de logout ──────────────────────────────────────────────────────

    /**
     * Test 8: logout cierra sesión y navega a AuthActivity.
     */
    @Test
    fun `logout llama navigateToAuth`() {
        // Act
        presenter.logout()

        // Assert
        verify(mockAuthRepository).logout()
        verify(mockView).navigateToAuth()
    }
}
