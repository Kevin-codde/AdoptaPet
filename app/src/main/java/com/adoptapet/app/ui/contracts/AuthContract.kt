// app/src/main/java/com/adoptapet/app/ui/contracts/AuthContract.kt
// Contrato MVP para la pantalla de autenticación

package com.adoptapet.app.ui.contracts

import com.adoptapet.app.data.model.User

/**
 * Contrato MVP para la pantalla de Autenticación (Login/Registro).
 * Define el contrato estricto entre View y Presenter.
 */
interface AuthContract {

    /**
     * Interfaz que debe implementar la View (AuthActivity/Fragment).
     * El Presenter llama estos métodos para actualizar la UI.
     */
    interface View {
        fun showLoading(show: Boolean)
        fun showLoginSuccess(user: User)
        fun showRegisterSuccess(user: User)
        fun showError(message: String)
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun showNameError(message: String)
        fun navigateToMain()
    }

    /**
     * Interfaz que debe implementar el Presenter.
     * La View llama estos métodos en respuesta a eventos del usuario.
     */
    interface Presenter {
        fun login(email: String, password: String)
        fun register(name: String, email: String, password: String)
        fun onDestroy()
    }
}
