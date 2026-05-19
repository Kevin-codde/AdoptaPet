// app/src/main/java/com/adoptapet/app/ui/presenter/PublishPresenter.kt
package com.adoptapet.app.ui.presenter

import android.net.Uri
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.ui.contracts.PublishContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID

class PublishPresenter(
    private var view: PublishContract.View?,
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository = AuthRepository()
) : PublishContract.Presenter {

    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun publishPet(
        name: String,
        type: String,
        age: String,
        description: String,
        contactInfo: String,
        photoUri: Uri?
    ) {
        if (!validateFields(name, type, age, description, contactInfo)) return

        if (photoUri == null) {
            view?.showError("Por favor selecciona una foto de la mascota")
            return
        }

        val currentUser = authRepository.getCurrentFirebaseUser()
        if (currentUser == null) {
            view?.showError("Debes iniciar sesión para publicar")
            return
        }

        view?.showLoading(true)

        presenterScope.launch {
            try {
                val userProfile = authRepository.getCurrentUserProfile()
                val ownerName = userProfile?.name ?: currentUser.email ?: "Usuario"

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

                // El repositorio se encarga de subir el Uri a Storage y luego a Firestore
                petRepository.publishPet(pet, photoUri)

                view?.showLoading(false)
                view?.showPublishSuccess()
                view?.clearForm()
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError("Error al subir: ${e.message}")
            }
        }
    }

    private fun validateFields(name: String, type: String, age: String, d: String, c: String): Boolean {
        var isValid = true
        if (name.isBlank()) { view?.showFieldError("name", "El nombre es obligatorio"); isValid = false }
        if (type.isBlank()) { view?.showFieldError("type", "Selecciona un tipo"); isValid = false }
        if (age.isBlank()) { view?.showFieldError("age", "La edad es obligatoria"); isValid = false }
        return isValid
    }

    override fun onDestroy() {
        presenterScope.cancel()
        view = null
    }
}