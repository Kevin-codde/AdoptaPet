/**
 * Fragment encargado de mostrar el detalle de una mascota.
 * Implementa la interfaz DetailContract.View.
 */
class DetailFragment : Fragment(), DetailContract.View {

    // Referencia al binding del layout.
    private var _binding: FragmentDetailBinding? = null

    // Acceso seguro al binding.
    private val binding get() = _binding!!

    // Presenter que contiene la lógica de negocio.
    private lateinit var presenter: DetailPresenter

    // Argumentos recibidos mediante Safe Args.
    private val args: DetailFragmentArgs by navArgs()

    /**
     * Infla el layout del fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Inicializa el Presenter y carga el detalle de la mascota.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtiene la instancia de la base de datos.
        val db = AppDatabase.getDatabase(requireContext())

        // Inicializa el Presenter con el repositorio.
        presenter = DetailPresenter(
            this,
            PetRepository(db.petDao())
        )

        // Carga la mascota usando el ID recibido.
        presenter.loadPetDetail(args.petId)
    }

    /**
     * Muestra u oculta el indicador de carga.
     */
    override fun showLoading(show: Boolean) {
        // No se implementa porque este layout no tiene ProgressBar.
    }

    /**
     * Muestra la información detallada de la mascota.
     */
    override fun showPetDetail(pet: Pet) {
        with(binding) {

            // Muestra el nombre de la mascota.
            tvDetailName.text = pet.name

            // Muestra el tipo con la primera letra en mayúscula.
            tvDetailType.text = pet.type.replaceFirstChar { it.uppercase() }

            // Muestra la edad de la mascota.
            tvDetailAge.text = pet.age

            // Muestra la descripción.
            tvDetailDescription.text = pet.description

            // Muestra la información de contacto.
            tvDetailContact.text = pet.contactInfo

            // Muestra el nombre del propietario.
            tvDetailOwner.text = "Publicado por: ${pet.ownerName}"

            // Verifica si existe una foto.
            if (pet.photoUrl.isNotEmpty()) {

                // Carga la imagen con Coil.
                ivDetailPhoto.load(pet.photoUrl) {
                    crossfade(true)
                    placeholder(com.adoptapet.app.R.drawable.ic_paw_placeholder)
                    error(com.adoptapet.app.R.drawable.ic_paw_placeholder)
                }
            } else {

                // Muestra imagen por defecto.
                ivDetailPhoto.setImageResource(
                    com.adoptapet.app.R.drawable.ic_paw_placeholder
                )
            }

            // Solicita la adopción al presionar el botón.
            btnRequestAdoption.setOnClickListener {
                presenter.onRequestAdoption(pet)
            }
        }
    }

    /**
     * Muestra un mensaje de error y regresa a la pantalla anterior.
     */
    override fun showError(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()

        findNavController().popBackStack()
    }

    /**
     * Muestra la información de contacto en un diálogo.
     */
    override fun showContactInfo(contactInfo: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Información de contacto")
            .setMessage(contactInfo)
            .setPositiveButton("Cerrar", null)
            .show()
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