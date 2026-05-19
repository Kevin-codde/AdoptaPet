// app/src/main/java/com/adoptapet/app/ui/publish/PublishFragment.kt
package com.adoptapet.app.ui.publish

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.adoptapet.app.data.local.AppDatabase
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.databinding.FragmentPublishBinding
import com.adoptapet.app.ui.contracts.PublishContract
import com.adoptapet.app.ui.presenter.PublishPresenter

/**
 * Fragment de publicación actualizado para AdoptaPet.
 * Gestiona la selección de imágenes locales y la publicación hacia Firebase Storage.
 */
class PublishFragment : Fragment(), PublishContract.View {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!
    private lateinit var presenter: PublishPresenter
    private var selectedPhotoUri: Uri? = null

    // Launcher actualizado para manejar la selección de imágenes de forma más robusta
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedPhotoUri = uri
                showPhotoPreview(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPublishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialización del Presenter
        val db = AppDatabase.getDatabase(requireContext())
        presenter = PublishPresenter(this, PetRepository(db.petDao()))

        setupPetTypeDropdown()
        setupClickListeners()
    }

    private fun setupPetTypeDropdown() {
        val types = listOf("Perro", "Gato", "Conejo", "Ave", "Otro")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.actvType.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        // Clic en el CardView de la huellita
        binding.cardPhoto.setOnClickListener {
            openGallery()
        }

        // Botón Publicar con lógica limpia de 6 parámetros
        binding.btnPublish.setOnClickListener {
            // Limpiar errores previos antes de validar
            binding.tilName.error = null
            binding.tilType.error = null
            binding.tilAge.error = null

            presenter.publishPet(
                name = binding.etName.text.toString(),
                type = binding.actvType.text.toString(),
                age = binding.etAge.text.toString(),
                description = binding.etDescription.text.toString(),
                contactInfo = binding.etContact.text.toString(),
                photoUri = selectedPhotoUri
            )
        }
    }

    private fun openGallery() {
        // Intent robusto para abrir el selector de contenido (Galería/Archivos)
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickImageLauncher.launch(Intent.createChooser(intent, "Selecciona la foto de tu mascota"))
    }

    // ─── Implementación de PublishContract.View ──────────────────────────

    override fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnPublish.isEnabled = !show
    }

    override fun showPublishSuccess() {
        Toast.makeText(requireContext(), "¡Mascota publicada con éxito!", Toast.LENGTH_LONG).show()
        clearForm()
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun showFieldError(field: String, message: String) {
        when (field) {
            "name" -> binding.tilName.error = message
            "type" -> binding.tilType.error = message
            "age" -> binding.tilAge.error = message
        }
    }

    override fun showPhotoPreview(uri: Uri) {
        // Al seleccionar, ocultamos la huella y mostramos la foto a pantalla completa en el card
        binding.layoutAddPhoto.visibility = View.GONE
        binding.ivPetPreview.alpha = 1.0f // Devolvemos la opacidad original
        binding.ivPetPreview.setImageURI(uri)
    }

    override fun clearForm() {
        binding.etName.text?.clear()
        binding.etAge.text?.clear()
        binding.etDescription.text?.clear()
        binding.etContact.text?.clear()
        binding.actvType.text?.clear()

        // Reset de la vista de imagen y vuelta a mostrar la huellita
        binding.ivPetPreview.setImageDrawable(null)
        binding.ivPetPreview.alpha = 0.2f
        binding.layoutAddPhoto.visibility = View.VISIBLE
        selectedPhotoUri = null

        // Limpiar errores visuales
        binding.tilName.error = null
        binding.tilType.error = null
        binding.tilAge.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        _binding = null
    }
}