// app/src/main/java/com/adoptapet/app/data/repository/PetRepository.kt
package com.adoptapet.app.data.repository

import android.net.Uri
import com.adoptapet.app.data.local.PetDao
import com.adoptapet.app.data.model.Pet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PetRepository(
    private val petDao: PetDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    companion object {
        private const val COLLECTION_PETS = "pets"
        private const val STORAGE_PETS_PATH = "pet_images"
    }

    fun getAllPetsLocal(): Flow<List<Pet>> = petDao.getAllPets()
    fun searchPetsLocal(query: String): Flow<List<Pet>> = petDao.searchPets(query)
    fun getPetsByOwnerLocal(ownerId: String): Flow<List<Pet>> = petDao.getPetsByOwner(ownerId)

    suspend fun syncPetsFromFirestore() {
        val snapshot = firestore.collection(COLLECTION_PETS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        val pets = snapshot.documents.mapNotNull { doc ->
            try {
                doc.data?.let { Pet.fromFirestoreMap(it) }
            } catch (e: Exception) { null }
        }

        petDao.deleteAll()
        petDao.insertAll(pets)
    }

    /**
     * Publica una mascota usando Firebase Storage para la imagen.
     */
    suspend fun publishPet(pet: Pet, photoUri: Uri?): Pet {
        // 1. Usar el ID generado en el Presenter o generar uno nuevo aquí
        val petId = if (pet.id.isBlank()) UUID.randomUUID().toString() else pet.id

        // 2. Subir imagen a Firebase Storage
        val photoUrl = if (photoUri != null) {
            uploadPetPhoto(petId, photoUri)
        } else {
            // Imagen genérica por si algo falla con el Uri
            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=1000"
        }

        // 3. Construir objeto final con la URL real de Firebase
        val finalPet = pet.copy(id = petId, photoUrl = photoUrl)

        // 4. Guardar en Firestore (Remoto)
        firestore.collection(COLLECTION_PETS)
            .document(petId)
            .set(finalPet.toFirestoreMap())
            .await()

        // 5. Guardar en Room (Local)
        petDao.insert(finalPet)

        return finalPet
    }

    private suspend fun uploadPetPhoto(petId: String, uri: Uri): String {
        val storageRef = storage.reference
            .child("$STORAGE_PETS_PATH/$petId.jpg")

        // Subir archivo y esperar
        storageRef.putFile(uri).await()

        // Retornar la URL de descarga pública de Firebase
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun deletePet(petId: String) {
        firestore.collection(COLLECTION_PETS).document(petId).delete().await()
        petDao.deleteById(petId)
    }

    suspend fun getPetById(petId: String): Pet? = petDao.getPetById(petId)
}