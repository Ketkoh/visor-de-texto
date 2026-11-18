package io.github.cake.visordetexto

import android.net.Uri
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.cake.visordetexto.core.leerInicio
import io.github.cake.visordetexto.ui.theme.VisorDeTextoTheme
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisorDeTextoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VisorApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/** Contiene el estado y la lógica de apertura (K-02). Solo lee; nunca escribe el archivo. */
@Composable
fun VisorApp(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var texto by rememberSaveable { mutableStateOf<String?>(null) }
    var hayError by rememberSaveable { mutableStateOf(false) }

    val selector = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        // Si se cancela el selector, uri es null y no pasa nada.
        if (uri != null) {
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