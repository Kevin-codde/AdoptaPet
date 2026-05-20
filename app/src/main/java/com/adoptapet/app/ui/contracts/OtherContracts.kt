/**
 * Contrato MVP para la pantalla de detalle de mascota.
 * Define la comunicación entre la Vista y el Presenter.
 */
interface DetailContract {

    /**
     * Métodos que la Vista debe implementar.
     */
    interface View {

        /**
         * Muestra la información completa de la mascota.
         */
        fun showPetDetail(pet: Pet)

        /**
         * Muestra u oculta el indicador de carga.
         */
        fun showLoading(show: Boolean)

        /**
         * Muestra un mensaje de error.
         */
        fun showError(message: String)

        /**
         * Muestra la información de contacto del propietario.
         */
        fun showContactInfo(contactInfo: String)
    }

    /**
     * Métodos que el Presenter debe implementar.
     */
    interface Presenter {

        /**
         * Carga los datos de la mascota seleccionada.
         */
        fun loadPetDetail(petId: String)

        /**
         * Procesa la solicitud de adopción.
         */
        fun onRequestAdoption(pet: Pet)

        /**
         * Libera recursos al destruir el Presenter.
         */
        fun onDestroy()
    }
}

/**
 * Contrato MVP para la pantalla de publicación de mascotas.
 */
interface PublishContract {

    /**
     * Métodos que la Vista debe implementar.
     */
    interface View {

        /**
         * Muestra u oculta el indicador de carga.
         */
        fun showLoading(show: Boolean)

        /**
         * Notifica que la publicación fue exitosa.
         */
        fun showPublishSuccess()

        /**
         * Muestra un mensaje de error.
         */
        fun showError(message: String)

        /**
         * Muestra un error en un campo específico.
         */
        fun showFieldError(field: String, message: String)

        /**
         * Muestra la vista previa de la foto seleccionada.
         */
        fun showPhotoPreview(uri: Uri)

        /**
         * Limpia todos los campos del formulario.
         */
        fun clearForm()
    }

    /**
     * Métodos que el Presenter debe implementar.
     */
    interface Presenter {

        /**
         * Publica una nueva mascota en el sistema.
         */
        fun publishPet(
            name: String,
            type: String,
            age: String,
            description: String,
            contactInfo: String,
            photoUri: Uri?
        )

        /**
         * Libera recursos al destruir el Presenter.
         */
        fun onDestroy()
    }
}

/**
 * Contrato MVP para la pantalla de perfil del usuario.
 */
interface ProfileContract {

    /**
     * Métodos que la Vista debe implementar.
     */
    interface View {

        /**
         * Muestra u oculta el indicador de carga.
         */
        fun showLoading(show: Boolean)

        /**
         * Muestra la información del usuario autenticado.
         */
        fun showUserProfile(user: User)

        /**
         * Muestra las mascotas publicadas por el usuario.
         */
        fun showMyPets(pets: List<Pet>)

        /**
         * Muestra u oculta el estado vacío de mascotas.
         */
        fun showEmptyPets(show: Boolean)

        /**
         * Muestra un mensaje de error.
         */
        fun showError(message: String)

        /**
         * Notifica que la mascota fue eliminada.
         */
        fun showDeleteSuccess()

        /**
         * Solicita confirmación antes de eliminar una mascota.
         */
        fun showDeleteConfirmation(pet: Pet)

        /**
         * Navega a la pantalla de autenticación.
         */
        fun navigateToAuth()
    }

    /**
     * Métodos que el Presenter debe implementar.
     */
    interface Presenter {

        /**
         * Carga la información del perfil.
         */
        fun loadProfile()

        /**
         * Carga las mascotas del usuario.
         */
        fun loadMyPets()

        /**
         * Elimina la mascota seleccionada.
         */
        fun deletePet(pet: Pet)

        /**
         * Cierra la sesión del usuario.
         */
        fun logout()

        /**
         * Libera recursos al destruir el Presenter.
         */
        fun onDestroy()
    }
}