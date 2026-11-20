package io.github.cake.visordetexto.core

/** Representa los datos relevantes de un Intent sin depender de android.*. */
data class DatosIntentEntrada(
    val accion: String?,
    val uriVista: String?,
    val uriEnvio: String?,
    val uriClip: String? = null,
)

/**
 * Extrae de forma pura la URI en formato texto desde un Intent de entrada (K-08 / RF-13).
 * Devuelve null si la acción no corresponde a apertura externa o no trae URI válida.
 */
fun extraerUriDesdeIntent(datos: DatosIntentEntrada): String? {
    val candidata = when (datos.accion) {
        "android.intent.action.VIEW" -> datos.uriVista
        "android.intent.action.SEND" -> datos.uriEnvio ?: datos.uriClip
        else -> null
    }
    return candidata?.trim()?.takeIf { it.isNotEmpty() }
}
