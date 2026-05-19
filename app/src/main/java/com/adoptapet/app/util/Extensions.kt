// app/src/main/java/com/adoptapet/app/util/Extensions.kt
// Funciones de extensión reutilizables en toda la aplicación

package com.adoptapet.app.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

/**
 * Colección de funciones de extensión para simplificar operaciones comunes en la UI.
 */

// ─── View extensions ─────────────────────────────────────────────────────────

/** Hace visible una View */
fun View.show() {
    visibility = View.VISIBLE
}

/** Oculta una View (no ocupa espacio) */
fun View.hide() {
    visibility = View.GONE
}

/** Hace invisible una View (mantiene el espacio) */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Muestra u oculta una View según el parámetro [visible].
 * Si [visible] es true usa VISIBLE, si no usa GONE.
 */
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

// ─── Context extensions ───────────────────────────────────────────────────────

/** Muestra un Toast corto */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/** Muestra un Toast largo */
fun Context.showToastLong(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// ─── Snackbar extensions ──────────────────────────────────────────────────────

/**
 * Muestra un Snackbar estándar sobre una View raíz.
 *
 * @param message  Texto a mostrar
 * @param duration Duración (Snackbar.LENGTH_SHORT | LENGTH_LONG | LENGTH_INDEFINITE)
 */
fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, duration).show()
}

/**
 * Muestra un Snackbar con botón de acción.
 */
fun View.showSnackbarAction(
    message: String,
    actionLabel: String,
    duration: Int = Snackbar.LENGTH_LONG,
    action: () -> Unit
) {
    Snackbar.make(this, message, duration)
        .setAction(actionLabel) { action() }
        .show()
}

// ─── Keyboard extensions ─────────────────────────────────────────────────────

/** Oculta el teclado virtual desde cualquier View */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

// ─── String extensions ────────────────────────────────────────────────────────

/** Retorna true si el string es un email válido */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/** Capitaliza la primera letra de cada palabra */
fun String.titleCase(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
}
