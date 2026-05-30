package com.adoptapet.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adoptapet.app.R
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.databinding.ActivityMainBinding
import com.adoptapet.app.ui.auth.AuthActivity

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepo = AuthRepository()

        // Si no está logueado, salir de inmediato y no ejecutar nada más
        if (!authRepo.isUserLoggedIn()) {
            navigateToAuth()
            return
        }

        try {
            _binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupNavigation()
        } catch (e: Exception) {
            // Si hay un error de "segunda vez" por datos corruptos, limpiamos y reiniciamos
            navigateToAuth()
        }
    }

    private fun setupNavigation() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment
            navController = navHostFragment.navController
            binding.bottomNavigation.setupWithNavController(navController)
        } catch (e: Exception) {
            // Error común si el fragmento no carga a tiempo
            e.printStackTrace()
        }
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}