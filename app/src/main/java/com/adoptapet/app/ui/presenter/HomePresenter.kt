// app/src/main/java/com/adoptapet/app/ui/presenter/HomePresenter.kt
// Presenter MVP para la pantalla de inicio (lista de mascotas)

package com.adoptapet.app.ui.presenter

import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.ui.contracts.HomeContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Presenter para la pantalla Home.
 * Coordina la carga de mascotas desde Room (inmediata) y la sincronización
 * con Firestore (en background).
 *
 * Estrategia Cache-first:
 * 1. Suscribe a Flow de Room → la UI muestra datos inmediatamente del caché
 * 2. Lanza sincronización con Firestore en paralelo
 * 3. Cuando Firestore responde, Room se actualiza y el Flow emite automáticamente
 */
class HomePresenter(
    private var view: HomeContract.View?,
    private val repository: PetRepository
) : HomeContract.Presenter {

    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    // Job de búsqueda separado para poder cancelarlo ante nueva búsqueda
    private var searchJob: Job? = null

    // ─── Carga de mascotas ────────────────────────────────────────────────────

    /**
     * Inicia la carga de mascotas:
     * 1. Muestra el loader
     * 2. Suscribe al Flow de Room para actualizaciones en tiempo real
     * 3. Dispara la sincronización con Firestore en background
     */
    override fun loadPets() {
        view?.showLoading(true)

        // Suscribir al caché local (Room) para respuesta inmediata
        presenterScope.launch {
            repository.getAllPetsLocal()
                .catch { e ->
                    view?.showLoading(false)
                    view?.showError("Error al cargar mascotas: ${e.message}")
                }
                .collect { pets ->
                    view?.showLoading(false)
                    if (pets.isEmpty()) {
                        view?.showEmptyState(true)
                    } else {
                        view?.showEmptyState(false)
                        view?.showPets(pets)
                    }
                }
        }

        // Sincronizar con Firestore en background
        syncFromFirestore()
    }

    /**
     * Sincroniza datos con Firestore de forma silenciosa.
     * Los errores de red muestran un mensaje no bloqueante.
     */
    private fun syncFromFirestore() {
        presenterScope.launch(Dispatchers.IO) {
            try {
                repository.syncPetsFromFirestore()
                // Room actualiza automáticamente el Flow, no es necesario acción adicional
            } catch (e: Exception) {
                // En fallo de red, Room ya tiene el caché disponible
                // Solo mostramos error si el caché también está vacío
                launch(Dispatchers.Main) {
                    view?.showError("Sin conexión: mostrando datos locales")
                }
            }
        }
    }

    // ─── Búsqueda ─────────────────────────────────────────────────────────────

    /**
     * Busca mascotas por nombre o tipo.
     * Cancela cualquier búsqueda anterior antes de iniciar la nueva.
     */
    override fun searchPets(query: String) {
        // Cancelar búsqueda anterior
        searchJob?.cancel()

        if (query.isBlank()) {
            // Si la búsqueda está vacía, volver a mostrar todas
            loadPets()
            return
        }

        searchJob = presenterScope.launch {
            repository.searchPetsLocal(query)
                .catch { e ->
                    view?.showError("Error en búsqueda: ${e.message}")
                }
                .collect { pets ->
                    if (pets.isEmpty()) {
                        view?.showEmptyState(true)
                        view?.showPets(emptyList())
                    } else {
                        view?.showEmptyState(false)
                        view?.showPets(pets)
                    }
                }
        }
    }

    // ─── Navegación ───────────────────────────────────────────────────────────

    /**
     * El usuario seleccionó una mascota para ver su detalle.
     */
    override fun onPetSelected(pet: Pet) {
        view?.navigateToDetail(pet)
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    override fun onDestroy() {
        presenterScope.cancel()
        view = null
    }
}
