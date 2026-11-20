package io.github.cake.visordetexto.core

import java.io.File
import java.io.RandomAccessFile

/**
 * Origen de bytes de solo lectura con acceso aleatorio.
 * No depende de android.* para poder probarse con JUnit local (K-04a).
 */
interface FuenteBytes {
    /** Tamaño total en bytes. */
    val tamano: Long

    /**
     * Copia en [destino] (desde su posición 0) hasta [cantidad] bytes que empiezan en el
     * desplazamiento [desde] de la fuente. Devuelve los bytes leídos; 0 si [desde] >= [tamano].
     * Puede devolver menos de [cantidad] sin haber llegado al final.
     */
    fun leer(desde: Long, destino: ByteArray, cantidad: Int): Int
}

/**
 * Fuente respaldada por un archivo, abierto únicamente para lectura.
 * Es segura entre hilos: el índice (hilo de fondo) y el lector comparten una misma instancia.
 */
class FuenteArchivo(archivo: File) : FuenteBytes, AutoCloseable {
    private val acceso = RandomAccessFile(archivo, "r")

    override val tamano: Long = acceso.length()

    @Synchronized
    override fun leer(desde: Long, destino: ByteArray, cantidad: Int): Int {
        require(desde >= 0L) { "desde debe ser >= 0" }
        require(cantidad >= 0 && cantidad <= destino.size) { "cantidad fuera de rango" }
        if (desde >= tamano || cantidad == 0) return 0
        acceso.seek(desde)
        var total = 0
        while (total < cantidad) {
            val n = acceso.read(destino, total, cantidad - total)
            if (n <= 0) break
            total += n
        }
        return total
    }

    @Synchronized
    override fun close() {
        acceso.close()
    }
}
