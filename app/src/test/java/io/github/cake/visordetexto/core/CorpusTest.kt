package io.github.cake.visordetexto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

/** Cada línea de grande_utf8.txt ocupa exactamente 100 bytes (terminador incluido). */
private const val BYTES_POR_LINEA = 100L

/** Prefijo que debe tener la línea [n] de grande_utf8.txt: equivale a f"L{n:012d}|" de Python. */
private fun prefijo(n: Long): String = String.format(Locale.ROOT, "L%012d|", n)

/** Lee la línea [n] y comprueba su número y su prefijo. */
private fun verificarLinea(lector: LectorDeLineas, n: Long) {
    val lineas = lector.leerLineas(n, 1)
    assertEquals("la línea $n debe existir", 1, lineas.size)
    assertEquals(n, lineas[0].numero)
    assertTrue(
        "la línea $n debe empezar por ${prefijo(n)} y empieza por '${lineas[0].texto.take(20)}'",
        lineas[0].texto.startsWith(prefijo(n)),
    )
}

/**
 * Pruebas de K-04a contra el corpus de K-33 (y de K-04c: el mismo código sirve para el corpus de 1 GB
 * y para el de 3 GB). Se omiten si VISOR_CORPUS no está definida.
 */
class CorpusTest {
    @Before
    fun requiereCorpus() {
        assumeTrue("VISOR_CORPUS no está definida: pruebas de corpus omitidas", ApoyoPruebas.directorioCorpus() != null)
    }

    @Test
    fun K04a_corpus_vacio_tieneCeroLineas() {
        FuenteArchivo(ApoyoPruebas.archivoCorpus("vacio.txt")).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente)
            assertEquals(0L, indice.totalLineas)
            assertTrue(LectorDeLineas(fuente, indice).leerLineas(1, 10).isEmpty())
        }
    }

    @Test
    fun K04a_corpus_saltosMixtos_tiene30Lineas() {
        FuenteArchivo(ApoyoPruebas.archivoCorpus("saltos_mixtos.txt")).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente)
            assertEquals(30L, indice.totalLineas)
            assertEquals(30, LectorDeLineas(fuente, indice).leerLineas(1, 100).size)
        }
    }

    @Test
    fun K04a_corpus_unaLinea10mb_seTrunca() {
        FuenteArchivo(ApoyoPruebas.archivoCorpus("una_linea_10mb.txt")).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente)
            assertEquals(1L, indice.totalLineas)
            val lineas = LectorDeLineas(fuente, indice).leerLineas(1, 10)
            assertEquals(1, lineas.size)
            assertTrue(lineas[0].truncada)
            assertTrue(lineas[0].texto.length <= 4096)
        }
    }

    @Test
    fun K04a_corpus_tildesUtf8_seDecodificanCorrectamente() {
        // Se desconoce el texto exacto del archivo: se compara con la decodificación completa de referencia
        // (el archivo es pequeño) y se exige que haya tildes y ningún carácter de reemplazo.
        val archivo = ApoyoPruebas.archivoCorpus("tildes_utf8.txt")
        val esperado = ApoyoPruebas.referencia(String(archivo.readBytes(), Charsets.UTF_8))
        FuenteArchivo(archivo).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente)
            val lineas = LectorDeLineas(fuente, indice, maxCaracteresPorLinea = Int.MAX_VALUE / 8)
                .leerLineas(1, esperado.size + 5)
            assertEquals(esperado, lineas.map { it.texto })
            val todo = lineas.joinToString("\n") { it.texto }
            assertFalse("aparece U+FFFD", todo.contains('\uFFFD'))
            assertTrue("no hay ninguna tilde ni eñe", todo.any { it in "áéíóúüñÁÉÍÓÚÜÑ" })
        }
    }

    @Test
    fun K04a_corpus_grandeUtf8_lineasSegunTamano() {
        FuenteArchivo(ApoyoPruebas.archivoCorpus("grande_utf8.txt")).use { fuente ->
            val tamano = fuente.tamano
            assertEquals("grande_utf8.txt debe tener un tamaño múltiplo de 100 bytes", 0L, tamano % BYTES_POR_LINEA)
            val esperadas = tamano / BYTES_POR_LINEA
            println("[K-04a/K-04c] grande_utf8.txt: $tamano bytes, $esperadas líneas esperadas")

            val indice = ApoyoPruebas.indiceCompleto(fuente)
            assertEquals(esperadas, indice.totalLineas)
            val maximo = esperadas / indice.intervalo + 1
            assertTrue(
                "guardó ${indice.desplazamientosGuardados}, máximo $maximo",
                indice.desplazamientosGuardados.toLong() <= maximo,
            )

            val lector = LectorDeLineas(fuente, indice)
            verificarLinea(lector, 1L)
            verificarLinea(lector, esperadas / 2)
            verificarLinea(lector, esperadas)
            assertTrue(lector.leerLineas(esperadas + 1, 1).isEmpty())
        }
    }
}
