// app/src/main/java/com/adoptapet/app/data/repository/AuthRepository.kt
// Repositorio de autenticación: encapsula Firebase Auth y Firestore para usuarios

package com.adoptapet.app.data.repository

import com.adoptapet.app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de autenticación y gestión de usuarios.
 * Encapsula toda la lógica de Firebase Auth y Firestore de usuarios.
 *
 * Operaciones soportadas:
 * - Registro con email/password
 * - Login con email/password
 * - Cierre de sesión
 * - Obtener usuario autenticado actual
 * - Obtener perfil del usuario desde Firestore
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLLECTION_USERS = "users"
    }

    // ─── Estado de autenticación ─────────────────────────────────────────────

    /**
     * Retorna el FirebaseUser actualmente autenticado, o null si no hay sesión.
     */
    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    /**
     * Retorna true si hay una sesión activa.
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // ─── Registro ────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo usuario con email y contraseña.
     * Crea el usuario en Firebase Auth y guarda su perfil en Firestore.
     *
     * @param name      Nombre completo del usuario
     * @param email     Correo electrónico
     * @param password  Contraseña (mínimo 6 caracteres)
     * @return          El objeto [User] del nuevo usuario
     * @throws Exception si el email ya está registrado o la contraseña es débil
     */
    suspend fun register(name: String, email: String, password: String): User {
        // 1. Crear cuenta en Firebase Auth
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
            ?: throw Exception("Error al crear usuario en Firebase Auth")

        // 2. Construir modelo de usuario
        val user = User(
            uid = firebaseUser.uid,
            name = name,
            email = email,
            photoUrl = ""
        )

        // 3. Guardar perfil en Firestore para acceso posterior
        firestore.collection(COLLECTION_USERS)
            .document(firebaseUser.uid)
            .set(user.toFirestoreMap())
            .await()

        return user
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * Inicia sesión con email y contraseña.
     *
     * @param email     Correo electrónico
     * @param password  Contraseña
     * @return          El objeto [User] del usuario autenticado
     * @throws Exception si las credenciales son incorrectas
     */
    suspend fun login(email: String, password: String): User {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
            ?: throw Exception("Error al iniciar sesión")

        // Intentar obtener perfil desde Firestore
        return getUserProfile(firebaseUser.uid)
            ?: User(uid = firebaseUser.uid, email = email, name = "")
    }

    // ─── Perfil de usuario ───────────────────────────────────────────────────

    /**
     * Obtiene el perfil de un usuario desde Firestore.
     *
     * @param uid  UID del usuario
     * @return     [User] si existe el documento, null en caso contrario
     */
    suspend fun getUserProfile(uid: String): User? {
        val doc = firestore.collection(COLLECTION_USERS)
            .document(uid)
            .get()
            .await()

        return if (doc.exists()) {
            doc.data?.let { User.fromFirestoreMap(it) }
        } else {
            null
        }
    }

    /**
     * Obtiene el perfil del usuario actualmente autenticado.
     *
     * @return [User] o null si no hay sesión activa
     */
    suspend fun getCurrentUserProfile(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return getUserProfile(uid)
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    /**
     * Cierra la sesión activa en Firebase Auth.
     */
    fun logout() {
        auth.signOut()
    }
}
