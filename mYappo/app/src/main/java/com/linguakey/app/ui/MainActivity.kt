package com.linguakey.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.linguakey.app.data.security.SecureStorage

class MainActivity : ComponentActivity() {

    private lateinit var storage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = SecureStorage(applicationContext)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF10B981),
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B)
                )
            ) {
                MainAppScreen(
                    onOpenKeyboardSettings = {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(onOpenKeyboardSettings: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LinguaKey — Traductor por IA") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1E293B)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Translate, contentDescription = null) },
                    label = { Text("Traductor") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Keyboard, contentDescription = null) },
                    label = { Text("Teclado") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Ajustes") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> StandaloneTranslatorScreen()
                1 -> KeyboardActivationScreen(onOpenKeyboardSettings)
                2 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun StandaloneTranslatorScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Modo A: Traductor Independiente", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Escribe en español para obtener una traducción adaptada al perfil seleccionado.")
    }
}

@Composable
fun KeyboardActivationScreen(onOpenSettings: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Modo B: Activar Teclado LinguaKey", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text("1. Ve a Ajustes del sistema > Idiomas e introducción.")
        Text("2. Activa LinguaKey en la lista de teclados disponibles.")
        Text("3. Selecciónalo como teclado predeterminado al redactar.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenSettings) {
            Text("Abrir Ajustes de Teclado de Android")
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuración de Proveedores y Seguridad", style = MaterialTheme.typography.titleMedium)
    }
}
