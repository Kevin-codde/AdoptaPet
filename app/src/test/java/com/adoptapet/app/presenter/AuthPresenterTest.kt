// app/src/test/java/com/adoptapet/app/presenter/AuthPresenterTest.kt
// Pruebas unitarias JUnit para AuthPresenter

package com.adoptapet.app.presenter

import com.adoptapet.app.data.model.User
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.ui.contracts.AuthContract
import com.adoptapet.app.ui.presenter.AuthPresenter
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pruebas unitarias para [AuthPresenter].
 *
 * Cubre:
 * 1. Login exitoso con credenciales válidas
 * 2. Login fallido por email inválido (sin llamar al repositorio)
 * 3. Login fallido por contraseña corta (sin llamar al repositorio)
 * 4. Registro exitoso con datos completos
 * 5. Registro fallido por nombre vacío
 * 6. Error de red retornado por el repositorio
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthPresenterTest {

    // Mocks
    private lateinit var mockView: AuthContract.View
    private lateinit var mockRepository: AuthRepository
    private lateinit var presenter: AuthPresenter

    // Dispatcher de prueba para controlar coroutines
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Reemplaza Main dispatcher con el de prueba
        Dispatchers.setMain(testDispatcher)

        mockView = mock()
        mockRepository = mock()
        presenter = AuthPresenter(mockView, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        presenter.onDestroy()
    }

    // ─── Tests de Login ───────────────────────────────────────────────────────

    /**
     * Test 1: Login exitoso.
     * Dado un email y contraseña válidos, el repositorio retorna un usuario
     * y la View recibe showLoginSuccess + navigateToMain.
     */
    @Test
    fun `login con credenciales validas llama loginSuccess y navigateToMain`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val user = User(uid = "uid123", email = email, name = "Test User")

        whenever(mockRepository.login(email, password)).thenReturn(user)

        // Act
        presenter.login(email, password)
        advanceUntilIdle() // Espera a que completen las coroutines

        // Assert
        verify(mockView).showLoading(true)
        verify(mockView).showLoading(false)
        verify(mockView).showLoginSuccess(user)
        verify(mockView).navigateToMain()
    }

    /**
     * Test 2: Login con email inválido.
     * La validación debe fallar localmente sin llamar al repositorio.
     */
    @Test
    fun `login con email invalido muestra error de email sin llamar al repositorio`() = runTest {
        // Arrange
        val email = "no-es-un-email"
        val password = "password123"

        // Act
        presenter.login(email, password)
        advanceUntilIdle()

        // Assert: se muestra error en el campo email
        verify(mockView).showEmailError(any())
        // Assert: el repositorio NO fue llamado
        verify(mockRepository, never()).login(any(), any())
        // Assert: el loading NO se activó
        verify(mockView, never()).showLoading(any())
    }

    /**
     * Test 3: Login con contraseña muy corta.
     * La validación debe fallar antes de llamar al repositorio.
     */
    @Test
    fun `login con contrasena corta muestra error de contrasena`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "123" // Menos de 6 caracteres

        // Act
        presenter.login(email, password)
        advanceUntilIdle()

        // Assert
        verify(mockView).showPasswordError(any())
        verify(mockRepository, never()).login(any(), any())
    }

    /**
     * Test 4: Login con error de red del repositorio.
     * Cuando el repositorio lanza excepción, la View recibe showError.
     */
    @Test
    fun `login con error de repositorio muestra showError`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"

        whenever(mockRepository.login(email, password))
            .thenThrow(RuntimeException("network error"))

        // Act
        presenter.login(email, password)
        advanceUntilIdle()

        // Assert
        verify(mockView).showLoading(true)
        verify(mockView).showLoading(false)
        verify(mockView).showError(any())
        verify(mockView, never()).navigateToMain()
    }

    // ─── Tests de Registro ────────────────────────────────────────────────────

    /**
     * Test 5: Registro exitoso con datos completos.
     */
    @Test
    fun `registro con datos validos llama registerSuccess y navigateToMain`() = runTest {
        // Arrange
        val name = "María García"
        val email = "maria@example.com"
        val password = "mipassword"
        val user = User(uid = "uid456", email = email, name = name)

        whenever(mockRepository.register(name, email, password)).thenReturn(user)

        // Act
        presenter.register(name, email, password)
        advanceUntilIdle()

        // Assert
        verify(mockView).showLoading(true)
        verify(mockView).showLoading(false)
        verify(mockView).showRegisterSuccess(user)
        verify(mockView).navigateToMain()
    }

    /**
     * Test 6: Registro con nombre vacío.
     * La validación falla localmente sin llamar al repositorio.
     */
    @Test
    fun `registro con nombre vacio muestra error de nombre`() = runTest {
        // Arrange
        val name = ""
        val email = "test@example.com"
        val password = "password123"

        // Act
        presenter.register(name, email, password)
        advanceUntilIdle()

        // Assert
        verify(mockView).showNameError(any())
        verify(mockRepository, never()).register(any(), any(), any())
    }
}
