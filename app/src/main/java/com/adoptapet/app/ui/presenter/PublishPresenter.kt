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
        sex: String,
        age: String,
        city: String,
        description: String,
        contactInfo: String,
        photoUri: Uri?
    ) {
        // Validación estricta de todos los campos obligatorios
        if (!validateFields(name, type, sex, age, city, description, contactInfo)) return

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
                    sex = sex.trim(),
                    age = age.trim(),
                    city = city.trim(),
                    description = description.trim(),
                    contactInfo = contactInfo.trim(),
                    ownerId = currentUser.uid,
                    ownerName = ownerName
                )

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

    private fun validateFields(name: String, type: String, sex: String, age: String, city: String, description: String, contactInfo: String): Boolean {
        var isValid = true

        // Limpiamos errores previos de los 7 campos antes de evaluar de nuevo
        view?.showFieldError("name", "")
        view?.showFieldError("type", "")
        view?.showFieldError("sex", "")
        view?.showFieldError("city", "")
        view?.showFieldError("age", "")
        view?.showFieldError("description", "")
        view?.showFieldError("contactInfo", "")

        // Validaciones obligatorias una por una
        if (name.isBlank()) { view?.showFieldError("name", "El nombre es obligatorio"); isValid = false }
        if (type.isBlank()) { view?.showFieldError("type", "Selecciona un tipo"); isValid = false }
        if (sex.isBlank()) { view?.showFieldError("sex", "Selecciona el sexo"); isValid = false }
        if (city.isBlank()) { view?.showFieldError("city", "La ciudad es obligatoria"); isValid = false }
        if (age.isBlank()) { view?.showFieldError("age", "La edad es obligatoria"); isValid = false }
        if (description.isBlank()) { view?.showFieldError("description", "La descripción es obligatoria"); isValid = false }
        if (contactInfo.isBlank()) { view?.showFieldError("contactInfo", "El contacto es obligatorio"); isValid = false }

        return isValid
    }

    override fun onDestroy() {
        presenterScope.cancel()
        view = null
    }
}