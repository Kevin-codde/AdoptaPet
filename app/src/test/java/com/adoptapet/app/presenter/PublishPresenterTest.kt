// app/src/test/java/com/adoptapet/app/presenter/PublishPresenterTest.kt
package com.adoptapet.app.presenter

import android.net.Uri
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.model.User
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.ui.contracts.PublishContract
import com.adoptapet.app.ui.presenter.PublishPresenter
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pruebas unitarias corregidas para [PublishPresenter].
 * Todos los entornos corren bajo 'runTest' para soportar funciones suspendidas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PublishPresenterTest {

    private lateinit var mockView: PublishContract.View
    private lateinit var mockPetRepository: PetRepository
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var mockUri: Uri
    private lateinit var presenter: PublishPresenter

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockView = mock()
        mockPetRepository = mock()
        mockAuthRepository = mock()
        mockFirebaseUser = mock()
        mockUri = mock()

        presenter = PublishPresenter(mockView, mockPetRepository, mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        presenter.onDestroy()
    }

    // ─── Tests de Publicación ─────────────────────────────────────────────────

    @Test
    fun `publishPet con campos vacios activa errores de validacion en la View`() = runTest {
        // Act
        presenter.publishPet("", "", "", "", "", "", "", mockUri)
        advanceUntilIdle()

        // Assert: Verifica que primero se limpie el error ("") y luego se muestre el texto real
        verify(mockView).showFieldError("name", "")
        verify(mockView).showFieldError("name", "El nombre es obligatorio")

        verify(mockView).showFieldError("type", "")
        verify(mockView).showFieldError("type", "Selecciona un tipo")

        verify(mockView).showFieldError("sex", "")
        verify(mockView).showFieldError("sex", "Selecciona el sexo")

        verify(mockView).showFieldError("city", "")
        verify(mockView).showFieldError("city", "La ciudad es obligatoria")

        verify(mockView).showFieldError("age", "")
        verify(mockView).showFieldError("age", "La edad es obligatoria")

        verify(mockView).showFieldError("description", "")
        verify(mockView).showFieldError("description", "La descripción es obligatoria")

        verify(mockView).showFieldError("contactInfo", "")
        verify(mockView).showFieldError("contactInfo", "El contacto es obligatorio")

        // El repositorio jamás debió ser contactado
        verify(mockPetRepository, never()).publishPet(any(), any())
    }

    @Test
    fun `publishPet sin foto lanza error inmediato`() = runTest {
        // Act
        presenter.publishPet("Max", "Perro", "Macho", "2 años", "Bogotá", "Lindo", "12345", null)
        advanceUntilIdle()

        // Assert
        verify(mockView).showError("Por favor selecciona una foto de la mascota")
        verify(mockPetRepository, never()).publishPet(any(), any())
    }

    @Test
    fun `publishPet sin sesion activa no permite guardar y pide login`() = runTest {
        // Arrange
        whenever(mockAuthRepository.getCurrentFirebaseUser()).thenReturn(null)

        // Act
        presenter.publishPet("Max", "Perro", "Macho", "2 años", "Bogotá", "Lindo", "12345", mockUri)
        advanceUntilIdle()

        // Assert
        verify(mockView).showError("Debes iniciar sesión para publicar")
        verify(mockPetRepository, never()).publishPet(any(), any())
    }

    @Test
    fun `publishPet exitoso coordina con el repositorio y limpia formulario`() = runTest {
        // Arrange
        whenever(mockAuthRepository.getCurrentFirebaseUser()).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("uid-123")
        whenever(mockFirebaseUser.email).thenReturn("test@user.com")
        whenever(mockAuthRepository.getCurrentUserProfile()).thenReturn(User("uid-123", "Juan", "test@user.com"))

        // Act
        presenter.publishPet("Max", "Perro", "Macho", "2 años", "Bogotá", "Lindo", "12345", mockUri)
        advanceUntilIdle()

        // Assert
        verify(mockView).showLoading(true)
        verify(mockPetRepository).publishPet(any(), eq(mockUri))
        verify(mockView).showLoading(false)
        verify(mockView).showPublishSuccess()
        verify(mockView).clearForm()
    }

    // ─── Tests de Edición y Carga ─────────────────────────────────────────────

    @Test
    fun `loadPetToEdit encuentra la mascota y rellena la UI`() = runTest {
        // Arrange
        val pet = Pet("id-pet-99", "Luna", "Gato", "Hembra", "1 año", "Medellín", "Cariñosa", "555", "uid-123", "Juan")
        whenever(mockPetRepository.getPetById("id-pet-99")).thenReturn(pet)

        // Act
        presenter.loadPetToEdit("id-pet-99")
        advanceUntilIdle()

        // Assert
        verify(mockView).showPetDataForEdit(pet)
    }

    @Test
    fun `loadPetToEdit con id inexistente gatilla mensaje de error`() = runTest {
        // Arrange
        whenever(mockPetRepository.getPetById("id-falso")).thenReturn(null)

        // Act
        presenter.loadPetToEdit("id-falso")
        advanceUntilIdle()

        // Assert
        verify(mockView).showError("No se pudo encontrar la información de la mascota")
    }

    @Test
    fun `updatePet exitoso realiza la modificacion y avisa a la vista`() = runTest {
        // Arrange
        val existingPet = Pet("id-123", "Max", "Perro", "Macho", "2 años", "Bogotá", "Lindo", "123", "uid-123", "Juan")
        whenever(mockPetRepository.getPetById("id-123")).thenReturn(existingPet)

        // Act
        presenter.updatePet("id-123", "Max Editado", "Perro", "Macho", "3 años", "Cali", "Muy Lindo", "123", mockUri)
        advanceUntilIdle()

        // Assert
        verify(mockView).showLoading(true)
        verify(mockPetRepository).updatePet(any(), eq(mockUri))
        verify(mockView).showLoading(false)
        verify(mockView).showPublishSuccess()
        verify(mockView).clearForm()
    }
}