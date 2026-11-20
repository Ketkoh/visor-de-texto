package io.github.cake.visordetexto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/** Pruebas de K-04a contra el corpus de K-33. Se omiten si VISOR_CORPUS no está definida. */
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
    fun K04a_corpus_grandeUtf8_10MillonesDeLineas() {
        FuenteArchivo(ApoyoPruebas.archivoCorpus("grande_utf8.txt")).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente)
            assertEquals(10_000_000L, indice.totalLineas)
            assertTrue("guardó ${indice.desplazamientosGuardados}", indice.desplazamientosGuardados <= 10_001)
            val lector = LectorDeLineas(fuente, indice)
            assertEquals(1L, lector.leerLineas(1, 1)[0].numero)
            assertTrue(lector.leerLineas(1, 1)[0].texto.startsWith("L000000000001|"))
            assertTrue(lector.leerLineas(5_000_000, 1)[0].texto.startsWith("L000005000000|"))
            assertTrue(lector.leerLineas(10_000_000, 1)[0].texto.startsWith("L000010000000|"))
            assertTrue(lector.leerLineas(10_000_001, 1).isEmpty())
        }
    }
}
