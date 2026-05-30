// app/src/main/java/com/adoptapet/app/ui/contracts/HomeContract.kt
package com.adoptapet.app.ui.contracts

import com.adoptapet.app.data.model.Pet

/** Contrato MVP para la pantalla de inicio (lista de mascotas) */
interface HomeContract {
    interface View {
        fun showLoading(show: Boolean)
        fun showPets(pets: List<Pet>)
        fun showEmptyState(show: Boolean)
        fun showError(message: String)
        fun navigateToDetail(pet: Pet)
    }
    interface Presenter {
        fun loadPets()
        fun searchPets(query: String)
        fun onPetSelected(pet: Pet)
        fun onDestroy()
    }
}
