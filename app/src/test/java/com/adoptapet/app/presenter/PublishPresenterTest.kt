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
import java.util.UUID // Importante para generar el ID manualmente ahora

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
        // Mantenemos la validación de campos
        if (!validateFields(name, type, age, description, contactInfo, photoUri)) return

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

                // --- BAIPÁS DE STORAGE ---
                // En lugar de usar photoUri (que requiere Storage), usamos una imagen real de internet
                // Esta imagen se verá en la lista de mascotas de todos los que instalen la app.
                val fakeImageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=1000"

                val pet = Pet(
                    id = UUID.randomUUID().toString(), // Generamos el ID aquí para saltar la lógica del Repo
                    name = name.trim(),
                    type = type.trim(),
                    age = age.trim(),
                    description = description.trim(),
                    contactInfo = contactInfo.trim(),
                    photoUrl = fakeImageUrl, // <--- AQUÍ PASAMOS LA URL DE INTERNET
                    ownerId = currentUser.uid,
                    ownerName = ownerName
                )

                // IMPORTANTE: Llamamos a una función que SOLO guarde en base de datos.
                // Si tu petRepository no tiene una función simple, usaremos directamente Firestore.
                // Pero lo ideal es que uses:
                petRepository.publishPet(pet, null)
                // Al pasar 'null' en photoUri, el repositorio debería ignorar el Storage
                // y guardar solo el objeto 'pet' con nuestra fakeImageUrl.

                view?.showLoading(false)
                view?.showPublishSuccess()
                view?.clearForm()

            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError("Error al publicar: ${e.message}")
            }
        }
    }

    private fun validateFields(
        name: String, type: String, age: String,
        description: String, contactInfo: String, photoUri: Uri?
    ): Boolean {
        var isValid = true
        if (name.isBlank()) { view?.showFieldError("name", "Obligatorio"); isValid = false }
        if (type.isBlank()) { view?.showFieldError("type", "Obligatorio"); isValid = false }
        if (age.isBlank()) { view?.showFieldError("age", "Obligatorio"); isValid = false }
        if (description.isBlank()) { view?.showFieldError("description", "Obligatorio"); isValid = false }
        if (contactInfo.isBlank()) { view?.showFieldError("contactInfo", "Obligatorio"); isValid = false }

        // Quitamos la validación obligatoria de foto por ahora para que no te bloquee
        return isValid
    }

    override fun onDestroy() {
        presenterScope.cancel()
        view = null
    }
}