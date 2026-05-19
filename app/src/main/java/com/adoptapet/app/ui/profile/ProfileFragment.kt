// app/src/main/java/com/adoptapet/app/ui/profile/ProfileFragment.kt
// Fragment de perfil del usuario: muestra datos y mascotas publicadas

package com.adoptapet.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.adoptapet.app.data.local.AppDatabase
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.model.User
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.databinding.FragmentProfileBinding
import com.adoptapet.app.ui.adapter.MyPetAdapter
import com.adoptapet.app.ui.auth.AuthActivity
import com.adoptapet.app.ui.contracts.ProfileContract
import com.adoptapet.app.ui.presenter.ProfilePresenter

/**
 * Fragment de perfil de usuario.
 * Muestra información del usuario autenticado y sus mascotas publicadas.
 * Implementa [ProfileContract.View].
 */
class ProfileFragment : Fragment(), ProfileContract.View {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: ProfilePresenter
    private lateinit var myPetAdapter: MyPetAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        presenter = ProfilePresenter(this, PetRepository(db.petDao()))

        setupRecyclerView()
        setupClickListeners()

        // Cargar datos del perfil y mascotas
        presenter.loadProfile()
        presenter.loadMyPets()
    }

    private fun setupRecyclerView() {
        myPetAdapter = MyPetAdapter(
            onDeleteClick = { pet -> presenter.deletePet(pet) }
        )

        binding.rvMyPets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myPetAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            // Confirmar antes de cerrar sesión
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar tu sesión?")
                .setPositiveButton("Sí") { _, _ -> presenter.logout() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // ─── ProfileContract.View ─────────────────────────────────────────────

    override fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showUserProfile(user: User) {
        binding.tvUserName.text = user.name.ifEmpty { "Usuario" }
        binding.tvUserEmail.text = user.email
    }

    override fun showMyPets(pets: List<Pet>) {
        myPetAdapter.submitList(pets)
    }

    override fun showEmptyPets(show: Boolean) {
        binding.emptyMyPets.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvMyPets.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun showDeleteSuccess() {
        Toast.makeText(requireContext(), "Mascota eliminada correctamente", Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar una publicación.
     * Si el usuario confirma, llama a [ProfilePresenter.confirmDeletePet].
     */
    override fun showDeleteConfirmation(pet: Pet) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(com.adoptapet.app.R.string.delete_confirm_title))
            .setMessage(getString(com.adoptapet.app.R.string.delete_confirm_message))
            .setPositiveButton(getString(com.adoptapet.app.R.string.btn_confirm)) { _, _ ->
                presenter.confirmDeletePet(pet)
            }
            .setNegativeButton(getString(com.adoptapet.app.R.string.btn_cancel), null)
            .show()
    }

    override fun navigateToAuth() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        _binding = null
    }
}
