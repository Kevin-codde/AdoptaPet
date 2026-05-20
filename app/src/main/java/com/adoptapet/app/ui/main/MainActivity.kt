/**
 * Actividad principal de la aplicación.
 * Contiene la navegación entre los fragments mediante Navigation Component.
 */
class MainActivity : AppCompatActivity() {

    // Referencia al binding del layout principal.
    private var _binding: ActivityMainBinding? = null

    // Acceso seguro al binding.
    private val binding get() = _binding!!

    // Controlador de navegación.
    private lateinit var navController: NavController

    /**
     * Inicializa la actividad y verifica si existe una sesión activa.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crea una instancia del repositorio de autenticación.
        val authRepo = AuthRepository()

        // Verifica si el usuario ha iniciado sesión.
        if (!authRepo.isUserLoggedIn()) {
            navigateToAuth()
            return
        }

        try {
            // Infla el layout principal.
            _binding = ActivityMainBinding.inflate(layoutInflater)

            // Muestra la interfaz en pantalla.
            setContentView(binding.root)

            // Configura la navegación.
            setupNavigation()
        } catch (e: Exception) {
            // Redirige al login si ocurre un error al cargar.
            navigateToAuth()
        }
    }

    /**
     * Configura el Navigation Component y el BottomNavigationView.
     */
    private fun setupNavigation() {
        try {
            // Obtiene el NavHostFragment definido en el layout.
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment

            // Obtiene el NavController.
            navController = navHostFragment.navController

            // Vincula el menú inferior con la navegación.
            binding.bottomNavigation
                .setupWithNavController(navController)
        } catch (e: Exception) {
            // Imprime el error en la consola.
            e.printStackTrace()
        }
    }

    /**
     * Navega a la pantalla de autenticación.
     */
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)

        // Limpia el historial de actividades.
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    /**
     * Libera el binding al destruir la actividad.
     */
    override fun onDestroy() {
        super.onDestroy()

        // Evita fugas de memoria.
        _binding = null
    }
}