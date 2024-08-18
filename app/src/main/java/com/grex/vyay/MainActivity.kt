package com.grex.vyay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.grex.vyay.ui.theme.ChampagnePink
import com.grex.vyay.ui.theme.ColumbiaBlue
import com.grex.vyay.ui.theme.LightCyan
import com.grex.vyay.ui.theme.VyayTheme


class MainActivity : ComponentActivity() {
    lateinit var smsAnalysisService: SmsAnalysisService
    lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smsAnalysisService = SmsAnalysisService.getInstance()
        settingsViewModel = SettingsViewModel(smsAnalysisService.appDao)

        setContent {
            VyayTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(this, settingsViewModel)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        smsAnalysisService.stopAnalysis()
    }
}

@Composable
fun AppNavigation(activity: MainActivity, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val userPreferences = UserPreferences(LocalContext.current)
    val startDestination = if (userPreferences.getUserName().isEmpty()) Screen.Onboarding.route else Screen.Splash.route

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold (
        topBar = {
            if ((currentRoute != Screen.Onboarding.route) && (currentRoute != Screen.Splash.route)) {
                TopMenuBar(navController)
            }
         },
        bottomBar = {
            if ((currentRoute != Screen.Onboarding.route) && (currentRoute != Screen.Splash.route)) {
                FooterNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onSetupComplete = {
                        navController.navigate(Screen.Splash.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    saveUserName = { name -> userPreferences.saveUserName(name) },
                    setNotificationsEnabled = { enabled -> userPreferences.setNotificationsEnabled(enabled) }
                )
            }
            composable(Screen.Splash.route) {
                SplashScreen(
                    smsAnalysisService = activity.smsAnalysisService,
                    onLoadingComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(activity, innerPadding)
            }
            composable(Screen.Reports.route) {
                ReportsScreen(activity, innerPadding)
            }
            composable(Screen.Statements.route) {
                StatementsScreen(activity, innerPadding)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    activity,
                    innerPadding,
                    settingsViewModel
                )
            }
        }
    }
}


// Previews --------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VyayTheme {
        Greeting("Android")
    }
}
@Preview(showBackground = false)
@Composable
fun PieChartPreview() {
    VyayTheme {
        val data = listOf(
            PieChartData(value = 0.4f, color = ChampagnePink),
            PieChartData(value = 0.6f, color = LightCyan),
            PieChartData(value = 0.5f, color = ColumbiaBlue)
        )
        PieChart(data = data, modifier = Modifier.height(200.dp))
    }
}
