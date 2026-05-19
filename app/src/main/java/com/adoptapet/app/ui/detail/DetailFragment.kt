// app/src/main/java/com/adoptapet/app/ui/detail/DetailFragment.kt
// Fragment de detalle de una mascota

package com.adoptapet.app.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        // Mostrar información de contacto en un diálogo
        AlertDialog.Builder(requireContext())
            .setTitle("Información de contacto")
            .setMessage(contactInfo)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        _binding = null
    }
}
