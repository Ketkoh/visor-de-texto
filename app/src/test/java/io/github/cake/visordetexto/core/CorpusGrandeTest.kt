package io.github.cake.visordetexto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * Prueba de K-04c: el núcleo con un archivo de más de 2 GiB (corpus de 3 GB: 30.000.000 de líneas de
 * 100 bytes). Solo se ejecuta si existe la variable de entorno VISOR_CORPUS_GRANDE, que puede apuntar
 * a la carpeta que contiene grande_utf8.txt o directamente al archivo. Si no existe, se omite (Assume).
 */
class CorpusGrandeTest {
    private fun ms(nanos: Long) = String.format(Locale.ROOT, "%.2f ms", nanos / 1e6)
    private fun mib(bytes: Long) = String.format(Locale.ROOT, "%.1f MiB", bytes / 1048576.0)

    /** Equivale a f"L{n:012d}|" de Python. */
    private fun prefijo(n: Long): String = String.format(Locale.ROOT, "L%012d|", n)

    private fun rutaGrande(): String? = System.getenv("VISOR_CORPUS_GRANDE")?.takeIf { it.isNotBlank() }

    private fun archivoGrande(): File {
        val ruta = rutaGrande() ?: error("VISOR_CORPUS_GRANDE no está definida")
        val base = File(ruta)
        val archivo = if (base.isDirectory) File(base, "grande_utf8.txt") else base
        check(archivo.isFile) { "No existe el archivo del corpus grande: ${archivo.absolutePath}" }
        return archivo
    }

    /** Lee directamente de la fuente [largo] bytes desde [desde] (para comprobar un desplazamiento guardado). */
    private fun textoEn(fuente: FuenteBytes, desde: Long, largo: Int): String {
        val destino = ByteArray(largo)
        val leidos = fuente.leer(desde, destino, largo)
        return String(destino, 0, leidos, Charsets.US_ASCII)
    }

    private fun verificarLinea(lector: LectorDeLineas, n: Long) {
        val lineas = lector.leerLineas(n, 1)
        assertEquals("la línea $n debe existir", 1, lineas.size)
        assertEquals(n, lineas[0].numero)
        assertTrue(
            "la línea $n debe empezar por ${prefijo(n)} y empieza por '${lineas[0].texto.take(20)}'",
            lineas[0].texto.startsWith(prefijo(n)),
        )
    }

    @Before
    fun requiereCorpusGrande() {
        assumeTrue("VISOR_CORPUS_GRANDE no está definida: prueba de más de 2 GiB omitida", rutaGrande() != null)
    }

    @Test
    fun K04c_corpusGrande_masDe2GiB_30MillonesDeLineas() {
        FuenteArchivo(archivoGrande()).use { fuente ->
            // Sin esto la prueba podría pasar con un archivo pequeño sin demostrar nada.
            assertTrue(
                "el corpus debe superar Int.MAX_VALUE (${Int.MAX_VALUE}) bytes y tiene ${fuente.tamano}",
                fuente.tamano > Int.MAX_VALUE.toLong(),
            )
            val indice = IndiceLineas(fuente)
            val lector = LectorDeLineas(fuente, indice)

            // 1) Primera ventana, en frío y antes de construir el índice.
            val t0 = System.nanoTime()
            val ventana = lector.leerLineas(1, 50)
            val primeraVentana = System.nanoTime() - t0
            assertEquals(50, ventana.size)

            // 2) Construcción del índice.
            val t1 = System.nanoTime()
            indice.construir()
            val construir = System.nanoTime() - t1
            assertTrue(indice.completo)
            assertEquals(30_000_000L, indice.totalLineas)
            assertTrue("guardó ${indice.desplazamientosGuardados}", indice.desplazamientosGuardados <= 30_001)

            // 3) Desplazamientos guardados por encima de 2^31: se comprueba el valor y que apunten al
            //    inicio real de la línea leyendo directamente de la fuente.
            for (linea in longArrayOf(21_475_001L, 25_000_000L)) {
                val punto = indice.desplazamientoCercano(linea)
                assertTrue(
                    "el desplazamiento ${punto.desplazamiento} debe superar Int.MAX_VALUE",
                    punto.desplazamiento > Int.MAX_VALUE.toLong(),
                )
                assertEquals(prefijo(punto.linea), textoEn(fuente, punto.desplazamiento, prefijo(punto.linea).length))
            }

            // 4) Alrededor del byte 2^31 (con líneas de 100 bytes, la línea 21.474.837 cruza ese byte).
            val tramo = lector.leerLineas(21_474_836L, 5)
            assertEquals(5, tramo.size)
            tramo.forEachIndexed { i, linea ->
                val n = 21_474_836L + i
                assertEquals(n, linea.numero)
                assertTrue("la línea $n empieza por '${linea.texto.take(20)}'", linea.texto.startsWith(prefijo(n)))
            }
            for (n in 21_474_836L..21_474_840L) verificarLinea(lector, n)

            // 5) Última línea y primera inexistente.
            val ultima = lector.leerLineas(30_000_000L, 1)
            assertEquals(1, ultima.size)
            assertEquals(30_000_000L, ultima[0].numero)
            assertTrue(ultima[0].texto.startsWith("L000030000000|"))
            assertTrue(lector.leerLineas(30_000_001L, 1).isEmpty())

            // 6) Lectura de una ventana en el tramo 25.000.000.
            val t2 = System.nanoTime()
            val lectura = lector.leerLineas(25_000_000L, 50)
            val lecturaMedio = System.nanoTime() - t2
            assertEquals(50, lectura.size)
            assertEquals(25_000_000L, lectura.first().numero)
            assertTrue(lectura.first().texto.startsWith(prefijo(25_000_000L)))
            assertTrue(lectura.last().texto.startsWith(prefijo(25_000_049L)))

            println("[K-04c] corpus grande = ${fuente.tamano} bytes (${mib(fuente.tamano)}), ${indice.totalLineas} líneas")
            println("[K-04c] construir índice: ${ms(construir)}")
            println("[K-04c] primera ventana leerLineas(1, 50) antes del índice, en frío: ${ms(primeraVentana)}")
            println("[K-04c] leerLineas(25_000_000, 50): ${ms(lecturaMedio)}")
            println("[K-04c] desplazamientos guardados: ${indice.desplazamientosGuardados}")
        }
    }
}
