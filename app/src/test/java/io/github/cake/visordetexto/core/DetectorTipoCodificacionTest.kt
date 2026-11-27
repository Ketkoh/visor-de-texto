package io.github.cake.visordetexto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class DetectorTipoCodificacionTest {

    // --- Criterio A2: Tabla determinista de casos (17 casos) ---

    @Test
    fun K05_tablaCasosDeterministas() {
        data class Caso(
            val nombre: String,
            val entrada: ByteArray,
            val esperado: ResultadoDeteccion,
        )

        val casos = listOf(
            // 1. Vacío
            Caso(
                nombre = "01_vacio",
                entrada = ByteArray(0),
                esperado = ResultadoDeteccion(TipoArchivo.VACIO, null, false, Confianza.ALTA),
            ),
            // 2. ASCII puro
            Caso(
                nombre = "02_ascii_puro",
                entrada = "Hola mundo 123\n\tLinea 2\r\n".toByteArray(Charsets.US_ASCII),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF8, true, Confianza.ALTA),
            ),
            // 3. UTF-8 con tildes y ñ
            Caso(
                nombre = "03_utf8_tildes_enie",
                entrada = "Canción de otoño en España: áéíóúüñ ÁÉÍÓÚÜÑ €".toByteArray(Charsets.UTF_8),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF8, false, Confianza.ALTA),
            ),
            // 4. UTF-8 con BOM
            Caso(
                nombre = "04_utf8_con_bom",
                entrada = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
                        "Texto con BOM".toByteArray(Charsets.UTF_8),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF8_CON_BOM, false, Confianza.ALTA),
            ),
            // 5. UTF-16LE con BOM
            Caso(
                nombre = "05_utf16le_con_bom",
                entrada = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) +
                        "Hola UTF-16LE".toByteArray(Charsets.UTF_16LE),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF16LE, false, Confianza.ALTA),
            ),
            // 6. UTF-16BE con BOM
            Caso(
                nombre = "06_utf16be_con_bom",
                entrada = byteArrayOf(0xFE.toByte(), 0xFF.toByte()) +
                        "Hola UTF-16BE".toByteArray(Charsets.UTF_16BE),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF16BE, false, Confianza.ALTA),
            ),
            // 7. UTF-16LE sin BOM con texto latino
            Caso(
                nombre = "07_utf16le_sin_bom",
                entrada = "Configuración del sistema en español".toByteArray(Charsets.UTF_16LE),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF16LE, false, Confianza.MEDIA),
            ),
            // 8. UTF-16BE sin BOM
            Caso(
                nombre = "08_utf16be_sin_bom",
                entrada = "Configuración del sistema en español".toByteArray(Charsets.UTF_16BE),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF16BE, false, Confianza.MEDIA),
            ),
            // 9. Bytes 0xF1 0xE9 sueltos (ñ, é en Windows-1252 / Latin-1)
            Caso(
                nombre = "09_windows1252_bytes_sueltos",
                entrada = byteArrayOf(0xF1.toByte(), 0xE9.toByte()),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.WINDOWS_1252, false, Confianza.MEDIA),
            ),
            // 10. Secuencia 0xC3 0x28 inválida en medio -> no UTF8 (Windows-1252)
            Caso(
                nombre = "10_utf8_invalido_en_medio",
                entrada = byteArrayOf(0x41, 0x42, 0xC3.toByte(), 0x28, 0x43, 0x44),
                esperado = ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.WINDOWS_1252, false, Confianza.MEDIA),
            ),
            // 11. Binario con NUL dispersos
            Caso(
                nombre = "11_binario_nul_dispersos",
                entrada = byteArrayOf(0x48, 0x6F, 0x6C, 0x61, 0x00, 0x4D, 0x75, 0x6E, 0x00, 0x64, 0x6F),
                esperado = ResultadoDeteccion(TipoArchivo.BINARIO, null, false, Confianza.ALTA),
            ),
            // 12. Binario con muchos bytes de control (> 10 %)
            Caso(
                nombre = "12_binario_controles",
                entrada = byteArrayOf(0x41, 0x42, 0x01, 0x02, 0x03, 0x43, 0x44, 0x45, 0x46, 0x47),
                esperado = ResultadoDeteccion(TipoArchivo.BINARIO, null, false, Confianza.ALTA),
            ),
            // 13. PDF
            Caso(
                nombre = "13_pdf",
                entrada = "%PDF-1.7\n%âãÏÓ\n".toByteArray(Charsets.ISO_8859_1),
                esperado = ResultadoDeteccion(TipoArchivo.PDF, null, false, Confianza.ALTA),
            ),
            // 14. ZIP
            Caso(
                nombre = "14_zip",
                entrada = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x00, 0x00),
                esperado = ResultadoDeteccion(TipoArchivo.ZIP, null, false, Confianza.ALTA),
            ),
            // 15. 7z
            Caso(
                nombre = "15_7z",
                entrada = byteArrayOf(0x37, 0x7A, 0xBC.toByte(), 0xAF.toByte(), 0x27, 0x1C, 0x00, 0x04),
                esperado = ResultadoDeteccion(TipoArchivo.SIETE_Z, null, false, Confianza.ALTA),
            ),
            // 16. RAR
            Caso(
                nombre = "16_rar",
                entrada = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00),
                esperado = ResultadoDeteccion(TipoArchivo.RAR, null, false, Confianza.ALTA),
            ),
            // 17. GZIP
            Caso(
                nombre = "17_gzip",
                entrada = byteArrayOf(0x1F, 0x8B.toByte(), 0x08, 0x00, 0x00, 0x00, 0x00, 0x00),
                esperado = ResultadoDeteccion(TipoArchivo.GZIP, null, false, Confianza.ALTA),
            ),
        )

        for (caso in casos) {
            val obtenido = detectar(caso.entrada)
            assertEquals("Falló caso ${caso.nombre}", caso.esperado, obtenido)
        }
    }

    // --- Casos borde adicionales de R3, R6 y R7 ---

    @Test
    fun K05_utf8RechazaSobrelargasSustitutosYFueraDeRango() {
        val sobrelarga = byteArrayOf(0x41, 0xC0.toByte(), 0xAF.toByte(), 0x42)
        assertEquals(Codificacion.WINDOWS_1252, detectar(sobrelarga).codificacion)

        val sustituto = byteArrayOf(0x41, 0xED.toByte(), 0xA0.toByte(), 0x80.toByte(), 0x42)
        assertEquals(Codificacion.WINDOWS_1252, detectar(sustituto).codificacion)

        val mayorMax = byteArrayOf(0x41, 0xF4.toByte(), 0x90.toByte(), 0x80.toByte(), 0x80.toByte(), 0x42)
        assertEquals(Codificacion.WINDOWS_1252, detectar(mayorMax).codificacion)
    }

    @Test
    fun K05_controlesPermitidosNoMarcanBinario() {
        val textoConControlesPermitidos = "\t\n\r\u000C\t\n\r\u000CHola".toByteArray(Charsets.UTF_8)
        val res = detectar(textoConControlesPermitidos)
        assertEquals(TipoArchivo.TEXTO, res.tipo)
        assertEquals(Codificacion.UTF8, res.codificacion)
        assertTrue(res.soloAscii)
    }

    @Test
    fun K05_bomUtf16ConCerosPosterioresNoEsCasoEspecial() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x00, 0x41, 0x00)
        val res = detectar(bytes)
        assertEquals(TipoArchivo.TEXTO, res.tipo)
        assertEquals(Codificacion.UTF16LE, res.codificacion)
        assertEquals(Confianza.ALTA, res.confianza)
    }

    // --- Criterio A3: Propiedad de corte en UTF-8 ---

    @Test
    fun K05_propiedadDeCorteUtf8EnCadaPosicion() {
        val texto = "Texto UTF-8 con tildes áéíóúüñ ÁÉÍÓÚÜÑ, euro € y emoji 😀 fin."
        val completo = texto.toByteArray(Charsets.UTF_8)

        for (corte in 1..completo.size) {
            val prefijo = completo.copyOf(corte)
            val res = detectar(prefijo)
            assertEquals("En corte=$corte de ${completo.size}, debe ser TEXTO", TipoArchivo.TEXTO, res.tipo)
            assertEquals("En corte=$corte de ${completo.size}, debe ser UTF8", Codificacion.UTF8, res.codificacion)
            assertEquals(Confianza.ALTA, res.confianza)
        }
    }

    // --- Criterio A7: Rendimiento informativo sobre 64 KiB ---

    @Test
    fun K05_medicionTiempo64KiB() {
        val patron = "Línea de prueba con caracteres UTF-8: áéíóúüñ ÁÉÍÓÚÜÑ y símbolos €.\n".toByteArray(Charsets.UTF_8)
        val muestra64K = ByteArray(MAX_BYTES_DETECCION) { i -> patron[i % patron.size] }

        repeat(20) { detectar(muestra64K) }

        val iteraciones = 100
        val inicioNs = System.nanoTime()
        repeat(iteraciones) {
            val r = detectar(muestra64K)
            assertEquals(Codificacion.UTF8, r.codificacion)
        }
        val duracionTotalNs = System.nanoTime() - inicioNs
        val promedioUs = (duracionTotalNs / iteraciones) / 1_000.0
        println(String.format(Locale.ROOT, "[K-05] Tiempo promedio 64 KiB (%d iter): %.2f us", iteraciones, promedioUs))
    }

    // --- Criterio A5: Pruebas sobre archivos del corpus (activadas con VISOR_CORPUS) ---

    @Test
    fun K05_corpus_archivosExistentes() {
        val carpeta = ApoyoPruebas.directorioCorpus()
        assumeTrue("VISOR_CORPUS no está definida: pruebas de corpus omitidas", carpeta != null)

        val archivosEsperados = mapOf(
            "vacio.txt" to ResultadoDeteccion(TipoArchivo.VACIO, null, false, Confianza.ALTA),
            "saltos_mixtos.txt" to ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF8, true, Confianza.ALTA),
            "una_linea_10mb.txt" to ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF8, true, Confianza.ALTA),
            "tildes_utf8.txt" to ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF8, false, Confianza.ALTA),
            "grande_utf8.txt" to ResultadoDeteccion(TipoArchivo.TEXTO, Codificacion.UTF8, true, Confianza.ALTA),
        )

        val usados = mutableListOf<String>()
        for ((nombre, esperado) in archivosEsperados) {
            val archivo = File(carpeta, nombre)
            if (!archivo.isFile) continue
            usados.add(nombre)

            val muestra = leerPrimerosBytes(archivo, MAX_BYTES_DETECCION)
            val obtenido = detectar(muestra)
            assertEquals("Falló archivo del corpus: $nombre", esperado, obtenido)
        }

        val otrosArchivos = carpeta!!.listFiles()
            ?.filter { it.isFile && it.name !in archivosEsperados.keys }
            ?.map { it.name }
            ?.sorted()
            .orEmpty()

        println("[K-05] Archivos del corpus verificados: $usados")
        println("[K-05] Otros archivos presentes en corpus (no asertados): $otrosArchivos")
        assertTrue("Debe existir al menos un archivo conocido en VISOR_CORPUS", usados.isNotEmpty())
    }

    private fun leerPrimerosBytes(archivo: File, maxBytes: Int): ByteArray {
        FuenteArchivo(archivo).use { fuente ->
            val aLeer = minOf(fuente.tamano, maxBytes.toLong()).toInt()
            if (aLeer == 0) return ByteArray(0)
            val buf = ByteArray(aLeer)
            var total = 0
            while (total < aLeer) {
                val n = fuente.leer(total.toLong(), buf, aLeer - total)
                if (n <= 0) break
                total += n
            }
            return if (total == aLeer) buf else buf.copyOf(total)
        }
    }
}
