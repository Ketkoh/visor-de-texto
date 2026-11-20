package io.github.cake.visordetexto

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.cake.visordetexto.core.DatosIntentEntrada
import io.github.cake.visordetexto.core.extraerUriDesdeIntent
import io.github.cake.visordetexto.core.leerInicio
import io.github.cake.visordetexto.ui.theme.VisorDeTextoTheme
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Solicitud de apertura proveniente de un Intent externo (K-08 / RF-13). */
private data class SolicitudExterna(
    val id: Int,
    val uri: Uri?,
)

class MainActivity : ComponentActivity() {
    private var contadorSolicitudes = 0
    private var solicitudExterna by mutableStateOf<SolicitudExterna?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            procesarIntentExterno(intent)
        }
        enableEdgeToEdge()
        setContent {
            VisorDeTextoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VisorApp(
                        solicitudExterna = solicitudExterna,
                        onSolicitudConsumida = { solicitudExterna = null },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarIntentExterno(intent)
    }

    private fun procesarIntentExterno(intent: Intent?) {
        val accion = intent?.action
        if (accion != Intent.ACTION_VIEW && accion != Intent.ACTION_SEND) return

        val streamUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
        val clipUri: Uri? = intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri

        val uriTexto = extraerUriDesdeIntent(
            DatosIntentEntrada(
                accion = accion,
                uriVista = intent.data?.toString(),
                uriEnvio = streamUri?.toString(),
                uriClip = clipUri?.toString(),
            )
        )
        val uriResuelta = uriTexto?.let { runCatching { Uri.parse(it) }.getOrNull() }
        contadorSolicitudes += 1
        solicitudExterna = SolicitudExterna(id = contadorSolicitudes, uri = uriResuelta)
    }
}

/** Contiene el estado y la lógica de apertura (K-02 + K-08). Solo lee; nunca escribe el archivo. */
@Composable
private fun VisorApp(
    solicitudExterna: SolicitudExterna?,
    onSolicitudConsumida: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var texto by rememberSaveable { mutableStateOf<String?>(null) }
    var hayError by rememberSaveable { mutableStateOf(false) }
    var ultimoIdProcesado by rememberSaveable { mutableIntStateOf(0) }

    fun abrirUri(uri: Uri?) {
        if (uri == null) {
            hayError = true
            texto = null
            return
        }
        alcance.launch {
            val resultado = try {
                withContext(Dispatchers.IO) {
                    val entrada = contexto.contentResolver.openInputStream(uri)
                        ?: throw IOException("No se obtuvo el flujo de entrada")
                    entrada.use { leerInicio(it) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (resultado == null) {
                hayError = true
                texto = null
            } else {
                hayError = false
                texto = resultado.texto
            }
        }
    }

    LaunchedEffect(solicitudExterna?.id) {
        val solicitud = solicitudExterna ?: return@LaunchedEffect
        if (solicitud.id != ultimoIdProcesado) {
            ultimoIdProcesado = solicitud.id
            abrirUri(solicitud.uri)
            onSolicitudConsumida()
        }
    }

    val selector = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        // Si se cancela el selector, uri es null y no pasa nada.
        if (uri != null) {
            abrirUri(uri)
        }
    }

    PantallaVisor(
        texto = texto,
        hayError = hayError,
        onAbrir = { selector.launch(arrayOf("*/*")) },
        modifier = modifier,
    )
}

@Composable
fun PantallaVisor(
    texto: String?,
    hayError: Boolean,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mostrado = when {
        hayError -> "No se pudo abrir el archivo"
        texto == null -> ""
        texto.isEmpty() -> "Archivo vacío"
        else -> texto
    }
    val desplazamiento = rememberScrollState()
    LaunchedEffect(mostrado) { desplazamiento.scrollTo(0) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onAbrir) { Text("Abrir archivo") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = mostrado,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(desplazamiento),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaVisorPreview() {
    VisorDeTextoTheme {
        PantallaVisor(texto = "Texto de ejemplo", hayError = false, onAbrir = {})
    }
}
