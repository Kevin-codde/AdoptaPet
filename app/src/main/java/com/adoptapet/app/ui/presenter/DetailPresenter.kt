// app/src/main/java/com/adoptapet/app/ui/presenter/DetailPresenter.kt
// Presenter MVP para la pantalla de detalle de mascota

package com.adoptapet.app.ui.presenter

import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.ui.contracts.DetailContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Presenter para la pantalla de detalle de mascota.
 * Carga los datos de una mascota desde Room y expone
 * la información de contacto del responsable.
 */
class DetailPresenter(
    private var view: DetailContract.View?,
    private val repository: PetRepository
) : DetailContract.Presenter {

    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    /**
     * Carga los detalles de una mascota desde el caché local (Room).
     * Si no está en caché, muestra un error informativo.
     */
    override fun loadPetDetail(petId: String) {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val pet = repository.getPetById(petId)
                view?.showLoading(false)
                if (pet != null) {
                    view?.showPetDetail(pet)
                } else {
                    view?.showError("No se encontró la información de esta mascota")
                }
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError("Error al cargar detalle: ${e.message}")
            }
        }
    }

    /**
     * El usuario presionó "Solicitar adopción".
     * Muestra la información de contacto para que el usuario
     * se comunique directamente con el responsable.
     */
    override fun onRequestAdoption(pet: Pet) {
        view?.showContactInfo(pet.contactInfo)
    }

    override fun onDestroy() {
        presenterScope.cancel()
        view = null
    }
}
