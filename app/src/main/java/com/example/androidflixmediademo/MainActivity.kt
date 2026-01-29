package com.example.androidflixmediademo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF007AFF),
                    secondary = Color(0xFF5AC8FA),
                    background = Color(0xFFF2F2F7)
                )
            ) {
                AppNavigation()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Main : Screen("main", "Main")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Browse : Screen("browse", "Browse", Icons.Default.Search)
    object Accordion : Screen("accordion", "Accordion", Icons.Default.List)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object ProductDetail : Screen(
        "product_detail/{mpn}/{ean}/{distId}/{iso}/{brand}/{title}/{price}/{imageUrl}",
        "Product Detail"
    )}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val isLoggedIn by context.dataStore.data
        .map { it[IS_LOGGED_IN] ?: false }
        .collectAsState(initial = null)

    if (isLoggedIn == null) return

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) Screen.Main.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Main.route) {
            MainTabNavigator(onLogout = {
                scope.launch {
                    context.dataStore.edit { it[IS_LOGGED_IN] = false }
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            })
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainTabNavigator(onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(Screen.Home, Screen.Browse, Screen.Accordion, Screen.Settings)

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon ?: Icons.Default.Home, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        navController = navController,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }
                composable(Screen.Browse.route) {
                    val context = LocalContext.current

                    val preferenceManager = remember { ProductPreferenceManager(context) }

                    val browseViewModel: BrowseViewModel = viewModel(
                        factory = BrowseViewModelFactory(preferenceManager)
                    )

                    BrowseScreen(navController, browseViewModel)
                }
                composable(Screen.Accordion.route) { AccordionScreen() }
                composable(Screen.Settings.route) { SettingsScreen(onLogout) }

                composable(route = Screen.ProductDetail.route) { backStackEntry ->
                    val args = backStackEntry.arguments
                    val params = ProductViewArguments(
                        mpn = args?.getString("mpn") ?: "",
                        ean = args?.getString("ean") ?: "",
                        distId = args?.getString("distId") ?: "6",
                        isoCode = args?.getString("iso") ?: "",
                        flIsoCode = "",
                        brand = URLDecoder.decode(args?.getString("brand")) ?: "",
                        title = URLDecoder.decode(args?.getString("title")) ?: "",
                        price = args?.getString("price") ?: "",
                        currency = "USD",

                        imageUrl = args?.getString("imageUrl")?.let { URLDecoder.decode(it, "UTF-8") }
                    )

                    ProductDetailScreen(
                        params = params,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }
            }
        }
    }
}