// app/src/main/java/com/adoptapet/app/ui/detail/DetailFragment.kt
// Fragment de detalle de una mascota

package com.adoptapet.app.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.adoptapet.app.data.local.AppDatabase
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.databinding.FragmentDetailBinding
import com.adoptapet.app.ui.contracts.DetailContract
import com.adoptapet.app.ui.presenter.DetailPresenter

/**
 * Fragment de detalle de mascota.
 * Recibe el petId via Safe Args desde HomeFragment.
 */
class DetailFragment : Fragment(), DetailContract.View {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: DetailPresenter

    // Argumentos de navegación tipados (Safe Args)
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        presenter = DetailPresenter(this, PetRepository(db.petDao()))

        // Cargar detalle de la mascota con el ID recibido
        presenter.loadPetDetail(args.petId)
    }

    // ─── DetailContract.View ──────────────────────────────────────────────

    override fun showLoading(show: Boolean) {
        // El layout de detalle no tiene ProgressBar explícito; podría añadirse
    }

    override fun showPetDetail(pet: Pet) {
        with(binding) {
            tvDetailName.text = pet.name
            tvDetailType.text = pet.type.replaceFirstChar { it.uppercase() }
            tvDetailAge.text = pet.age

            // ─── CORRECCIÓN: Asignamos los datos que hacían falta ───
            tvDetailSex.text = pet.sex
            tvDetailCity.text = pet.city
            // ────────────────────────────────────────────────────────

            tvDetailDescription.text = pet.description
            tvDetailContact.text = pet.contactInfo
            tvDetailOwner.text = "Publicado por: ${pet.ownerName}"

            if (pet.photoUrl.isNotEmpty()) {
                ivDetailPhoto.load(pet.photoUrl) {
                    crossfade(true)
                    placeholder(com.adoptapet.app.R.drawable.ic_paw_placeholder)
                    error(com.adoptapet.app.R.drawable.ic_paw_placeholder)
                }
            } else {
                ivDetailPhoto.setImageResource(com.adoptapet.app.R.drawable.ic_paw_placeholder)
            }

            btnRequestAdoption.setOnClickListener {
                presenter.onRequestAdoption(pet)
            }
        }
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        findNavController().popBackStack()
    }

    override fun showContactInfo(contactInfo: String) {
        // 1. Inflar de forma manual el diseño personalizado del diálogo
        val dialogView = LayoutInflater.from(requireContext()).inflate(com.adoptapet.app.R.layout.dialog_contact_info, null)

        // 2. CORRECCIÓN: Construir el diálogo usando MaterialAlertDialogBuilder para un diseño adaptativo y con buen contraste
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // 3. Vincular los elementos internos del XML del diálogo
        val tvDialogContactValue = dialogView.findViewById<TextView>(com.adoptapet.app.R.id.tvDialogContactValue)
        val btnDialogClose = dialogView.findViewById<com.google.android.material.button.MaterialButton>(com.adoptapet.app.R.id.btnDialogClose)

        // 4. Inyectar el valor real de contacto de la mascota
        tvDialogContactValue.text = contactInfo

        // 5. Configurar el botón "ENTENDIDO" para cerrar el cuadro al hacer click
        btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        // 6. Lanzar el diálogo en primer plano
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        _binding = null
    }
}