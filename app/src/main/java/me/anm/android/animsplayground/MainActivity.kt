package me.anm.android.animsplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.anm.android.animsplayground.ui.theme.AnimsPlaygroundTheme
import me.anm.android.animsplayground.ui.theme.MainMenu
import me.anm.android.animsplayground.ui.starfield.StarfieldDemo

const val ROUTE_MENU = "menu"
const val ROUTE_STARFIELD = "starfield"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainActivityViewModel by viewModels()
        setContent {
            AnimsPlaygroundTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainActivityNav(viewModel)
                }
            }
        }
    }
}

class MainActivityViewModel: androidx.lifecycle.ViewModel() {
    // Example: var text by mutableStateOf("Hello, World!")
}

@Composable
fun MainActivityNav(viewModel: MainActivityViewModel) {
    val navController = rememberNavController()

    val gotoStarfield = {
        navController.navigate(ROUTE_STARFIELD)
    }

    NavHost(navController, startDestination = ROUTE_MENU) {
        composable(route = ROUTE_MENU) { MainMenu(gotoStarfield) }
        composable(route = ROUTE_STARFIELD) {StarfieldDemo() }
    }
}
