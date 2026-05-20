/**
 * Contrato MVP para la pantalla de inicio.
 * Define la comunicación entre la Vista y el Presenter.
 */
interface HomeContract {

    /**
     * Métodos que la Vista debe implementar.
     */
    interface View {

        /**
         * Muestra u oculta el indicador de carga.
         */
        fun showLoading(show: Boolean)

        /**
         * Muestra la lista de mascotas disponibles.
         */
        fun showPets(pets: List<Pet>)

        /**
         * Muestra u oculta el estado vacío.
         */
        fun showEmptyState(show: Boolean)

        /**
         * Muestra un mensaje de error.
         */
        fun showError(message: String)

        /**
         * Navega al detalle de la mascota seleccionada.
         */
        fun navigateToDetail(pet: Pet)
    }

    /**
     * Métodos que el Presenter debe implementar.
     */
    interface Presenter {

        /**
         * Carga todas las mascotas disponibles.
         */
        fun loadPets()

        /**
         * Filtra mascotas según el texto ingresado.
         */
        fun searchPets(query: String)

        /**
         * Procesa la selección de una mascota.
         */
        fun onPetSelected(pet: Pet)

        /**
         * Libera recursos al destruir el Presenter.
         */
        fun onDestroy()
    }
}