package udeg.cuvalles.r5navegacionnavhost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme()
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AplicacionNavegacion()
                }
            }
        }
    }
}

private object Rutas {
    const val INICIO = "inicio"
    const val PERFIL = "perfil/{nombre}/{edad}"
    const val CONFIRMACION = "confirmacion"

    fun perfil(nombre: String, edad: String): String {
        val nombreSeguro = URLEncoder.encode(nombre, StandardCharsets.UTF_8.toString())
        return "perfil/$nombreSeguro/$edad"
    }
}

@Composable
fun AplicacionNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.INICIO
    ) {
        composable(Rutas.INICIO) {
            PantallaInicio(
                alContinuar = { nombre, edad ->
                    navController.navigate(Rutas.perfil(nombre, edad))
                }
            )
        }

        composable(
            route = Rutas.PERFIL,
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("edad") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre").orEmpty()
            val edad = backStackEntry.arguments?.getString("edad").orEmpty()

            PantallaPerfil(
                nombre = nombre,
                edad = edad,
                alConfirmar = {
                    navController.navigate(Rutas.CONFIRMACION)
                },
                alRegresar = { navController.popBackStack() }
            )
        }

        composable(Rutas.CONFIRMACION) {
            PantallaConfirmacion(
                alVolverInicio = {
                    navController.navigate(Rutas.INICIO) {
                        popUpTo(Rutas.INICIO) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun PantallaInicio(alContinuar: (String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    ColumnaBase(titulo = "Bienvenido", subtitulo = "Pantalla 1 · Inicio") {
        Text(
            text = "Ingresa tus datos para continuar",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; error = "" },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = edad,
            onValueChange = { edad = it.filter(Char::isDigit).take(3); error = "" },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (error.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                when {
                    nombre.isBlank() -> error = "Escribe tu nombre para continuar."
                    edad.isBlank() || edad.toIntOrNull() == null || edad.toInt() !in 1..120 ->
                        error = "Escribe una edad válida (1 a 120)."
                    else -> alContinuar(nombre.trim(), edad)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar al perfil")
        }
    }
}

@Composable
private fun PantallaPerfil(
    nombre: String,
    edad: String,
    alConfirmar: () -> Unit,
    alRegresar: () -> Unit
) {
    ColumnaBase(titulo = "Tu perfil", subtitulo = "Pantalla 2 · Argumentos de navegación") {
        Text(
            "Estos datos llegaron desde la pantalla de inicio mediante la ruta de navegación.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Nombre", fontWeight = FontWeight.SemiBold)
                Text(nombre, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Edad", fontWeight = FontWeight.SemiBold)
                Text("$edad años", fontSize = 22.sp)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = alConfirmar, modifier = Modifier.fillMaxWidth()) {
            Text("Confirmar datos")
        }
        Button(onClick = alRegresar, modifier = Modifier.fillMaxWidth()) {
            Text("Regresar")
        }
    }
}

@Composable
private fun PantallaConfirmacion(alVolverInicio: () -> Unit) {
    ColumnaBase(titulo = "¡Listo!", subtitulo = "Pantalla 3 · Confirmación") {
        Text("Tus datos se recibieron correctamente.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Has completado el flujo de navegación con NavHost.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = alVolverInicio, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al inicio")
        }
    }
}

@Composable
private fun ColumnaBase(
    titulo: String,
    subtitulo: String,
    contenido: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            titulo,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            subtitulo,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        contenido()
    }
}
