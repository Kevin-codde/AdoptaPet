package com.adoptapet.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adoptapet.app.data.model.User
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.databinding.ActivityAuthBinding
import com.adoptapet.app.ui.contracts.AuthContract
import com.adoptapet.app.ui.main.MainActivity
import com.adoptapet.app.ui.presenter.AuthPresenter

class AuthActivity : AppCompatActivity(), AuthContract.View {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var presenter: AuthPresenter
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificación de sesión rápida antes de inflar la vista
        val authRepo = AuthRepository()
        if (authRepo.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = AuthPresenter(this)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPrimary.setOnClickListener {
            if (isRegisterMode) {
                presenter.register(
                    name = binding.etName.text.toString(),
                    email = binding.etEmail.text.toString(),
                    password = binding.etPassword.text.toString()
                )
            } else {
                presenter.login(
                    email = binding.etEmail.text.toString(),
                    password = binding.etPassword.text.toString()
                )
            }
        }

        binding.btnToggle.setOnClickListener { toggleMode() }
    }

    private fun toggleMode() {
        isRegisterMode = !isRegisterMode
        if (isRegisterMode) {
            binding.tilName.visibility = View.VISIBLE
            binding.spaceName.visibility = View.VISIBLE
            binding.btnPrimary.text = "Registrarse"
            binding.btnToggle.text = "¿Ya tienes cuenta? Inicia sesión"
        } else {
            binding.tilName.visibility = View.GONE
            binding.spaceName.visibility = View.GONE
            binding.btnPrimary.text = "Iniciar Sesión"
            binding.btnToggle.text = "¿No tienes cuenta? Regístrate"
        }
    }

    override fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnPrimary.isEnabled = !show
        binding.btnToggle.isEnabled = !show
    }

    override fun showLoginSuccess(user: User) {
        navigateToMain()
    }

    override fun showRegisterSuccess(user: User) {
        navigateToMain()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showEmailError(message: String) { binding.tilEmail.error = message }
    override fun showPasswordError(message: String) { binding.tilPassword.error = message }
    override fun showNameError(message: String) { binding.tilName.error = message }

    override fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::presenter.isInitialized) presenter.onDestroy()
    }
}