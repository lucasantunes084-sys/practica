package com.example.mecanse

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mecanse.ui.theme.MeCanseTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Estado para controlar el acceso del usuario
        var isAuthenticated by mutableStateOf(false)

        // Verificamos si el hardware permite biometría
        checkBiometricAvailability(this)

        setContent {
            MeCanseTheme {
                val currentContext = LocalContext.current

                // Función para llamar al sensor
                fun iniciarAutenticacion() {
                    autenticarConHuella { success ->
                        isAuthenticated = success
                    }
                }

                // Iniciar escaneo automáticamente al entrar
                LaunchedEffect(Unit) {
                    iniciarAutenticacion()
                }

                if (isAuthenticated) {
                    // --- APP PRINCIPAL (CONTENIDO PROTEGIDO) ---
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = Destinos.PAGINA1) {
                            composable(Destinos.PAGINA1) { home(navController) }
                            composable(Destinos.PRINCIPAL) { WorkHourTracker(currentContext, navController) }
                            composable(Destinos.JUEGO_MENU) { PantallaJuegoMenu(navController) }
                            composable(Destinos.FOTOS) { PantallaFotos(navController) }

                            // Secciones de la Wiki
                            composable(Destinos.INTRODUCCION) { PantallaGenerica("Introducción", "Guía.") { navController.popBackStack() } }
                            composable(Destinos.SEDE_RECURSOS) { PantallaGenerica("Sede", "Mejora.") { navController.popBackStack() } }
                            composable(Destinos.HEROES_EJERCITO) { PantallaLista("Héroes", infoHeroes) { navController.popBackStack() } }
                            composable(Destinos.INVESTIGACIONES) { PantallaLista("Investigaciones", infoInvestigaciones) { navController.popBackStack() } }
                            composable(Destinos.FARMEO) { PantallaLista("Farmeo", infoFarmeo) { navController.popBackStack() } }
                            composable(Destinos.EVENTOS) { PantallaLista("Eventos", infoEventos) { navController.popBackStack() } }
                        }
                    }
                } else {
                    // --- PANTALLA DE BLOQUEO CON VIDEO ---
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                        // 1. Capa inferior: El Video
                        VideoBackgroundExo()

                        // 2. Capa media: Oscurecer el video para que el texto sea legible
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

                        // 3. Capa superior: Textos y Botón
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "BIENVENIDO A ME CANSE",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Acceso restringido.\nEscanea tu huella para ingresar.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(onClick = { iniciarAutenticacion() }) {
                                Text(text = "REINTENTAR HUELLA")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun autenticarConHuella(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, "Error: $errString", Toast.LENGTH_SHORT).show()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@MainActivity, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Seguridad")
            .setSubtitle("Escanea tu sensor")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkBiometricAvailability(context: Context) {
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {}
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Toast.makeText(context, "No hay sensor", Toast.LENGTH_LONG).show()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Toast.makeText(context, "Sensor ocupado/no disponible", Toast.LENGTH_LONG).show()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Toast.makeText(context, "Registra una huella en ajustes", Toast.LENGTH_LONG).show()
            else -> {}
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoBackgroundExo() {
    val context = LocalContext.current

    // Configuramos ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Asegúrate de que el video esté en res/raw/animacion_bloqueo.mp4
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.animacion_bloqueo}")
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ALL // Para que sea infinito
            prepare()
            playWhenReady = true
        }
    }

    // Liberamos memoria al salir
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // El componente que renderiza el video
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // Oculta controles de video
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Escala para llenar pantalla (Crop)
                // Esto ayuda a que el video sea visible de inmediato:
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}