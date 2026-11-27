package io.github.cake.visordetexto.core

/** Tamaño máximo recomendado de la muestra inicial a analizar (64 KiB). */
const val MAX_BYTES_DETECCION: Int = 64 * 1024

/** Tipo de archivo detectado a partir de sus primeros bytes. */
enum class TipoArchivo {
    VACIO,
    TEXTO,
    PDF,
    ZIP,
    SIETE_Z,
    RAR,
    GZIP,
    BINARIO,
}

/** Codificación de texto detectada cuando [TipoArchivo] es [TipoArchivo.TEXTO]. */
enum class Codificacion {
    UTF8,
    UTF8_CON_BOM,
    UTF16LE,
    UTF16BE,
    WINDOWS_1252,
}

/** Nivel de certeza de la clasificación obtenida. */
enum class Confianza {
    ALTA,
    MEDIA,
    BAJA,
}

/**
 * Resultado de analizar los primeros bytes de una fuente (K-05, RF-16).
 *
 * @property tipo tipo de contenido identificado.
 * @property codificacion codificación aplicable si [tipo] es [TipoArchivo.TEXTO]; `null` en otro caso.
 * @property soloAscii `true` únicamente cuando es texto UTF-8 válido y todos sus bytes son `< 0x80`.
 * @property confianza nivel de certeza del resultado según las reglas deterministas.
 */
data class ResultadoDeteccion(
    val tipo: TipoArchivo,
    val codificacion: Codificacion?,
    val soloAscii: Boolean,
    val confianza: Confianza,
)

private val FIRMA_PDF = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D) // "%PDF-"
private val FIRMA_ZIP = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
private val FIRMA_SIETE_Z = byteArrayOf(0x37, 0x7A, 0xBC.toByte(), 0xAF.toByte(), 0x27, 0x1C)
private val FIRMA_RAR = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07) // "Rar!" + 1A 07
private val FIRMA_GZIP = byteArrayOf(0x1F, 0x8B.toByte())

/**
 * Analiza [muestra] (hasta los primeros 64 KiB de un archivo) y determina su tipo y codificación
 * de forma determinista sin depender de la extensión del archivo ni de APIs de Android.
 */
fun detectar(muestra: ByteArray): ResultadoDeteccion {
    // R1. Muestra vacía -> VACIO
    if (muestra.isEmpty()) {
        return ResultadoDeteccion(
            tipo = TipoArchivo.VACIO,
            codificacion = null,
            soloAscii = false,
            confianza = Confianza.ALTA,
        )
    }

    // R2. Firmas por bytes iniciales
    val tipoPorFirma = detectarFirmaBinaria(muestra)
    if (tipoPorFirma != null) {
        return ResultadoDeteccion(
            tipo = tipoPorFirma,
            codificacion = null,
            soloAscii = false,
            confianza = Confianza.ALTA,
        )
    }

    // R3. BOM (EF BB BF -> UTF8_CON_BOM; FF FE -> UTF16LE; FE FF -> UTF16BE)
    if (muestra.size >= 3 &&
        muestra[0] == 0xEF.toByte() &&
        muestra[1] == 0xBB.toByte() &&
        muestra[2] == 0xBF.toByte()
    ) {
        return ResultadoDeteccion(
            tipo = TipoArchivo.TEXTO,
            codificacion = Codificacion.UTF8_CON_BOM,
            soloAscii = false,
            confianza = Confianza.ALTA,
        )
    }
    if (muestra.size >= 2) {
        if (muestra[0] == 0xFF.toByte() && muestra[1] == 0xFE.toByte()) {
            return ResultadoDeteccion(
                tipo = TipoArchivo.TEXTO,
                codificacion = Codificacion.UTF16LE,
                soloAscii = false,
                confianza = Confianza.ALTA,
            )
        }
        if (muestra[0] == 0xFE.toByte() && muestra[1] == 0xFF.toByte()) {
            return ResultadoDeteccion(
                tipo = TipoArchivo.TEXTO,
                codificacion = Codificacion.UTF16BE,
                soloAscii = false,
                confianza = Confianza.ALTA,
            )
        }
    }

    // R4. Sin BOM, UTF-16 heurístico (longitud >= 4)
    if (muestra.size >= 4) {
        val utf16SinBom = detectarUtf16SinBom(muestra)
        if (utf16SinBom != null) {
            return ResultadoDeteccion(
                tipo = TipoArchivo.TEXTO,
                codificacion = utf16SinBom,
                soloAscii = false,
                confianza = Confianza.MEDIA,
            )
        }
    }

    // R5. Si contiene 0x00 y no cumplió R4 -> BINARIO
    for (b in muestra) {
        if (b == 0x00.toByte()) {
            return ResultadoDeteccion(
                tipo = TipoArchivo.BINARIO,
                codificacion = null,
                soloAscii = false,
                confianza = Confianza.ALTA,
            )
        }
    }

    // R6. Más del 10 % de bytes de control (salvo \t, \n, \r, \f) -> BINARIO
    var controles = 0
    for (b in muestra) {
        val v = b.toInt() and 0xFF
        if (esByteControlProhibido(v)) {
            controles++
        }
    }
    if (controles * 10 > muestra.size) {
        return ResultadoDeteccion(
            tipo = TipoArchivo.BINARIO,
            codificacion = null,
            soloAscii = false,
            confianza = Confianza.ALTA,
        )
    }

    // R7. Validación estricta de UTF-8 (tolerando secuencia incompleta solo al final)
    val estadoUtf8 = validarUtf8(muestra)
    if (estadoUtf8.valido) {
        return ResultadoDeteccion(
            tipo = TipoArchivo.TEXTO,
            codificacion = Codificacion.UTF8,
            soloAscii = estadoUtf8.soloAscii,
            confianza = Confianza.ALTA,
        )
    }

    // R8. No es UTF-8 válido pero pasó R5 y R6 -> TEXTO, WINDOWS_1252, confianza MEDIA
    return ResultadoDeteccion(
        tipo = TipoArchivo.TEXTO,
        codificacion = Codificacion.WINDOWS_1252,
        soloAscii = false,
        confianza = Confianza.MEDIA,
    )
}

