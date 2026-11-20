package io.github.cake.visordetexto.core

import java.io.File

/** Utilidades compartidas por las pruebas de K-04a. */
internal object ApoyoPruebas {
    /** Carpeta del corpus (variable de entorno VISOR_CORPUS) o null si no está definida. */
    fun directorioCorpus(): File? {
        val ruta = System.getenv("VISOR_CORPUS")
        return if (ruta.isNullOrBlank()) null else File(ruta)
    }

    /** Archivo del corpus; falla con un mensaje claro si no existe. */
    fun archivoCorpus(nombre: String): File {
        val carpeta = directorioCorpus() ?: error("VISOR_CORPUS no está definida")
        val archivo = File(carpeta, nombre)
        check(archivo.isFile) { "No existe el archivo del corpus: ${archivo.absolutePath}" }
        return archivo
    }

    /** Implementación de referencia: líneas de un texto con terminadores \n, \r\n y \r. */
    fun referencia(texto: String): List<String> {
        if (texto.isEmpty()) return emptyList()
        val partes = texto.split(Regex("\r\n|\r|\n"))
        return if (texto.endsWith("\n") || texto.endsWith("\r")) partes.dropLast(1) else partes
    }

    /** Construye el índice completo de [fuente]. */
    fun indiceCompleto(fuente: FuenteBytes, intervalo: Int = 1000, bloque: Int = 1024 * 1024): IndiceLineas =
        IndiceLineas(fuente, intervalo, bloque).also { it.construir() }

    /** Fuente que registra el mayor tamaño solicitado en una sola lectura. */
    class FuenteContadora(private val interna: FuenteBytes) : FuenteBytes {
        var maxSolicitado: Int = 0
            private set
        override val tamano: Long get() = interna.tamano
        override fun leer(desde: Long, destino: ByteArray, cantidad: Int): Int {
            if (cantidad > maxSolicitado) maxSolicitado = cantidad
            return interna.leer(desde, destino, cantidad)
        }
    }
}
