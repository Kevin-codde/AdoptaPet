// app/src/test/java/com/adoptapet/app/model/PetModelTest.kt
// Pruebas unitarias para el modelo Pet (serialización/deserialización Firestore)

package com.adoptapet.app.model

import com.adoptapet.app.data.model.Pet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas para la conversión entre Pet y Map<String, Any> (Firestore).
 * No requieren Android ni Firebase — ejecutan en JVM pura.
 */
class PetModelTest {

    private val samplePet = Pet(
        id = "abc-123",
        name = "Luna",
        type = "Gato",
        age = "1 año",
        description = "Gata cariñosa y juguetona",
        contactInfo = "Tel: 555-9876",
        photoUrl = "https://storage.example.com/luna.jpg",
        ownerId = "user-uid-001",
        ownerName = "Sofía",
        createdAt = 1700000000000L
    )

    /**
     * Test 1: toFirestoreMap() incluye todos los campos requeridos.
     */
    @Test
    fun `toFirestoreMap incluye todos los campos`() {
        val map = samplePet.toFirestoreMap()

        assertEquals("abc-123", map["id"])
        assertEquals("Luna", map["name"])
        assertEquals("Gato", map["type"])
        assertEquals("1 año", map["age"])
        assertEquals("Gata cariñosa y juguetona", map["description"])
        assertEquals("Tel: 555-9876", map["contactInfo"])
        assertEquals("https://storage.example.com/luna.jpg", map["photoUrl"])
        assertEquals("user-uid-001", map["ownerId"])
        assertEquals("Sofía", map["ownerName"])
        assertEquals(1700000000000L, map["createdAt"])
    }

    /**
     * Test 2: fromFirestoreMap() reconstruye el objeto correctamente.
     */
    @Test
    fun `fromFirestoreMap reconstruye el Pet correctamente`() {
        val map = mapOf<String, Any>(
            "id" to "abc-123",
            "name" to "Luna",
            "type" to "Gato",
            "age" to "1 año",
            "description" to "Gata cariñosa y juguetona",
            "contactInfo" to "Tel: 555-9876",
            "photoUrl" to "https://storage.example.com/luna.jpg",
            "ownerId" to "user-uid-001",
            "ownerName" to "Sofía",
            "createdAt" to 1700000000000L
        )

        val pet = Pet.fromFirestoreMap(map)

        assertEquals(samplePet.id, pet.id)
        assertEquals(samplePet.name, pet.name)
        assertEquals(samplePet.type, pet.type)
        assertEquals(samplePet.age, pet.age)
        assertEquals(samplePet.description, pet.description)
        assertEquals(samplePet.contactInfo, pet.contactInfo)
        assertEquals(samplePet.photoUrl, pet.photoUrl)
        assertEquals(samplePet.ownerId, pet.ownerId)
        assertEquals(samplePet.ownerName, pet.ownerName)
        assertEquals(samplePet.createdAt, pet.createdAt)
    }

    /**
     * Test 3: fromFirestoreMap() maneja campos nulos con defaults seguros.
     * Un documento Firestore incompleto no debe lanzar excepción.
     */
    @Test
    fun `fromFirestoreMap con mapa vacio usa defaults seguros`() {
        val emptyMap = emptyMap<String, Any>()

        val pet = Pet.fromFirestoreMap(emptyMap)

        assertEquals("", pet.id)
        assertEquals("", pet.name)
        assertEquals("", pet.type)
        assertEquals("", pet.description)
        assertEquals("", pet.ownerId)
        // createdAt debe tener un valor (no 0)
        assertTrue(pet.createdAt > 0)
    }

    /**
     * Test 4: Round-trip — toFirestoreMap() → fromFirestoreMap() produce el mismo objeto.
     */
    @Test
    fun `roundtrip toFirestoreMap y fromFirestoreMap produce Pet identico`() {
        val map = samplePet.toFirestoreMap()
        val restored = Pet.fromFirestoreMap(map)

        assertEquals(samplePet.id, restored.id)
        assertEquals(samplePet.name, restored.name)
        assertEquals(samplePet.ownerId, restored.ownerId)
        assertEquals(samplePet.createdAt, restored.createdAt)
    }

    /**
     * Test 5: copy() funciona correctamente (inmutabilidad de data class).
     */
    @Test
    fun `copy modifica solo los campos especificados`() {
        val updated = samplePet.copy(name = "Luna II", photoUrl = "https://new-url.com/photo.jpg")

        assertEquals("Luna II", updated.name)
        assertEquals("https://new-url.com/photo.jpg", updated.photoUrl)
        // Los demás campos permanecen iguales
        assertEquals(samplePet.id, updated.id)
        assertEquals(samplePet.ownerId, updated.ownerId)
        assertEquals(samplePet.type, updated.type)
    }
}
