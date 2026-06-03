// app/src/main/java/com/adoptapet/app/ui/contracts/OtherContracts.kt
package com.adoptapet.app.ui.contracts

import android.net.Uri
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.model.User

interface DetailContract {
    interface View {
        fun showPetDetail(pet: Pet)
        fun showLoading(show: Boolean)
        fun showError(message: String)
        fun showContactInfo(contactInfo: String)
    }
    interface Presenter {
        fun loadPetDetail(petId: String)
        fun onRequestAdoption(pet: Pet)
        fun onDestroy()
    }
}

interface PublishContract {
    interface View {
        fun showLoading(show: Boolean)
        fun showPublishSuccess()
        fun showError(message: String)
        fun showFieldError(field: String, message: String)
        fun showPhotoPreview(uri: Uri)
        fun clearForm()

        // NUEVO: Pasa los datos de la mascota cargada desde el Presenter a los formularios de la UI
        fun showPetDataForEdit(pet: Pet)
    }
    interface Presenter {
        fun publishPet(
            name: String,
            type: String,
            sex: String,
            age: String,
            city: String,
            description: String,
            contactInfo: String,
            photoUri: Uri?
        )

        // NUEVO: Solicita cargar una mascota desde el repositorio usando su ID (String por el UUID)
        fun loadPetToEdit(petId: String)

        // NUEVO: Procesa la actualización de la mascota existente con los nuevos valores del formulario
        fun updatePet(
            id: String,
            name: String,
            type: String,
            sex: String,
            age: String,
            city: String,
            description: String,
            contactInfo: String,
            photoUri: Uri?
        )

        fun onDestroy()
    }
}

interface ProfileContract {
    interface View {
        fun showLoading(show: Boolean)
        fun showUserProfile(user: User)
        fun showMyPets(pets: List<Pet>)
        fun showEmptyPets(show: Boolean)
        fun showError(message: String)
        fun showDeleteSuccess()
        fun showDeleteConfirmation(pet: Pet)
        fun navigateToAuth()
    }
    interface Presenter {
        fun loadProfile()
        fun loadMyPets()
        fun deletePet(pet: Pet)
        fun logout()
        fun onDestroy()
    }
}