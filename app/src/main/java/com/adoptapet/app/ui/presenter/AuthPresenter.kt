// app/src/main/java/com/adoptapet/app/ui/presenter/AuthPresenter.kt
// Presenter MVP para autenticación: login y registro

package com.adoptapet.app.ui.presenter

import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.ui.contracts.AuthContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Presenter de autenticación.
 * Contiene TODA la lógica de negocio para login y registro.
 * No tiene referencias directas a Android SDK (testeable con JUnit puro).
 *
 * @param view        Referencia a la interfaz View (AuthActivity)
 * @param repository  Repositorio de autenticación inyectado
 */
class AuthPresenter(
    private var view: AuthContract.View?,
    private val repository: AuthRepository = AuthRepository()
) : AuthContract.Presenter {

    // CoroutineScope con Dispatchers.Main para actualizar UI desde coroutines
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * Valida las credenciales y ejecuta el login.
     * Actualiza la View según el resultado (éxito o error).
     */
    override fun login(email: String, password: String) {
        // Validación de campos
        if (!validateLoginFields(email, password)) return

        view?.showLoading(true)

        presenterScope.launch {
            try {
                val user = repository.login(email.trim(), password)
                view?.showLoading(false)
                view?.showLoginSuccess(user)
                view?.navigateToMain()
            } catch (e: Exception) {
                view?.showLoading(false)
                val errorMsg = parseFirebaseError(e.message)
                view?.showError(errorMsg)
            }
        }
    }

    // ─── Registro ─────────────────────────────────────────────────────────────

    /**
     * Valida los datos y ejecuta el registro de nuevo usuario.
     */
    override fun register(name: String, email: String, password: String) {
        // Validación de campos de registro
        if (!validateRegisterFields(name, email, password)) return

        view?.showLoading(true)

        presenterScope.launch {
            try {
                val user = repository.register(name.trim(), email.trim(), password)
                view?.showLoading(false)
                view?.showRegisterSuccess(user)
                view?.navigateToMain()
            } catch (e: Exception) {
                view?.showLoading(false)
                val errorMsg = parseFirebaseError(e.message)
                view?.showError(errorMsg)
            }
        }
    }

    // ─── Validaciones ─────────────────────────────────────────────────────────

    /**
     * Valida los campos del formulario de login.
     * @return true si todos los campos son válidos
     */
    private fun validateLoginFields(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            view?.showEmailError("El correo es obligatorio")
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view?.showEmailError("Correo electrónico inválido")
            isValid = false
        }

        if (password.isBlank()) {
            view?.showPasswordError("La contraseña es obligatoria")
            isValid = false
        } else if (password.length < 6) {
            view?.showPasswordError("Mínimo 6 caracteres")
            isValid = false
        }

        return isValid
    }

    /**
     * Valida los campos del formulario de registro.
     * @return true si todos los campos son válidos
     */
    private fun validateRegisterFields(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isBlank()) {
            view?.showNameError("El nombre es obligatorio")
            isValid = false
        }

        if (email.isBlank()) {
            view?.showEmailError("El correo es obligatorio")
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view?.showEmailError("Correo electrónico inválido")
            isValid = false
        }

        if (password.isBlank()) {
            view?.showPasswordError("La contraseña es obligatoria")
            isValid = false
        } else if (password.length < 6) {
            view?.showPasswordError("Mínimo 6 caracteres")
            isValid = false
        }

        return isValid
    }

    /**
     * Traduce mensajes de error de Firebase a mensajes amigables en español.
     */
    private fun parseFirebaseError(message: String?): String {
        return when {
            message == null -> "Ocurrió un error desconocido"
            message.contains("email address is already in use") ->
                "Este correo ya está registrado"
            message.contains("no user record") || message.contains("password is invalid") ->
                "Correo o contraseña incorrectos"
            message.contains("network") || message.contains("Network") ->
                "Error de conexión. Verifica tu internet"
            message.contains("badly formatted") ->
                "Formato de correo inválido"
            else -> "Error: $message"
        }
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    /**
     * Libera recursos cuando la View se destruye.
     * Cancela coroutines en vuelo y elimina la referencia a la View
     * para evitar memory leaks.
     */
    override fun onDestroy() {
        presenterScope.cancel()
        view = null
    }
}
