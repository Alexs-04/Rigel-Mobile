package atlix.rigelMobile

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

//TODO AQUÍ DEBE IR LA URL DE TU SERVIDOR LOCAL. DEBE SER UNA IP VÁLIDA EN TU RED LOCAL.
private const val HOME_PAGE_URL = "http://172.30.46.179:8080"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Envuelve tu contenido con tu tema de Material
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Llama a la función Composable que contiene el WebView
                    WebViewScreen(url = HOME_PAGE_URL)
                }
            }
        }
    }
}

/**
 * Función Composable que contiene y configura el Android WebView.
 *
 * @param url La URL a cargar.
 */
@Composable
fun WebViewScreen(url: String) {
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    AndroidView(
        // Modificador clave: respeta las ventanas del sistema
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()  // Agrega padding para la barra de estado
            .navigationBarsPadding(), // Agrega padding para la barra de navegación
        factory = { ctx ->
            WebView(ctx).apply {
                // Configuración existente...
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
                        request?.url?.toString()?.let { view.loadUrl(it) }
                        return true
                    }
                }
                webChromeClient = object : android.webkit.WebChromeClient() {}
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                webViewState.value = this
                clearCache(true)
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )

    DisposableEffect(key1 = Unit) {
        onDispose {
            webViewState.value?.let { wv ->
                try {
                    wv.stopLoading()
                    wv.clearHistory()
                    wv.clearCache(true)
                    wv.removeAllViews()
                    wv.destroy()
                } catch (e: Exception) {
                    Log.w("WebViewScreen", "Error destroying WebView", e)
                }
            }
            webViewState.value = null
        }
    }
}