// app/src/main/java/com/adoptapet/app/data/model/Pet.kt
// Entidad Room + modelo Firestore para mascotas

package com.adoptapet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad principal de mascota.
 * Sirve tanto como entidad Room (caché local) como modelo de datos para Firestore.
 *
 * @param id           ID único generado por Firestore (string UUID)
 * @param name         Nombre de la mascota
 * @param type         Tipo: "perro", "gato" u "otro"
 * @param age          Edad aproximada (ej: "2 años", "6 meses")
 * @param city         Ciudad donde se encuentra la mascota
 * @param description  Descripción completa de la mascota
 * @param contactInfo  Información de contacto del responsable
 * @param photoUrl     URL de la foto en Firebase Storage (o vacío si no hay)
 * @param ownerId      UID del usuario Firebase que publicó la mascota
 * @param ownerName    Nombre del usuario propietario
 * @param createdAt    Timestamp de creación (millis)
 */
@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val age: String = "",
    val city: String = "", // <-- Propiedad integrada con éxito
    val description: String = "",
    val contactInfo: String = "",
    val photoUrl: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Convierte la entidad a un Map para guardar en Firestore.
     * Room usa el data class directamente; Firestore requiere Map<String, Any>.
     */
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "type" to type,
        "age" to age,
        "city" to city, // <-- Mapeado a la base remota
        "description" to description,
        "contactInfo" to contactInfo,
        "photoUrl" to photoUrl,
        "ownerId" to ownerId,
        "ownerName" to ownerName,
        "createdAt" to createdAt
    )

    companion object {
        /**
         * Construye un Pet a partir de un documento Firestore (Map).
         * Maneja valores nulos con defaults seguros.
         */
        fun fromFirestoreMap(map: Map<String, Any>): Pet = Pet(
            id = map["id"] as? String ?: "",
            name = map["name"] as? String ?: "",
            type = map["type"] as? String ?: "",
            age = map["age"] as? String ?: "",
            city = map["city"] as? String ?: "", // <-- Recuperado de forma segura
            description = map["description"] as? String ?: "",
            contactInfo = map["contactInfo"] as? String ?: "",
            photoUrl = map["photoUrl"] as? String ?: "",
            ownerId = map["ownerId"] as? String ?: "",
            ownerName = map["ownerName"] as? String ?: "",
            createdAt = (map["createdAt"] as? Long) ?: System.currentTimeMillis()
        )
    }
}