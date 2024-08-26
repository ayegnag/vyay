package com.grex.vyay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.grex.vyay.ui.theme.CustomColors
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CustomColors.backgroundPrimaryBottom
                ) {
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
    val startDestination = if (userPreferences.getUserName()
            .isEmpty()
    ) Screen.Onboarding.route else Screen.Splash.route

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var isTransactionRecordUpdated by remember { mutableStateOf<Boolean>(false) }
    var showFab by remember { mutableStateOf(true) }

    fun navigateToTransactionDetails(transaction: TransactionRecord) {
        val route = Screen.TransactionDetails.createRoute(transaction.id, transaction.isManual)
        navController.navigate(route)
    }

    fun navigateToMonthlyStatement(yearMonth: String) {
        val route = Screen.Statements.createRoute(yearMonth)
        navController.navigate(route)
    }

//    val screensWithoutTopBar = listOf(Screen.Onboarding.route, Screen.Splash.route, Screen.TransactionDetails.route)
    val screensWithoutNavBar =
        listOf(
            Screen.Onboarding.route,
            Screen.Splash.route,
            Screen.TransactionDetails.route,
            Screen.AddTransaction.route
        )

    Scaffold(
//        topBar = {
//            if (currentRoute?.let { screensWithoutTopBar.contains(it) } == false) {
//                TopMenuBar(navController)
//            }
//        },
        bottomBar = {
            if (currentRoute?.let { screensWithoutNavBar.contains(it) } == false) {
                FooterNavBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddTransaction.route) },
                    containerColor = CustomColors.primary, contentColor = CustomColors.onPrimary, shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
                }
            }
        },
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
                    setNotificationsEnabled = { enabled ->
                        userPreferences.setNotificationsEnabled(
                            enabled
                        )
                    }
                )
                onScreenChange(screen = Screen.Onboarding.route, navController) {
                    showFab = false
                }
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
                onScreenChange(screen = Screen.Splash.route, navController) {
                    showFab = false
                }
            }
            composable(Screen.Home.route) {
                HomeScreen(activity, innerPadding)
                onScreenChange(screen = Screen.Home.route, navController) {
                    showFab = true
                }
            }
            composable(Screen.Reports.route) {
                ReportsScreen(
                    activity,
                    innerPadding,
                    isTransactionRecordUpdated,
                    onReportClick = { yearMonth ->
                        navigateToMonthlyStatement(yearMonth)
                    },
                    onAckUpdate = { value -> isTransactionRecordUpdated = value }
                )
                onScreenChange(screen = Screen.Reports.route, navController) {
                    showFab = true
                }
            }
            composable(
                route = Screen.Statements.route,
                arguments = listOf(navArgument("yearMonth") { type = NavType.StringType })
            ) { backStackEntry ->
                val yearMonth = backStackEntry.arguments?.getString("yearMonth")
                StatementsScreen(
                    yearMonth,
                    activity,
                    innerPadding,
                    onTransactionClick = { transaction ->
                        navigateToTransactionDetails(transaction)
                    }
                )
                onScreenChange(screen = Screen.Statements.route, navController) {
                    showFab = true
                }
            }
            composable(
                route = Screen.TransactionDetails.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType }),
                enterTransition = {
                    EnterTransition.None
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")
                val isManual = backStackEntry.arguments?.getString("isManual").toBoolean()
                TransactionDetails(
                    transactionId = transactionId,
                    isManual = isManual,
                    navController = navController,
                    onAckUpdate = { value -> isTransactionRecordUpdated = value }
                )
                onScreenChange(screen = Screen.TransactionDetails.route, navController) {
                    showFab = false
                }
            }
            composable(Screen.AddTransaction.route) {
                AddTransaction(
                    navController = navController,
                    smsAnalysisService = activity.smsAnalysisService,
                    onAckUpdate = { value -> isTransactionRecordUpdated = value }
                )
                onScreenChange(screen = Screen.AddTransaction.route, navController) {
                    showFab = false
                }
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    activity,
                    innerPadding,
                    settingsViewModel
                )
                onScreenChange(screen = Screen.Settings.route, navController) {
                    showFab = false
                }
            }
        }
    }
}

@Composable
fun onScreenChange(
    screen: String,
    navController: NavHostController,
    updateFabVisibility: () -> Unit
) {
    // Update FAB visibility based on the current screen
    LaunchedEffect(screen) {
        updateFabVisibility()
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

//@Preview(showBackground = false)
//@Composable
//fun PieChartPreview() {
//    VyayTheme {
//        val data = listOf(
//            PieChartData(value = 0.4f, color = ChampagnePink),
//            PieChartData(value = 0.6f, color = LightCyan),
//            PieChartData(value = 0.5f, color = ColumbiaBlue)
//        )
//        PieChart(data = data, modifier = Modifier.height(200.dp))
//    }
//}
