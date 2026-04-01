package com.example.mecanse

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mecanse.ui.theme.MeCanseTheme

// Usamos FragmentActivity para soporte de Biometría
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Estado para controlar el acceso
        var isAuthenticated by mutableStateOf(false)

        // Ejecutar la autenticación al abrir la app
        autenticarConHuella { success ->
            isAuthenticated = success
        }

        setContent {
            MeCanseTheme {
                // Solo si la huella es correcta se muestra la App
                if (isAuthenticated) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val context = LocalContext.current

                        NavHost(
                            navController = navController,
                            startDestination = Destinos.PAGINA1
                        ) {
                            // Inicio
                            composable(Destinos.PAGINA1) { home(navController) }

                            // Gestor de Horas
                            composable(Destinos.PRINCIPAL) { WorkHourTracker(context, navController) }

                            // Wiki
                            composable(Destinos.JUEGO_MENU) { PantallaJuegoMenu(navController) }

                            // Ruta de Fotos
                            composable(Destinos.FOTOS) { PantallaFotos(navController) }

                            // Secciones de la Wiki
                            composable(Destinos.INTRODUCCION) {
                                PantallaGenerica("Introducción", "Guía de inicio.") { navController.popBackStack() }
                            }
                            composable(Destinos.SEDE_RECURSOS) {
                                PantallaGenerica("Sede y Recursos", "Mejora tu base.") { navController.popBackStack() }
                            }
                            composable(Destinos.HEROES_EJERCITO) {
                                PantallaLista("Héroes", infoHeroes) { navController.popBackStack() }
                            }
                            composable(Destinos.INVESTIGACIONES) {
                                PantallaLista("Investigaciones", infoInvestigaciones) { navController.popBackStack() }
                            }
                            composable(Destinos.FARMEO) {
                                PantallaLista("Farmeo", infoFarmeo) { navController.popBackStack() }
                            }
                            composable(Destinos.EVENTOS) {
                                PantallaLista("Eventos", infoEventos) { navController.popBackStack() }
                            }
                        }
                    }
                } else {
                    // Pantalla de bloqueo mientras se espera la huella
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Identidad requerida para ingresar",
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                    onResult(true) // Acceso concedido
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Si hay un error (ej. no hay huellas registradas), mostramos el mensaje
                    Toast.makeText(this@MainActivity, "Error: $errString", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@MainActivity, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                    // No llamamos a onResult(false) aquí para dejar que el usuario intente de nuevo
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Seguridad de Me Canse")
            .setSubtitle("Escanea tu huella para entrar")
            .setNegativeButtonText("Salir")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}