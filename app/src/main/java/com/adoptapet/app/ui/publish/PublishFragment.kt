/**
 * Fragment encargado de publicar nuevas mascotas.
 * Implementa la interfaz PublishContract.View.
 */
class PublishFragment : Fragment(), PublishContract.View {

    // Referencia al binding del layout.
    private var _binding: FragmentPublishBinding? = null

    // Acceso seguro al binding.
    private val binding get() = _binding!!

    // Presenter que contiene la lógica de publicación.
    private lateinit var presenter: PublishPresenter

    // URI de la foto seleccionada por el usuario.
    private var selectedPhotoUri: Uri? = null

    /**
     * Launcher que abre la galería y recibe la imagen seleccionada.
     */
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        // Verifica que la selección fue exitosa.
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->

                // Guarda la URI de la imagen.
                selectedPhotoUri = uri

                // Muestra la vista previa.
                showPhotoPreview(uri)
            }
        }
    }

    /**
     * Infla el layout del fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublishBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    /**
     * Inicializa el Presenter y configura la interfaz.
     */
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Obtiene la instancia de la base de datos.
        val db = AppDatabase.getDatabase(requireContext())

        // Inicializa el Presenter con el repositorio.
        presenter = PublishPresenter(
            this,
            PetRepository(db.petDao())
        )

        // Configura el listado de tipos de mascota.
        setupPetTypeDropdown()

        // Configura los eventos de la interfaz.
        setupClickListeners()
    }

    /**
     * Configura las opciones del campo tipo de mascota.
     */
    private fun setupPetTypeDropdown() {
        val types = listOf(
            "Perro",
            "Gato",
            "Conejo",
            "Ave",
            "Otro"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            types
        )

        binding.actvType.setAdapter(adapter)
    }

    /**
     * Configura los eventos de clic de la interfaz.
     */
    private fun setupClickListeners() {

        // Abre la galería al tocar la tarjeta de la foto.
        binding.cardPhoto.setOnClickListener {
            openGallery()
        }

        // Envía los datos al Presenter al presionar Publicar.
        binding.btnPublish.setOnClickListener {

            // Limpia errores anteriores.
            binding.tilName.error = null
            binding.tilType.error = null
            binding.tilAge.error = null

            // Solicita la publicación de la mascota.
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

    /**
     * Abre la galería para seleccionar una imagen.
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        pickImageLauncher.launch(
            Intent.createChooser(
                intent,
                "Selecciona la foto de tu mascota"
            )
        )
    }

    /**
     * Muestra u oculta el indicador de carga.
     */
    override fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility =
            if (show) View.VISIBLE else View.GONE

        binding.btnPublish.isEnabled = !show
    }

    /**
     * Notifica que la publicación fue exitosa.
     */
    override fun showPublishSuccess() {
        Toast.makeText(
            requireContext(),
            "¡Mascota publicada con éxito!",
            Toast.LENGTH_LONG
        ).show()

        clearForm()
    }

    /**
     * Muestra un mensaje de error.
     */
    override fun showError(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Muestra un error en un campo específico.
     */
    override fun showFieldError(
        field: String,
        message: String
    ) {
        when (field) {
            "name" -> binding.tilName.error = message
            "type" -> binding.tilType.error = message
            "age" -> binding.tilAge.error = message
        }
    }

    /**
     * Muestra la vista previa de la imagen seleccionada.
     */
    override fun showPhotoPreview(uri: Uri) {

        // Oculta el ícono de agregar foto.
        binding.layoutAddPhoto.visibility = View.GONE

        // Restablece la opacidad de la imagen.
        binding.ivPetPreview.alpha = 1.0f

        // Carga la imagen seleccionada.
        binding.ivPetPreview.setImageURI(uri)
    }

    /**
     * Limpia todos los campos del formulario.
     */
    override fun clearForm() {

        // Limpia los campos de texto.
        binding.etName.text?.clear()
        binding.etAge.text?.clear()
        binding.etDescription.text?.clear()
        binding.etContact.text?.clear()
        binding.actvType.text?.clear()

        // Limpia la imagen seleccionada.
        binding.ivPetPreview.setImageDrawable(null)

        // Reduce la opacidad de la imagen.
        binding.ivPetPreview.alpha = 0.2f

        // Vuelve a mostrar el ícono de agregar foto.
        binding.layoutAddPhoto.visibility = View.VISIBLE

        // Reinicia la URI seleccionada.
        selectedPhotoUri = null

        // Limpia los errores visuales.
        binding.tilName.error = null
        binding.tilType.error = null
        binding.tilAge.error = null
    }

    /**
     * Libera recursos al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()

        // Libera la referencia del Presenter.
        presenter.onDestroy()

        // Libera el binding para evitar fugas de memoria.
        _binding = null
    }
}