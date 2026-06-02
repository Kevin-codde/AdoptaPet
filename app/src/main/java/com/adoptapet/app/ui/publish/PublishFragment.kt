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
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.databinding.FragmentPublishBinding
import com.adoptapet.app.ui.contracts.PublishContract
import com.adoptapet.app.ui.presenter.PublishPresenter

/**
 * Fragment de publicación actualizado para AdoptaPet.
 * Gestiona la selección de imágenes locales, el sexo y la publicación hacia Firebase Storage.
 */
class PublishFragment : Fragment(), PublishContract.View {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!
    private lateinit var presenter: PublishPresenter
    private var selectedPhotoUri: Uri? = null

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

        val db = AppDatabase.getDatabase(requireContext())
        presenter = PublishPresenter(
            view = this,
            petRepository = PetRepository(db.petDao()),
            authRepository = AuthRepository()
        )

        setupPetTypeDropdown()
        setupSexDropdown()
        setupClickListeners()
    }

    private fun setupPetTypeDropdown() {
        val types = listOf("Perro", "Gato", "Conejo", "Ave", "Otro")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.actvType.setAdapter(adapter)
    }

    private fun setupSexDropdown() {
        val optionsSex = listOf("Macho", "Hembra", "No aplica")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, optionsSex)
        binding.actvSex.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.cardPhoto.setOnClickListener {
            openGallery()
        }

        binding.btnPublish.setOnClickListener {
            // Limpiamos los errores visuales de los 7 contenedores antes de validar
            binding.tilName.error = null
            binding.tilType.error = null
            binding.tilSex.error = null
            binding.tilCity.error = null
            binding.tilAge.error = null
            binding.tilDescription.error = null
            binding.tilContact.error = null

            // Enviamos todos los datos recopilados al presentador
            presenter.publishPet(
                name = binding.etName.text.toString(),
                type = binding.actvType.text.toString(),
                sex = binding.actvSex.text.toString(),
                age = binding.etAge.text.toString(),
                city = binding.etCity.text.toString(),
                description = binding.etDescription.text.toString(),
                contactInfo = binding.etContact.text.toString(),
                photoUri = selectedPhotoUri
            )
        }
    }

    private fun openGallery() {
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
        val errorValue = if (message.isEmpty()) null else message

        // Controla la aparición y desaparición de los errores en toda la UI
        when (field) {
            "name" -> binding.tilName.error = errorValue
            "type" -> binding.tilType.error = errorValue
            "sex" -> binding.tilSex.error = errorValue
            "city" -> binding.tilCity.error = errorValue
            "age" -> binding.tilAge.error = errorValue
            "description" -> binding.tilDescription.error = errorValue
            "contactInfo" -> binding.tilContact.error = errorValue
        }
    }

    override fun showPhotoPreview(uri: Uri) {
        binding.layoutAddPhoto.visibility = View.GONE
        binding.ivPetPreview.alpha = 1.0f
        binding.ivPetPreview.setImageURI(uri)
    }

    override fun clearForm() {
        binding.etName.text?.clear()
        binding.etCity.text?.clear()
        binding.etAge.text?.clear()
        binding.etDescription.text?.clear()
        binding.etContact.text?.clear()
        binding.actvType.text?.clear()
        binding.actvSex.text?.clear()

        binding.ivPetPreview.setImageDrawable(null)
        binding.ivPetPreview.alpha = 0.5f
        binding.layoutAddPhoto.visibility = View.VISIBLE
        selectedPhotoUri = null

        binding.tilName.error = null
        binding.tilType.error = null
        binding.tilSex.error = null
        binding.tilCity.error = null
        binding.tilAge.error = null
        binding.tilDescription.error = null
        binding.tilContact.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        _binding = null
    }
}