// app/src/main/java/com/adoptapet/app/data/model/User.kt
// Modelo de usuario para la aplicación

package com.adoptapet.app.data.model

/**
 * Modelo de usuario de AdoptaPet.
 * No es entidad Room porque los datos de usuario se obtienen de Firebase Auth
 * y se guardan en Firestore. Solo se usa en memoria durante la sesión.
 *
 * @param uid         UID único de Firebase Authentication
 * @param name        Nombre completo del usuario
 * @param email       Correo electrónico
 * @param photoUrl    URL de foto de perfil (opcional)
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = ""
) {
    /** Convierte el modelo a Map para guardar en Firestore */
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "photoUrl" to photoUrl
    )

    companion object {
        /** Construye un User desde un documento Firestore */
        fun fromFirestoreMap(map: Map<String, Any>): User = User(
            uid = map["uid"] as? String ?: "",
            name = map["name"] as? String ?: "",
            email = map["email"] as? String ?: "",
            photoUrl = map["photoUrl"] as? String ?: ""
        )
    }
}
