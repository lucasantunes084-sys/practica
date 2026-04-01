package com.example.mecanse.lastz

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

annotation class AppNavigation
@Composable
fun AppNavigation(navController: NavHostController) { // <--- Asegúrate de que tenga esto
    NavHost(navController = navController, startDestination = "tu_ruta") {
        // ... tus rutas de la Wiki y Horas
    }
}