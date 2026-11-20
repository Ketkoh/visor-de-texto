package io.github.cake.visordetexto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Locale

/**
 * Medición de K-04a (criterios A3 y A4). Solo se ejecuta si VISOR_CORPUS está definida.
 * La salida queda en la consola de la ejecución (o en el campo system-out del informe XML de Gradle).
 */
class MedicionTest {
    @get:Rule
    val carpeta = TemporaryFolder()

    private fun ms(nanos: Long) = String.format(Locale.ROOT, "%.2f ms", nanos / 1e6)
    private fun mib(bytes: Long) = String.format(Locale.ROOT, "%.1f MiB", bytes / 1048576.0)

    private fun memoriaUsada(): Long {
        repeat(3) {
            System.gc()
            Thread.sleep(50)
        }
        val r = Runtime.getRuntime()
        return r.totalMemory() - r.freeMemory()
    }

    private fun primeraVentanaNanos(fuente: FuenteBytes): Long {
        val lector = LectorDeLineas(fuente, IndiceLineas(fuente))
        val t0 = System.nanoTime()
        val lineas = lector.leerLineas(1, 50)
        val dt = System.nanoTime() - t0
        assertEquals(50, lineas.size)
        return dt
    }

    private fun mediana(fuente: FuenteBytes, repeticiones: Int = 9): Long =
        (1..repeticiones).map { primeraVentanaNanos(fuente) }.sorted()[repeticiones / 2]

    @Test
    fun K04a_medicion_grandeUtf8() {
        assumeTrue("VISOR_CORPUS no está definida: medición omitida", ApoyoPruebas.directorioCorpus() != null)
        val pequeno = carpeta.newFile().also { archivo ->
            val texto = StringBuilder()
            var n = 1
            while (texto.length < 1024 * 1024) texto.append(String.format(Locale.ROOT, "L%012d|línea de prueba con texto de relleno\n", n++))
            archivo.writeBytes(texto.toString().toByteArray(Charsets.UTF_8))
        }

        FuenteArchivo(ApoyoPruebas.archivoCorpus("grande_utf8.txt")).use { grande ->
            FuenteArchivo(pequeno).use { chico ->
                val indice = IndiceLineas(grande)
                val lector = LectorDeLineas(grande, indice)

                // 1) Primera ventana de grande, en frío y antes de construir el índice.
                val t0 = System.nanoTime()
                val ventana = lector.leerLineas(1, 50)
                val primeraGrande = System.nanoTime() - t0
                assertEquals(50, ventana.size)

                // 2) Primera ventana del archivo de 1 MB, también antes de construir su índice.
                val primeraChico = primeraVentanaNanos(chico)

                // 3) Construcción del índice y memoria.
                val antes = memoriaUsada()
                val t1 = System.nanoTime()
                indice.construir()
                val construir = System.nanoTime() - t1
                val despues = memoriaUsada()

                // 4) Lectura en el medio.
                val t2 = System.nanoTime()
                val medio = lector.leerLineas(5_000_000, 50)
                val lecturaMedio = System.nanoTime() - t2
                assertEquals(50, medio.size)

                // 5) Medianas (JIT ya caliente) para el criterio A3.
                val medianaGrande = mediana(grande)
                val medianaChico = mediana(chico)

                println("[K-04a medición] grande_utf8.txt = ${mib(grande.tamano)}, ${indice.totalLineas} líneas")
                println("[K-04a] primera ventana (1, 50) antes del índice, en frío: grande ${ms(primeraGrande)} | 1 MB ${ms(primeraChico)}")
                println("[K-04a] primera ventana, mediana de 9: grande ${ms(medianaGrande)} | 1 MB ${ms(medianaChico)}")
                println("[K-04a] construir índice: ${ms(construir)}")
                println("[K-04a] desplazamientos guardados: ${indice.desplazamientosGuardados}")
                println("[K-04a] memoria usada antes: ${mib(antes)} | después: ${mib(despues)} | diferencia: ${mib(despues - antes)}")
                println("[K-04a] leerLineas(5_000_000, 50): ${ms(lecturaMedio)}")

                assertEquals(10_000_000L, indice.totalLineas)
                // A4: a lo sumo 10.001 desplazamientos y memoria sin relación con el tamaño del archivo.
                assertTrue(indice.desplazamientosGuardados <= 10_001)
                assertTrue("la memoria creció ${mib(despues - antes)}", despues - antes < 8L * 1024 * 1024)
                // A3: como máximo el doble; se añade 1 ms de holgura para el ruido de medir fracciones de milisegundo.
                assertTrue(
                    "A3: grande ${ms(medianaGrande)} vs 1 MB ${ms(medianaChico)}",
                    medianaGrande <= 2 * medianaChico + 1_000_000L,
                )
            }
        }
    }
}
