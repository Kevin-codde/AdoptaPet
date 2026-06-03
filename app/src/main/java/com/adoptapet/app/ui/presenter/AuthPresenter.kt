// app/src/main/java/com/adoptapet/app/ui/presenter/AuthPresenter.kt
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
 * Contiene TODA la lógica de negocio para login, registro y recuperación de contraseña.
 */
class AuthPresenter(
    private var view: AuthContract.View?,
    private val repository: AuthRepository = AuthRepository()
) : AuthContract.Presenter {

    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    // ─── Login ────────────────────────────────────────────────────────────────

    override fun login(email: String, password: String) {
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

    override fun register(name: String, email: String, password: String) {
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

    // ─── Recuperar Contraseña ──────────────────────────────────────────────────

    override fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            view?.showEmailError("El correo es obligatorio")
            return
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view?.showEmailError("Correo electrónico inválido")
            return
        }

        view?.showLoading(true)

        presenterScope.launch {
            try {
                // Se invoca el método asíncrono desde el repositorio usando corrutinas
                repository.sendPasswordResetEmail(email.trim())
                view?.showLoading(false)
                view?.showResetPasswordEmailSent()
            } catch (e: Exception) {
                view?.showLoading(false)
                val errorMsg = parseFirebaseError(e.message)
                view?.showError(errorMsg)
            }
        }
    }

    // ─── Validaciones ─────────────────────────────────────────────────────────

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

    private fun parseFirebaseError(message: String?): String {
        return when {
            message == null -> "Ocurrió un error desconocido"
            message.contains("email address is already in use") -> "Este correo ya está registrado"
            message.contains("no user record") || message.contains("password is invalid") || message.contains("wrong-password") -> "Correo o contraseña incorrectos"
            message.contains("network") || message.contains("Network") -> "Error de conexión. Verifica tu internet"
            message.contains("badly formatted") -> "Formato de correo inválido"
            else -> "Error: $message"
        }
    }

    override fun onDestroy() {
        presenterScope.cancel()
        view = null
    }
}