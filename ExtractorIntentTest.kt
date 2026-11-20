package io.github.cake.visordetexto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExtractorIntentTest {

    @Test
    fun k08_actionView_devuelveUriVista() {
        val datos = DatosIntentEntrada(
            accion = "android.intent.action.VIEW",
            uriVista = "content://com.android.externalstorage.documents/document/123",
            uriEnvio = null,
        )
        assertEquals(
            "content://com.android.externalstorage.documents/document/123",
            extraerUriDesdeIntent(datos),
        )
    }

    @Test
    fun k08_actionSend_devuelveUriEnvio() {
        val datos = DatosIntentEntrada(
            accion = "android.intent.action.SEND",
            uriVista = null,
            uriEnvio = "content://com.whatsapp.provider.media/item/456",
        )
        assertEquals(
            "content://com.whatsapp.provider.media/item/456",
            extraerUriDesdeIntent(datos),
        )
    }

    @Test
    fun k08_actionSendSinExtra_usaRespaldoClipData() {
        val datos = DatosIntentEntrada(
            accion = "android.intent.action.SEND",
            uriVista = null,
            uriEnvio = null,
            uriClip = "content://com.google.android.gm.sapi/attachment/789",
        )
        assertEquals(
            "content://com.google.android.gm.sapi/attachment/789",
            extraerUriDesdeIntent(datos),
        )
    }

    @Test
    fun k08_uriNulaOVacia_devuelveNull() {
        val datosNulos = DatosIntentEntrada(
            accion = "android.intent.action.VIEW",
            uriVista = null,
            uriEnvio = null,
        )
        val datosVacios = DatosIntentEntrada(
            accion = "android.intent.action.SEND",
            uriVista = null,
            uriEnvio = "   ",
        )
        assertNull(extraerUriDesdeIntent(datosNulos))
        assertNull(extraerUriDesdeIntent(datosVacios))
    }

    @Test
    fun k08_accionMainOIgnorada_devuelveNull() {
        val datosMain = DatosIntentEntrada(
            accion = "android.intent.action.MAIN",
            uriVista = "content://algo",
            uriEnvio = "content://algo",
        )
        assertNull(extraerUriDesdeIntent(datosMain))
    }
}
