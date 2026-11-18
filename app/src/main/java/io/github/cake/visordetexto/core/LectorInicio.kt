package io.github.cake.visordetexto.core

import java.io.InputStream

/** Cantidad máxima por defecto de bytes que se leen del inicio de un archivo (8 KiB). */
const val MAX_BYTES_INICIO: Int = 8192

/**
 * Resultado de leer el inicio de un archivo (K-02).
 *
 * @property texto texto decodificado en UTF-8, sin bytes incompletos al final.
 * @property bytesLeidos cantidad de bytes que forman [texto] (es decir, `texto` codificado
 * en UTF-8 ocupa exactamente esta cantidad). Nunca supera `maxBytes`.
 * @property truncado `true` si el archivo tiene al menos un byte más después de `maxBytes`.
 */
data class ResultadoLectura(
    val texto: String,
    val bytesLeidos: Int,
    val truncado: Boolean,
)

/**
 * Lee como máximo [maxBytes] bytes del inicio de [entrada] y los decodifica como UTF-8.
 *
 * - Lee a lo sumo `maxBytes + 1` bytes: el byte adicional solo sirve para saber si hubo
 *   truncado y nunca forma parte del texto.
 * - Si el corte cae a mitad de un carácter multibyte, se descartan los bytes incompletos
 *   (no se usa símbolo de reemplazo para ese caso).
 * - No cierra [entrada]: el llamador es responsable de cerrarla (por ejemplo con `use`).
 *
 * @throws IllegalArgumentException si [maxBytes] es negativo o igual a [Int.MAX_VALUE].
 * @throws java.io.IOException si falla la lectura de [entrada].
 */
fun leerInicio(entrada: InputStream, maxBytes: Int = MAX_BYTES_INICIO): ResultadoLectura {
    require(maxBytes in 0 until Int.MAX_VALUE) { "maxBytes debe estar entre 0 y Int.MAX_VALUE - 1" }

    val capacidad = maxBytes + 1
    val buffer = ByteArray(capacidad)
    var total = 0
    while (total < capacidad) {
        val n = entrada.read(buffer, total, capacidad - total)
        if (n < 0) break
        total += n
    }

    val truncado = total > maxBytes
    val limite = if (truncado) maxBytes else total
    val conservar = if (truncado) longitudSinCaracterIncompleto(buffer, limite) else limite

    return ResultadoLectura(
        texto = String(buffer, 0, conservar, Charsets.UTF_8),
        bytesLeidos = conservar,
        truncado = truncado,
    )
}

/**
 * Devuelve cuántos de los primeros [fin] bytes se conservan para no dejar una secuencia
 * UTF-8 incompleta al final. Solo descarta cuando el último carácter empieza con un byte
 * inicial válido y le faltan bytes de continuación.
 */
private fun longitudSinCaracterIncompleto(bytes: ByteArray, fin: Int): Int {
    var i = fin - 1
    var continuaciones = 0
    while (i >= 0 && continuaciones < 3 && (bytes[i].toInt() and 0xC0) == 0x80) {
        i--
        continuaciones++
    }
    if (i < 0) return fin

    val inicial = bytes[i].toInt() and 0xFF
    val esperados = when {
        (inicial and 0xF8) == 0xF0 -> 4
        (inicial and 0xF0) == 0xE0 -> 3
        (inicial and 0xE0) == 0xC0 -> 2
        else -> return fin // ASCII o byte que no inicia una secuencia: se deja al decodificador
    }
    return if (continuaciones + 1 < esperados) i else fin
}