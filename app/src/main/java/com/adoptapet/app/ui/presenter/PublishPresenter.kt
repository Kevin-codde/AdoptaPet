/**
 * Presenter encargado de gestionar la lógica de publicación de mascotas.
 * Implementa la interfaz PublishContract.Presenter.
 */
class PublishPresenter(
    // Referencia a la Vista para actualizar la interfaz.
    private var view: PublishContract.View?,

    // Repositorio encargado de guardar las mascotas.
    private val petRepository: PetRepository,

    // Repositorio de autenticación para obtener el usuario actual.
    private val authRepository: AuthRepository = AuthRepository()
) : PublishContract.Presenter {

    // Scope de corrutinas para ejecutar tareas asíncronas.
    private val presenterScope =
        CoroutineScope(Dispatchers.Main + Job())

    /**
     * Valida los datos y publica una nueva mascota.
     */
    override fun publishPet(
        name: String,
        type: String,
        age: String,
        description: String,
        contactInfo: String,
        photoUri: Uri?
    ) {
        // Valida los campos del formulario.
        if (!validateFields(name, type, age, description, contactInfo)) {
            return
        }

        // Verifica que se haya seleccionado una foto.
        if (photoUri == null) {
            view?.showError("Por favor selecciona una foto de la mascota")
            return
        }

        // Obtiene el usuario autenticado.
        val currentUser = authRepository.getCurrentFirebaseUser()

        // Verifica que exista una sesión activa.
        if (currentUser == null) {
            view?.showError("Debes iniciar sesión para publicar")
            return
        }

        // Muestra el indicador de carga.
        view?.showLoading(true)

        // Ejecuta la publicación de forma asíncrona.
        presenterScope.launch {
            try {
                // Obtiene el perfil del usuario.
                val userProfile =
                    authRepository.getCurrentUserProfile()

                // Define el nombre del propietario.
                val ownerName =
                    userProfile?.name
                        ?: currentUser.email
                        ?: "Usuario"

                // Crea el objeto de la mascota.
                val pet = Pet(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    type = type.trim(),
                    age = age.trim(),
                    description = description.trim(),
                    contactInfo = contactInfo.trim(),
                    ownerId = currentUser.uid,
                    ownerName = ownerName
                )

                // Guarda la mascota y sube la foto.
                petRepository.publishPet(pet, photoUri)

                // Oculta el indicador de carga.
                view?.showLoading(false)

                // Notifica el éxito de la publicación.
                view?.showPublishSuccess()

                // Limpia el formulario.
                view?.clearForm()

            } catch (e: Exception) {
                // Oculta el indicador de carga.
                view?.showLoading(false)

                // Muestra el mensaje de error.
                view?.showError("Error al subir: ${e.message}")
            }
        }
    }

    /**
     * Valida los campos obligatorios del formulario.
     */
    private fun validateFields(
        name: String,
        type: String,
        age: String,
        d: String,
        c: String
    ): Boolean {
        var isValid = true

        // Valida el nombre de la mascota.
        if (name.isBlank()) {
            view?.showFieldError(
                "name",
                "El nombre es obligatorio"
            )
            isValid = false
        }

        // Valida el tipo de mascota.
        if (type.isBlank()) {
            view?.showFieldError(
                "type",
                "Selecciona un tipo"
            )
            isValid = false
        }

        // Valida la edad de la mascota.
        if (age.isBlank()) {
            view?.showFieldError(
                "age",
                "La edad es obligatoria"
            )
            isValid = false
        }

        // Retorna si todos los campos son válidos.
        return isValid
    }

    /**
     * Cancela las corrutinas y libera la referencia de la Vista.
     */
    override fun onDestroy() {
        // Cancela las tareas pendientes.
        presenterScope.cancel()

        // Libera la referencia de la Vista.
        view = null
    }
}