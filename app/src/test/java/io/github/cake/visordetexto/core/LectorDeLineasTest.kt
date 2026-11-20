package io.github.cake.visordetexto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Random

/** Pruebas de K-04a con archivos temporales creados por cada prueba (siempre se ejecutan). */
class LectorDeLineasTest {
    @get:Rule
    val carpeta = TemporaryFolder()

    private fun crear(contenido: ByteArray): File = carpeta.newFile().also { it.writeBytes(contenido) }
    private fun crear(texto: String): File = crear(texto.toByteArray(Charsets.UTF_8))
    private fun textos(lineas: List<Linea>) = lineas.map { it.texto }

    @Test
    fun K04a_archivoVacio_tieneCeroLineas() {
        FuenteArchivo(crear(ByteArray(0))).use { fuente ->
            val indice = IndiceLineas(fuente)
            indice.construir()
            assertTrue(indice.completo)
            assertEquals(0L, indice.totalLineas)
            assertEquals(0L, indice.lineasConocidas)
            assertEquals(0, indice.desplazamientosGuardados)
            assertTrue(LectorDeLineas(fuente, indice).leerLineas(1, 10).isEmpty())
        }
    }

    @Test
    fun K04a_unaLineaSinTerminador() {
        FuenteArchivo(crear("hola")).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente)
            assertEquals(1L, indice.totalLineas)
            assertEquals(
                listOf(Linea(1, "hola", false)),
                LectorDeLineas(fuente, indice).leerLineas(1, 10),
            )
        }
    }

    @Test
    fun K04a_terminadorFinalNoCreaLineaExtra() {
        val casos = mapOf("a\nb\n" to 2L, "a\r\n" to 1L, "a\r" to 1L, "\n" to 1L, "\r\n" to 1L, "\n\n" to 2L, "a\n\n" to 2L)
        for ((contenido, esperadas) in casos) {
            FuenteArchivo(crear(contenido)).use { fuente ->
                val indice = ApoyoPruebas.indiceCompleto(fuente, bloque = 2)
                assertEquals("total de ${contenido.toByteArray().toList()}", esperadas, indice.totalLineas)
                val leidas = LectorDeLineas(fuente, indice).leerLineas(1, 100)
                assertEquals("lectura de ${contenido.toByteArray().toList()}", esperadas.toInt(), leidas.size)
            }
        }
    }

    @Test
    fun K04a_mezclaDeTerminadores_30Lineas() {
        val terminadores = listOf("\n", "\r\n", "\r")
        val contenido = StringBuilder()
        for (n in 1..30) {
            contenido.append("línea ").append(n)
            if (n < 30) contenido.append(terminadores[n % 3])
        }
        val esperadas = (1..30).map { "línea $it" }
        val archivo = crear(contenido.toString())
        for ((intervalo, bloque) in listOf(1 to 1, 3 to 5, 7 to 16, 1000 to 1024 * 1024)) {
            FuenteArchivo(archivo).use { fuente ->
                val indice = ApoyoPruebas.indiceCompleto(fuente, intervalo, bloque)
                assertEquals(30L, indice.totalLineas)
                val lector = LectorDeLineas(fuente, indice)
                assertEquals(esperadas, textos(lector.leerLineas(1, 100)))
                assertEquals((1L..30L).toList(), lector.leerLineas(1, 100).map { it.numero })
                assertEquals(esperadas.subList(9, 14), textos(lector.leerLineas(10, 5)))
            }
        }
    }

    @Test
    fun K04a_crLfPartidoEnElLimiteDeUnBloque_cuentaComoUnTerminador() {
        // Bloque de 16 bytes: el \r es el byte 15 (último del bloque 1) y el \n el byte 16 (primero del bloque 2).
        val contenido = "123456789012345\r\nABC"
        FuenteArchivo(crear(contenido)).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente, intervalo = 1, bloque = 16)
            assertEquals(2L, indice.totalLineas)
            assertEquals(PuntoIndice(1, 0), indice.desplazamientoCercano(1))
            assertEquals(PuntoIndice(2, 17), indice.desplazamientoCercano(2))
            val lector = LectorDeLineas(fuente, indice)
            lector.bloqueLectura = 16
            assertEquals(listOf("123456789012345", "ABC"), textos(lector.leerLineas(1, 10)))
            assertEquals(listOf("ABC"), textos(lector.leerLineas(2, 10)))
        }
    }

    @Test
    fun K04a_crFinalDeBloqueSeguidoDeOtraCosa_sonDosTerminadores() {
        // \r al final del bloque y \r (no \n) después: son dos terminadores.
        FuenteArchivo(crear("123456789012345\r\rZ")).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente, intervalo = 1, bloque = 16)
            assertEquals(3L, indice.totalLineas)
            val lector = LectorDeLineas(fuente, indice)
            lector.bloqueLectura = 16
            assertEquals(listOf("123456789012345", "", "Z"), textos(lector.leerLineas(1, 10)))
        }
    }

    @Test
    fun K04a_ennePartidaEnElLimiteDeUnBloque_sedecodificaEntera() {
        // "ñ" = C3 B1, en los bytes 15 y 16: queda partida con bloques de 16 bytes.
        val contenido = "a".repeat(15) + "ñ" + "\nfin"
        FuenteArchivo(crear(contenido)).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente, intervalo = 1, bloque = 16)
            val lector = LectorDeLineas(fuente, indice)
            lector.bloqueLectura = 16
            assertEquals(listOf("a".repeat(15) + "ñ", "fin"), textos(lector.leerLineas(1, 10)))
        }
    }

    @Test
    fun K04a_bytesInvalidos_seDecodificanComoU_FFFD() {
        val bytes = byteArrayOf('a'.code.toByte(), 0xFF.toByte(), 'b'.code.toByte(), '\n'.code.toByte(), 0xC3.toByte(), 'c'.code.toByte())
        FuenteArchivo(crear(bytes)).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente)
            assertEquals(listOf("a\uFFFDb", "\uFFFDc"), textos(LectorDeLineas(fuente, indice).leerLineas(1, 10)))
        }
    }

    @Test
    fun K04a_lineaMasLargaQueElMaximo_seTruncaSinCargarlaCompleta() {
        val largo = 5 * 1024 * 1024
        val bytes = ByteArray(largo + 4).also {
            java.util.Arrays.fill(it, 0, largo, 'x'.code.toByte())
            "\nfin".toByteArray().copyInto(it, largo)
        }
        FuenteArchivo(crear(bytes)).use { base ->
            val fuente = ApoyoPruebas.FuenteContadora(base)
            val indice = ApoyoPruebas.indiceCompleto(fuente, bloque = 1024 * 1024)
            assertEquals(2L, indice.totalLineas)
            val lector = LectorDeLineas(fuente, indice, maxCaracteresPorLinea = 100)
            val lineas = lector.leerLineas(1, 2)
            assertEquals(Linea(1, "x".repeat(100), true), lineas[0])
            assertEquals(Linea(2, "fin", false), lineas[1])
            assertTrue("pidió ${fuente.maxSolicitado} bytes de una vez", fuente.maxSolicitado <= 1024 * 1024)
        }
    }

    @Test
    fun K04a_truncado_respetaElLimiteExactoYLosCaracteresMultibyte() {
        val contenido = "a".repeat(10) + "\n" + "a".repeat(11) + "\n" + "ñ".repeat(50) + "\n" + "😀".repeat(10)
        FuenteArchivo(crear(contenido)).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente)
            val lector = LectorDeLineas(fuente, indice, maxCaracteresPorLinea = 10)
            val lineas = lector.leerLineas(1, 10)
            assertEquals(Linea(1, "a".repeat(10), false), lineas[0]) // justo en el máximo: no se trunca
            assertEquals(Linea(2, "a".repeat(10), true), lineas[1])
            assertEquals(Linea(3, "ñ".repeat(10), true), lineas[2])
            // Máximo 10 chars = 5 emojis de 2 chars; con máximo 5 no se parte el par sustituto.
            assertEquals(Linea(4, "😀".repeat(5), true), lineas[3])
            val impar = LectorDeLineas(fuente, indice, maxCaracteresPorLinea = 5).leerLineas(4, 1)[0]
            assertEquals(Linea(4, "😀".repeat(2), true), impar)
        }
    }

    @Test
    fun K04a_indiceConIntervalo3_lecturaEnElMedio() {
        val contenido = (1..20).joinToString("\n") { "fila $it" } + "\n"
        FuenteArchivo(crear(contenido)).use { fuente ->
            val indice = ApoyoPruebas.indiceCompleto(fuente, intervalo = 3)
            assertEquals(20L, indice.totalLineas)
            assertEquals(7, indice.desplazamientosGuardados) // líneas 1, 4, 7, 10, 13, 16 y 19
            val desplazamientoLinea7 = (1..6).sumOf { "fila $it\n".length }.toLong()
            assertEquals(PuntoIndice(7, desplazamientoLinea7), indice.desplazamientoCercano(8))
            assertEquals(PuntoIndice(10, (1..9).sumOf { "fila $it\n".length }.toLong()), indice.desplazamientoCercano(10))
            assertEquals(PuntoIndice(19, (1..18).sumOf { "fila $it\n".length }.toLong()), indice.desplazamientoCercano(500))
            val lector = LectorDeLineas(fuente, indice)
            assertEquals((8..11).map { "fila $it" }, textos(lector.leerLineas(8, 4)))
            assertEquals((8..11).map { it.toLong() }, lector.leerLineas(8, 4).map { it.numero })
            assertEquals(listOf("fila 19", "fila 20"), textos(lector.leerLineas(19, 5)))
            assertTrue(lector.leerLineas(21, 1).isEmpty())
        }
    }

    @Test
    fun K04a_cancelarLaConstruccion_dejaLoIndexadoUtilizable() {
        val contenido = (1..1000).joinToString("\n") { "l$it" } + "\n"
        FuenteArchivo(crear(contenido)).use { fuente ->
            val indice = IndiceLineas(fuente, intervalo = 3, bloque = 16)
            var consultas = 0
            val progresos = ArrayList<Long>()
            indice.construir(cancelado = { ++consultas > 3 }, progreso = { progresos.add(it) })
            assertFalse(indice.completo)
            assertNull(indice.totalLineas)
            assertEquals(listOf(16L, 32L, 48L), progresos)
            assertTrue(indice.lineasConocidas in 1..999)

            val lector = LectorDeLineas(fuente, indice)
            assertEquals(listOf("l1", "l2"), textos(lector.leerLineas(1, 2)))
            val conocidas = indice.lineasConocidas
            assertEquals(listOf("l$conocidas"), textos(lector.leerLineas(conocidas, 1)))
            assertTrue(lector.leerLineas(1000, 1).isEmpty()) // aún no indexada

            indice.construir() // volver a construir reinicia y completa
            assertTrue(indice.completo)
            assertEquals(1000L, indice.totalLineas)
            assertEquals(listOf("l1000"), textos(lector.leerLineas(1000, 1)))
        }
    }

    @Test
    fun K04a_sinConstruirElIndice_laLinea1SeLee() {
        FuenteArchivo(crear("uno\ndos\ntres\n")).use { fuente ->
            val indice = IndiceLineas(fuente)
            val lector = LectorDeLineas(fuente, indice)
            assertEquals(listOf("uno", "dos"), textos(lector.leerLineas(1, 2)))
            assertTrue(lector.leerLineas(2, 1).isEmpty())
            assertEquals(PuntoIndice(1, 0), indice.desplazamientoCercano(1))
        }
    }

    @Test
    fun K04a_aleatorio_coincideConLaImplementacionDeReferencia() {
        val trozos = listOf("a", "b", " ", "ñ", "€", "😀", "Z9")
        val terminadores = listOf("\n", "\r\n", "\r")
        for (semilla in 1L..5L) {
            val azar = Random(semilla)
            val texto = StringBuilder()
            val total = 150 + azar.nextInt(100)
            for (n in 1..total) {
                repeat(azar.nextInt(10)) { texto.append(trozos[azar.nextInt(trozos.size)]) }
                if (n < total || azar.nextBoolean()) texto.append(terminadores[azar.nextInt(3)])
            }
            val esperado = ApoyoPruebas.referencia(texto.toString())
            FuenteArchivo(crear(texto.toString())).use { fuente ->
                for (intervalo in listOf(1, 2, 3, 7)) {
                    for (bloque in listOf(1, 2, 3, 16, 1024 * 1024)) {
                        val indice = ApoyoPruebas.indiceCompleto(fuente, intervalo, bloque)
                        val etiqueta = "semilla=$semilla intervalo=$intervalo bloque=$bloque"
                        assertEquals(etiqueta, esperado.size.toLong(), indice.totalLineas)
                        val guardados = if (esperado.isEmpty()) 0 else (esperado.size - 1) / intervalo + 1
                        assertEquals(etiqueta, guardados, indice.desplazamientosGuardados)
                        for (lectura in listOf(1, 3, 16, 64 * 1024)) {
                            val lector = LectorDeLineas(fuente, indice)
                            lector.bloqueLectura = lectura
                            assertEquals("$etiqueta lectura=$lectura", esperado, textos(lector.leerLineas(1, esperado.size + 5)))
                            val desde = 1 + azar.nextInt(esperado.size)
                            val cantidad = 1 + azar.nextInt(20)
                            assertEquals(
                                "$etiqueta lectura=$lectura desde=$desde cantidad=$cantidad",
                                esperado.drop(desde - 1).take(cantidad),
                                textos(lector.leerLineas(desde.toLong(), cantidad)),
                            )
                        }
                    }
                }
            }
        }
    }
}
