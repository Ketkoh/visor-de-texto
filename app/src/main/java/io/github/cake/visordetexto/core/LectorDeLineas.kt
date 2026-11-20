package io.github.cake.visordetexto.core

/** Una línea de texto ya decodificada. [truncada] es true si superó el máximo de caracteres. */
data class Linea(val numero: Long, val texto: String, val truncada: Boolean)

/**
 * Devuelve tramos de líneas leyendo solo lo necesario: parte del desplazamiento indexado más
 * cercano y recorre como máximo hasta la última línea pedida.
 *
 * - UTF-8; los bytes inválidos se decodifican como U+FFFD. Cada línea se decodifica completa
 *   (no por bloques), así que un carácter multibyte nunca se corta en un límite de lectura.
 * - Una línea más larga que [maxCaracteresPorLinea] se guarda cortada (truncada = true) sin
 *   acumularla completa: solo se retienen como máximo 4 bytes por carácter permitido.
 */
class LectorDeLineas(
    private val fuente: FuenteBytes,
    private val indice: IndiceLineas,
    val maxCaracteresPorLinea: Int = 4096,
) {
    init {
        require(maxCaracteresPorLinea >= 1) { "maxCaracteresPorLinea debe ser >= 1" }
    }

    /** Tamaño de cada lectura a la fuente. Visible solo para pruebas (bloques pequeños). */
    internal var bloqueLectura: Int = 64 * 1024

    private val maxBytesPorLinea: Int =
        minOf(maxCaracteresPorLinea.toLong() * 4L, (Int.MAX_VALUE - 16).toLong()).toInt()

    /**
     * Devuelve hasta [cantidad] líneas desde [desdeLinea] (desde 1). Lista vacía si la línea
     * pedida no existe o si el índice aún no llegó a ella (compruebe [IndiceLineas.completo]
     * para distinguir ambos casos). La línea 1 siempre se puede leer.
     */
    fun leerLineas(desdeLinea: Long, cantidad: Int): List<Linea> {
        require(desdeLinea >= 1L) { "las líneas se numeran desde 1" }
        require(cantidad >= 0) { "cantidad debe ser >= 0" }
        require(bloqueLectura >= 1)
        if (cantidad == 0) return emptyList()
        if (desdeLinea > 1L && !indice.completo && desdeLinea > indice.lineasConocidas) {
            return emptyList()
        }
        val total = indice.totalLineas
        if (total != null && desdeLinea > total) return emptyList()

        val punto = indice.desplazamientoCercano(desdeLinea)
        val resultado = ArrayList<Linea>(minOf(cantidad, 256))
        val buffer = ByteArray(bloqueLectura)
        var posicion = punto.desplazamiento
        var lineaActual = punto.linea - 1L
        var inicioPendiente = true
        var despuesDeCR = false
        var enRango = false
        var acumulado = ByteArray(minOf(maxBytesPorLinea, 256))
        var largo = 0
        var excedida = false

        while (true) {
            val leidos = fuente.leer(posicion, buffer, buffer.size)
            if (leidos <= 0) break
            var i = 0
            while (i < leidos) {
                val b = buffer[i].toInt()
                if (inicioPendiente) {
                    if (despuesDeCR && b == LF) { // segunda mitad de un \r\n
                        despuesDeCR = false
                        i++
                        continue
                    }
                    lineaActual++
                    inicioPendiente = false
                    despuesDeCR = false
                    enRango = lineaActual >= desdeLinea
                }
                if (b == LF || b == CR) {
                    if (enRango) {
                        resultado.add(crearLinea(lineaActual, acumulado, largo, excedida))
                        if (resultado.size == cantidad) return resultado
                        largo = 0
                        excedida = false
                        enRango = false
                    }
                    inicioPendiente = true
                    despuesDeCR = b == CR
                } else if (enRango) {
                    if (largo < maxBytesPorLinea) {
                        if (largo == acumulado.size) {
                            acumulado = acumulado.copyOf(
                                minOf(acumulado.size * 2L, maxBytesPorLinea.toLong()).toInt(),
                            )
                        }
                        acumulado[largo++] = buffer[i]
                    } else {
                        excedida = true
                    }
                }
                i++
            }
            posicion += leidos
        }
        // Fin del archivo sin terminador: la última línea queda abierta.
        if (!inicioPendiente && enRango) {
            resultado.add(crearLinea(lineaActual, acumulado, largo, excedida))
        }
        return resultado
    }

    private fun crearLinea(numero: Long, bytes: ByteArray, largo: Int, excedida: Boolean): Linea {
        var texto = String(bytes, 0, largo, Charsets.UTF_8)
        var truncada = excedida
        if (texto.length > maxCaracteresPorLinea) {
            var corte = maxCaracteresPorLinea
            if (Character.isHighSurrogate(texto[corte - 1])) corte-- // no partir un par sustituto
            texto = texto.substring(0, corte)
            truncada = true
        }
        return Linea(numero, texto, truncada)
    }

    private companion object {
        const val LF = 0x0A
        const val CR = 0x0D
    }
}
