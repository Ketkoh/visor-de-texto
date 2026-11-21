package io.github.cake.visordetexto.core

/** Línea indexada ([linea], numerada desde 1) y el [desplazamiento] en bytes donde empieza. */
data class PuntoIndice(val linea: Long, val desplazamiento: Long)

/**
 * Índice de líneas disperso: guarda solo el desplazamiento de las líneas 1 + k × [intervalo].
 * Terminadores reconocidos: \n, \r\n y \r (un \r\n cuenta como uno solo, aunque quede partido
 * entre dos bloques). Un terminador final no crea una línea vacía; un archivo vacío tiene 0 líneas.
 *
 * [construir] está pensado para ejecutarse en un hilo de fondo; las propiedades y
 * [desplazamientoCercano] pueden consultarse desde otro hilo mientras tanto.
 */
class IndiceLineas(
    private val fuente: FuenteBytes,
    val intervalo: Int = 1000,
    val bloque: Int = 1024 * 1024,
) {
    init {
        require(intervalo >= 1) { "intervalo debe ser >= 1" }
        require(bloque >= 1) { "bloque debe ser >= 1" }
    }

    private val cerrojo = Any()
    private var desplazamientos = LongArray(1024)
    private var cantidadGuardada = 0

    /** Líneas cuyo inicio ya se encontró. Crece durante la construcción. */
    @Volatile
    var lineasConocidas: Long = 0L
        private set

    @Volatile
    var completo: Boolean = false
        private set

    /** null hasta que la construcción termina. */
    @Volatile
    var totalLineas: Long? = null
        private set

    /** Cantidad de desplazamientos guardados (para verificar el uso de memoria). */
    val desplazamientosGuardados: Int
        get() = synchronized(cerrojo) { cantidadGuardada }

    /**
     * Recorre la fuente por bloques (nunca completa en memoria). Si [cancelado] devuelve true
     * (se consulta antes de cada bloque) se detiene: lo ya indexado sigue siendo válido,
     * [completo] queda en false y [totalLineas] en null. [progreso] recibe los bytes recorridos.
     * Volver a llamar reinicia la construcción.
     */
    fun construir(cancelado: () -> Boolean = { false }, progreso: (Long) -> Unit = {}) {
        synchronized(cerrojo) { cantidadGuardada = 0 }
        lineasConocidas = 0L
        totalLineas = null
        completo = false

        val buffer = ByteArray(bloque)
        var posicion = 0L
        var lineas = 0L
        var faltan = 0 // líneas por saltar hasta la próxima línea indexada
        var inicioPendiente = true // el próximo byte (salvo el \n de un \r\n) inicia una línea
        var despuesDeCR = false

        while (true) {
            if (cancelado()) return
            val leidos = fuente.leer(posicion, buffer, bloque)
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
                    lineas++
                    if (faltan == 0) {
                        guardar(posicion + i)
                        faltan = intervalo
                    }
                    faltan--
                    inicioPendiente = false
                    despuesDeCR = false
                }
                if (b == LF) {
                    inicioPendiente = true
                } else if (b == CR) {
                    inicioPendiente = true
                    despuesDeCR = true
                }
                i++
            }
            posicion += leidos
            lineasConocidas = lineas
            progreso(posicion)
        }
        lineasConocidas = lineas
        totalLineas = lineas
        completo = true
    }

    /**
     * La mayor línea indexada <= [linea] y su desplazamiento. La línea 1 (desplazamiento 0)
     * está disponible siempre, incluso antes de construir.
     */
    fun desplazamientoCercano(linea: Long): PuntoIndice {
        require(linea >= 1L) { "las líneas se numeran desde 1" }
        synchronized(cerrojo) {
            if (cantidadGuardada == 0) return PuntoIndice(1L, 0L)
            val k = minOf((linea - 1L) / intervalo, (cantidadGuardada - 1).toLong()).toInt()
            return PuntoIndice(1L + k.toLong() * intervalo, desplazamientos[k])
        }
    }

    private fun guardar(desplazamiento: Long) {
        synchronized(cerrojo) {
            if (cantidadGuardada == desplazamientos.size) {
                // La duplicación se calcula en Long: con Int, 2^30 * 2 desbordaría a un valor negativo.
                val nuevaCapacidad = minOf(cantidadGuardada.toLong() * 2L, MAX_DESPLAZAMIENTOS.toLong()).toInt()
                check(nuevaCapacidad > cantidadGuardada) {
                    "demasiados desplazamientos indexados ($cantidadGuardada); use un intervalo mayor"
                }
                desplazamientos = desplazamientos.copyOf(nuevaCapacidad)
            }
            desplazamientos[cantidadGuardada++] = desplazamiento
        }
    }

    private companion object {
        const val LF = 0x0A
        const val CR = 0x0D

        /** Tamaño máximo prudente de un LongArray en la JVM (algunas máquinas virtuales reservan cabecera). */
        const val MAX_DESPLAZAMIENTOS = Int.MAX_VALUE - 8
    }
}
