package com.example.mecanse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mecanse.ui.theme.MeCanseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeCanseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val context = LocalContext.current

                    // REGISTRO DE TODAS LAS RUTAS
                    NavHost(navController = navController, startDestination = Destinos.PAGINA1) {
                        // Inicio
                        composable(Destinos.PAGINA1) { home(navController) }

                        // Gestor de Horas
                        composable(Destinos.PRINCIPAL) { WorkHourTracker(context, navController) }

                        // Wiki
                        composable(Destinos.JUEGO_MENU) { PantallaJuegoMenu(navController) }

                        // NUEVA RUTA DE FOTOS (Evita el cierre de la app)
                        composable(Destinos.FOTOS) { PantallaFotos(navController) }

                        // Secciones de la Wiki
                        composable(Destinos.INTRODUCCION) { PantallaGenerica("Introducción", "Guía de inicio.") { navController.popBackStack() } }
                        composable(Destinos.SEDE_RECURSOS) { PantallaGenerica("Sede y Recursos", "Mejora tu base.") { navController.popBackStack() } }
                        composable(Destinos.HEROES_EJERCITO) { PantallaLista("Héroes", infoHeroes) { navController.popBackStack() } }
                        composable(Destinos.INVESTIGACIONES) { PantallaLista("Investigaciones", infoInvestigaciones) { navController.popBackStack() } }
                        composable(Destinos.FARMEO) { PantallaLista("Farmeo", infoFarmeo) { navController.popBackStack() } }
                        composable(Destinos.EVENTOS) { PantallaLista("Eventos", infoEventos) { navController.popBackStack() } }
                    }
                }
            }
        }
    }
}