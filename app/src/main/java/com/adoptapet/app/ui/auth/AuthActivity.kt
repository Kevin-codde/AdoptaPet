/**
 * Actividad encargada de gestionar el inicio de sesión y registro de usuarios.
 * Implementa la interfaz AuthContract.View para comunicarse con el Presenter.
 */
class AuthActivity : AppCompatActivity(), AuthContract.View {

    // Referencia a los componentes visuales mediante View Binding.
    private lateinit var binding: ActivityAuthBinding

    // Presenter que contiene la lógica de autenticación.
    private lateinit var presenter: AuthPresenter

    // Indica si la pantalla está en modo registro o inicio de sesión.
    private var isRegisterMode = false

    /**
     * Inicializa la actividad y verifica si ya existe una sesión activa.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crea una instancia del repositorio de autenticación.
        val authRepo = AuthRepository()

        // Verifica si el usuario ya inició sesión.
        if (authRepo.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        // Infla el layout y lo muestra en pantalla.
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa el Presenter.
        presenter = AuthPresenter(this)

        // Configura los eventos de los botones.
        setupClickListeners()
    }

    /**
     * Configura las acciones de los botones principales.
     */
    private fun setupClickListeners() {

        // Ejecuta login o registro según el modo actual.
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

        // Cambia entre modo login y registro.
        binding.btnToggle.setOnClickListener {
            toggleMode()
        }
    }

    /**
     * Alterna entre el formulario de registro e inicio de sesión.
     */
    private fun toggleMode() {
        isRegisterMode = !isRegisterMode

        if (isRegisterMode) {

            // Muestra el campo de nombre.
            binding.tilName.visibility = View.VISIBLE
            binding.spaceName.visibility = View.VISIBLE

            // Cambia los textos al modo registro.
            binding.btnPrimary.text = "Registrarse"
            binding.btnToggle.text = "¿Ya tienes cuenta? Inicia sesión"
        } else {

            // Oculta el campo de nombre.
            binding.tilName.visibility = View.GONE
            binding.spaceName.visibility = View.GONE

            // Cambia los textos al modo login.
            binding.btnPrimary.text = "Iniciar Sesión"
            binding.btnToggle.text = "¿No tienes cuenta? Regístrate"
        }
    }

    /**
     * Muestra u oculta el indicador de carga.
     */
    override fun showLoading(show: Boolean) {
        binding.progressBar.visibility =
            if (show) View.VISIBLE else View.GONE

        // Deshabilita los botones mientras se procesa.
        binding.btnPrimary.isEnabled = !show
        binding.btnToggle.isEnabled = !show
    }

    /**
     * Se ejecuta cuando el inicio de sesión es exitoso.
     */
    override fun showLoginSuccess(user: User) {
        navigateToMain()
    }

    /**
     * Se ejecuta cuando el registro es exitoso.
     */
    override fun showRegisterSuccess(user: User) {
        navigateToMain()
    }

    /**
     * Muestra un mensaje de error al usuario.
     */
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Muestra un error en el campo de correo.
     */
    override fun showEmailError(message: String) {
        binding.tilEmail.error = message
    }

    /**
     * Muestra un error en el campo de contraseña.
     */
    override fun showPasswordError(message: String) {
        binding.tilPassword.error = message
    }

    /**
     * Muestra un error en el campo de nombre.
     */
    override fun showNameError(message: String) {
        binding.tilName.error = message
    }

    /**
     * Navega a la pantalla principal de la aplicación.
     */
    override fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)

        // Limpia el historial de actividades.
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    /**
     * Libera la referencia del Presenter al destruir la actividad.
     */
    override fun onDestroy() {
        super.onDestroy()

        // Evita fugas de memoria.
        if (::presenter.isInitialized) {
            presenter.onDestroy()
        }
    }
}