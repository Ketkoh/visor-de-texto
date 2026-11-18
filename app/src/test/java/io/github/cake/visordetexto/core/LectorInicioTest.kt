package io.github.cake.visordetexto.core

import java.io.ByteArrayInputStream
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas unitarias locales de la tarjeta K-02 (`leerInicio`). */
class LectorInicioTest {

    private fun entrada(texto: String): InputStream =
        ByteArrayInputStream(texto.toByteArray(Charsets.UTF_8))

    private fun tamanoUtf8(texto: String): Int = texto.toByteArray(Charsets.UTF_8).size

    // ---- Criterio A1: casos obligatorios ----

    @Test
    fun k02_archivoVacio() {
        val r = leerInicio(entrada(""))
        assertEquals("", r.texto)
        assertEquals(0, r.bytesLeidos)
        assertFalse(r.truncado)
    }

    @Test
    fun k02_archivoMenorAlLimite() {
        val r = leerInicio(entrada("Hola mundo"))
        assertEquals("Hola mundo", r.texto)
        assertEquals(10, r.bytesLeidos)
        assertFalse(r.truncado)
    }

    @Test
    fun k02_archivoExactamenteIgualAlLimite() {
        val contenido = "a".repeat(MAX_BYTES_INICIO)
        val r = leerInicio(entrada(contenido))
        assertEquals(contenido, r.texto)
        assertEquals(MAX_BYTES_INICIO, r.bytesLeidos)
        assertFalse(r.truncado)
    }

    @Test
    fun k02_archivoMayorAlLimite_truncado() {
        val r = leerInicio(entrada("a".repeat(MAX_BYTES_INICIO + 1)))
        assertEquals("a".repeat(MAX_BYTES_INICIO), r.texto)
        assertEquals(MAX_BYTES_INICIO, r.bytesLeidos)
        assertTrue(r.truncado)
    }

    @Test
    fun k02_corteAMitadDeEnie_sinSimboloDeReemplazo() {
        // 8191 bytes ASCII + "ñ" (2 bytes: C3 B1): el corte en 8192 deja solo el byte C3.
        val contenido = "a".repeat(MAX_BYTES_INICIO - 1) + "ñ"
        val r = leerInicio(entrada(contenido))
        assertEquals("a".repeat(MAX_BYTES_INICIO - 1), r.texto)
        assertEquals(MAX_BYTES_INICIO - 1, r.bytesLeidos)
        assertTrue(r.truncado)
        assertFalse(r.texto.contains('\uFFFD'))
    }

    @Test
    fun k02_tildesCorrectas() {
        val contenido = "áéíóú ÁÉÍÓÚ ñ Ñ ü Ü ¿Qué tal? ¡Sí!"
        val r = leerInicio(entrada(contenido))
        assertEquals(contenido, r.texto)
        assertEquals(tamanoUtf8(contenido), r.bytesLeidos)
        assertFalse(r.truncado)
    }

    // ---- Casos adicionales ----

    @Test
    fun k02_corteAMitadDeEnie_conLimitePequeno() {
        // "abcdñ": a b c d C3 B1 -> con maxBytes = 5 el corte deja C3 suelto.
        val r = leerInicio(entrada("abcdñ"), maxBytes = 5)
        assertEquals("abcd", r.texto)
        assertEquals(4, r.bytesLeidos)
        assertTrue(r.truncado)
    }

    @Test
    fun k02_corteEnSecuenciaDeTresBytes() {
        // "€" = E2 82 AC. Se prueba el corte tras 1 y tras 2 bytes del carácter.
        val corteTras1 = leerInicio(entrada("abc€"), maxBytes = 4)
        assertEquals("abc", corteTras1.texto)
        assertEquals(3, corteTras1.bytesLeidos)
        assertTrue(corteTras1.truncado)

        val corteTras2 = leerInicio(entrada("abc€"), maxBytes = 5)
        assertEquals("abc", corteTras2.texto)
        assertEquals(3, corteTras2.bytesLeidos)
        assertTrue(corteTras2.truncado)
    }

    @Test
    fun k02_corteEnSecuenciaDeCuatroBytes() {
        // "😀" = F0 9F 98 80.
        val r = leerInicio(entrada("a😀"), maxBytes = 3)
        assertEquals("a", r.texto)
        assertEquals(1, r.bytesLeidos)
        assertTrue(r.truncado)
    }

    @Test
    fun k02_caracterCompletoJustoEnElLimite_seConserva() {
        // "abc€" ocupa 6 bytes; la "x" es el byte adicional que marca el truncado.
        val r = leerInicio(entrada("abc€x"), maxBytes = 6)
        assertEquals("abc€", r.texto)
        assertEquals(6, r.bytesLeidos)
        assertTrue(r.truncado)
    }

    @Test
    fun k02_bytesLeidosCoincideConElTextoCodificado() {
        val r = leerInicio(entrada("ñandú ".repeat(3000)))
        assertEquals(tamanoUtf8(r.texto), r.bytesLeidos)
        assertTrue(r.bytesLeidos <= MAX_BYTES_INICIO)
        assertTrue(r.truncado)
    }

    @Test
    fun k02_lecturaFragmentada_unByteACadaVez() {
        val bytes = ("a".repeat(100) + "ñ").toByteArray(Charsets.UTF_8)
        val fragmentada = object : InputStream() {
            private val interno = ByteArrayInputStream(bytes)
            override fun read(): Int = interno.read()
            override fun read(b: ByteArray, off: Int, len: Int): Int =
                interno.read(b, off, minOf(len, 1))
        }
        val r = leerInicio(fragmentada, maxBytes = 1000)
        assertEquals("a".repeat(100) + "ñ", r.texto)
        assertFalse(r.truncado)
    }

    @Test
    fun k02_noLeeMasDeMaxBytesMasUno() {
        var leidos = 0
        val infinita = object : InputStream() {
            override fun read(): Int {
                leidos++
                return 'a'.code
            }
        }
        val r = leerInicio(infinita)
        assertTrue(r.truncado)
        assertTrue("se leyeron $leidos bytes", leidos <= MAX_BYTES_INICIO + 1)
    }

    @Test
    fun k02_limiteInvalido_lanzaExcepcion() {
        var lanzada = false
        try {
            leerInicio(entrada("x"), maxBytes = -1)
        } catch (e: IllegalArgumentException) {
            lanzada = true
        }
        assertTrue(lanzada)
    }
}