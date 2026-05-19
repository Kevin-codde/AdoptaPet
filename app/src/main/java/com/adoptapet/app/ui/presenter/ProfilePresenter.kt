// app/src/main/java/com/adoptapet/app/ui/presenter/ProfilePresenter.kt
// Presenter MVP para la pantalla de perfil del usuario

package com.adoptapet.app.ui.presenter

import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.repository.AuthRepository
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.ui.contracts.ProfileContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Presenter para la pantalla de Perfil.
 * Carga el perfil del usuario autenticado y sus mascotas publicadas.
 * Permite eliminar publicaciones propias y cerrar sesión.
 */
class ProfilePresenter(
    private var view: ProfileContract.View?,
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository = AuthRepository()
) : ProfileContract.Presenter {

    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    /**
     * Carga el perfil del usuario autenticado desde Firestore.
     */
    override fun loadProfile() {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val user = authRepository.getCurrentUserProfile()
                view?.showLoading(false)
                if (user != null) {
                    view?.showUserProfile(user)
                } else {
                    // Usuario autenticado pero sin perfil Firestore
                    val firebaseUser = authRepository.getCurrentFirebaseUser()
                    if (firebaseUser != null) {
                        view?.showUserProfile(
                            com.adoptapet.app.data.model.User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                name = firebaseUser.email ?: "Usuario"
                            )
                        )
                    } else {
                        view?.navigateToAuth()
                    }
                }
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError("Error al cargar perfil: ${e.message}")
            }
        }
    }

    /**
     * Carga las mascotas publicadas por el usuario actual desde Room (Flow reactivo).
     */
    override fun loadMyPets() {
        val uid = authRepository.getCurrentFirebaseUser()?.uid ?: return

        presenterScope.launch {
            petRepository.getPetsByOwnerLocal(uid)
                .catch { e ->
                    view?.showError("Error al cargar tus mascotas: ${e.message}")
                }
                .collect { pets ->
                    if (pets.isEmpty()) {
                        view?.showEmptyPets(true)
                        view?.showMyPets(emptyList())
                    } else {
                        view?.showEmptyPets(false)
                        view?.showMyPets(pets)
                    }
                }
        }
    }

    /**
     * Solicita confirmación antes de eliminar una mascota.
     * La View muestra un diálogo de confirmación y luego llama a deletePet.
     */
    override fun deletePet(pet: Pet) {
        view?.showDeleteConfirmation(pet)
    }

    /**
     * Elimina definitivamente una mascota de Firestore y Room.
     * Llamado desde la View tras la confirmación del usuario.
     */
    fun confirmDeletePet(pet: Pet) {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                petRepository.deletePet(pet.id)
                view?.showLoading(false)
                view?.showDeleteSuccess()
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError("Error al eliminar: ${e.message}")
            }
        }
    }

    /**
     * Cierra la sesión del usuario y navega a la pantalla de autenticación.
     */
    override fun logout() {
        authRepository.logout()
        view?.navigateToAuth()
    }

    override fun onDestroy() {
        presenterScope.cancel()
        view = null
    }
}
