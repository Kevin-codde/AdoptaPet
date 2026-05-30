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
    }
    interface Presenter {
        fun publishPet(
            name: String,
            type: String,
            age: String,
            city: String, // <-- Parámetro agregado al contrato
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