private fun detectarFirmaBinaria(muestra: ByteArray): TipoArchivo? = when {
    empiezaCon(muestra, FIRMA_PDF) -> TipoArchivo.PDF
    empiezaCon(muestra, FIRMA_ZIP) -> TipoArchivo.ZIP
    empiezaCon(muestra, FIRMA_SIETE_Z) -> TipoArchivo.SIETE_Z
    empiezaCon(muestra, FIRMA_RAR) -> TipoArchivo.RAR
    empiezaCon(muestra, FIRMA_GZIP) -> TipoArchivo.GZIP
    else -> null
}

private fun empiezaCon(muestra: ByteArray, firma: ByteArray): Boolean {
    if (muestra.size < firma.size) return false
    for (i in firma.indices) {
        if (muestra[i] != firma[i]) return false
    }
    return true
}

private fun detectarUtf16SinBom(muestra: ByteArray): Codificacion? {
    var paresTotal = 0
    var paresCero = 0
    var imparesTotal = 0
    var imparesCero = 0

    for (i in muestra.indices) {
        if (i % 2 == 0) {
            paresTotal++
            if (muestra[i] == 0x00.toByte()) paresCero++
        } else {
            imparesTotal++
            if (muestra[i] == 0x00.toByte()) imparesCero++
        }
    }

    // UTF16LE: >= 80 % en posiciones impares son 0x00 y <= 5 % en pares son 0x00
    val esLe = (imparesCero * 100 >= imparesTotal * 80) && (paresCero * 100 <= paresTotal * 5)
    if (esLe) return Codificacion.UTF16LE

    // UTF16BE: >= 80 % en posiciones pares son 0x00 y <= 5 % en impares son 0x00
    val esBe = (paresCero * 100 >= paresTotal * 80) && (imparesCero * 100 <= imparesTotal * 5)
    if (esBe) return Codificacion.UTF16BE

    return null
}

private fun esByteControlProhibido(v: Int): Boolean {
    if (v == 0x09 || v == 0x0A || v == 0x0D || v == 0x0C) return false // \t, \n, \r, \f
    return v < 0x20 || v == 0x7F
}

private data class EstadoUtf8(val valido: Boolean, val soloAscii: Boolean)

private fun validarUtf8(muestra: ByteArray): EstadoUtf8 {
    var i = 0
    val n = muestra.size
    var soloAscii = true

    while (i < n) {
        val b0 = muestra[i].toInt() and 0xFF
        if (b0 < 0x80) {
            i++
            continue
        }

        soloAscii = false

        val longitud: Int
        val minCodigo: Int
        when {
            b0 in 0xC2..0xDF -> {
                longitud = 2
                minCodigo = 0x80
            }
            b0 in 0xE0..0xEF -> {
                longitud = 3
                minCodigo = 0x800
            }
            b0 in 0xF0..0xF4 -> {
                longitud = 4
                minCodigo = 0x10000
            }
            else -> return EstadoUtf8(valido = false, soloAscii = false) // Byte de continuación suelto, sobrelarga C0/C1 o > F4
        }

        // Si la secuencia queda cortada justo al final de la muestra, validar los bytes presentes y tolerar el corte
        if (i + longitud > n) {
            for (j in (i + 1) until n) {
                val bx = muestra[j].toInt() and 0xFF
                if ((bx and 0xC0) != 0x80) {
                    return EstadoUtf8(valido = false, soloAscii = false)
                }
            }
            // Verificar prefijo válido para 3 y 4 bytes antes de que lleguen todos los bytes
            if (longitud >= 3 && i + 1 < n) {
                val b1 = muestra[i + 1].toInt() and 0xFF
                if (b0 == 0xE0 && b1 < 0xA0) return EstadoUtf8(valido = false, soloAscii = false) // Sobrelarga
                if (b0 == 0xED && b1 >= 0xA0) return EstadoUtf8(valido = false, soloAscii = false) // Sustituto UTF-16
                if (b0 == 0xF0 && b1 < 0x90) return EstadoUtf8(valido = false, soloAscii = false) // Sobrelarga
                if (b0 == 0xF4 && b1 >= 0x90) return EstadoUtf8(valido = false, soloAscii = false) // > U+10FFFF
            }
            return EstadoUtf8(valido = true, soloAscii = false)
        }

        var puntoCodigo = when (longitud) {
            2 -> b0 and 0x1F
            3 -> b0 and 0x0F
            else -> b0 and 0x07
        }

        for (k in 1 until longitud) {
            val bk = muestra[i + k].toInt() and 0xFF
            if ((bk and 0xC0) != 0x80) {
                return EstadoUtf8(valido = false, soloAscii = false)
            }
            puntoCodigo = (puntoCodigo shl 6) or (bk and 0x3F)
        }

        // Rechazar sobrelargas, sustitutos (U+D800..U+DFFF) y puntos > U+10FFFF
        if (puntoCodigo < minCodigo || puntoCodigo in 0xD800..0xDFFF || puntoCodigo > 0x10FFFF) {
            return EstadoUtf8(valido = false, soloAscii = false)
        }

        i += longitud
    }

    return EstadoUtf8(valido = true, soloAscii = soloAscii)
}



