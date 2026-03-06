package com.grex.vyay

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.grex.vyay.services.AutoAssignTagsWorker
import com.grex.vyay.services.CheckAssignableRecordsWorker
import com.grex.vyay.ui.components.FooterNavBar
import com.grex.vyay.ui.components.HomeViewModel
import com.grex.vyay.ui.components.SharedViewModel
import com.grex.vyay.ui.theme.CustomColors
import com.grex.vyay.ui.theme.VyayTheme
import java.util.concurrent.TimeUnit


open class MainActivity : ComponentActivity() {
    lateinit var smsAnalysisService: SmsAnalysisService
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var autoAssignWorkRequest: WorkRequest
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var homeViewModel: HomeViewModel

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize SharedPreferences
        sharedPrefs = getSharedPreferences("vyay_prefs", Context.MODE_PRIVATE)
        autoAssignWorkRequest = OneTimeWorkRequestBuilder<AutoAssignTagsWorker>().build()

        smsAnalysisService = SmsAnalysisService.getInstance()
        settingsViewModel = SettingsViewModel(smsAnalysisService.appDao)
        homeViewModel = HomeViewModel(sharedPrefs)

        if (!sharedPrefs.getBoolean("show_updateSimilarRecords_banner", false)) {
            // Schedule CheckAssignableRecordsWorker
            val checkWorkRequest = OneTimeWorkRequestBuilder<CheckAssignableRecordsWorker>()
                .setInitialDelay(3, TimeUnit.SECONDS) // Slight delay for demo purposes
                .build()

            WorkManager.getInstance(this).enqueue(checkWorkRequest)

            // Observe WorkManager for alert
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(checkWorkRequest.id)
                .observe(this) { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        // Check if alert should be shown
                        if (sharedPrefs.getBoolean("show_updateSimilarRecords_banner", false)) {
                            Log.d("ShowAlert", "True")
//                        showAlert()
                            sharedPrefs.edit().putBoolean("show_updateSimilarRecords_banner", true)
                                .apply()
                        } else {
                            Log.d("ShowAlert", "False")

                        }
                    }
                }
        } else {
            homeViewModel.commitPrefUpdateFlag()
        }


        setContent {
            VyayTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CustomColors.backgroundPrimaryBottom
                ) {
                    AppNavigation(
                        this,
                        settingsViewModel,
                        homeViewModel,
                        sharedViewModel,
                        startAutoAssignTagsWorker = { startAutoAssignTagsWorker() })
                }
            }
        }
    }

    private fun startAutoAssignTagsWorker() {
        Log.d("startAutoAssignTagsWorker", "Worker queued!")
        homeViewModel.commitPrefProcessingFlag()
        WorkManager.getInstance(this).enqueue(autoAssignWorkRequest)
        // Observe WorkManager for alert
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(autoAssignWorkRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    sharedPrefs.edit().putBoolean("show_updateSimilarRecords_banner", false).apply()
                    sharedPrefs.edit().putBoolean("show_processingSimilarRecords_banner", false)
                        .apply()

                    homeViewModel.resetPrefUpdateFlag()
                    homeViewModel.resetPrefProcessingFlag()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        smsAnalysisService.stopAnalysis()
    }


//    private fun showAlert() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Similar Records Found")
//        builder.setMessage("At least three similar records exist. Do you want to auto-assign tags?")
//        builder.setPositiveButton("Start") { dialog, _ ->
//            dialog.dismiss()
//
//            // Start AutoAssignTagsWorker
//            val autoAssignWorkRequest = OneTimeWorkRequestBuilder<AutoAssignTagsWorker>()
//                .build()
//            WorkManager.getInstance(this).enqueue(autoAssignWorkRequest)
//
//            // Observe completion
//            WorkManager.getInstance(this).getWorkInfoByIdLiveData(autoAssignWorkRequest.id)
//                .observe(this) { workInfo ->
//                    if (workInfo != null && workInfo.state.isFinished) {
//                        showCompletionMessage()
//                    }
//                }
//        }
//        builder.setNegativeButton("Cancel") { dialog, _ ->
//            dialog.dismiss()
//        }
//        builder.show()
//    }
//
//    private fun showCompletionMessage() {
//        AlertDialog.Builder(this)
//            .setTitle("Auto-Assign Complete")
//            .setMessage("The tags have been successfully auto-assigned.")
//            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
//            .show()
//    }
//    private val smsProcessedReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            // Refresh your UI here
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        LocalBroadcastManager.getInstance(this).registerReceiver(smsProcessedReceiver, IntentFilter("SMS_PROCESSED"))
//    }
//
//    override fun onPause() {
//        super.onPause()
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(smsProcessedReceiver)
//    }

}

@Composable
fun AppNavigation(
    activity: MainActivity,
    settingsViewModel: SettingsViewModel,
    homeViewModel: HomeViewModel,
    sharedViewModel: SharedViewModel,
    startAutoAssignTagsWorker: () -> Unit
) {
    val navController = rememberNavController()
    val userPreferences = UserPreferences(LocalContext.current)
    val startDestination = if (userPreferences.getUserName()
            .isEmpty()
    ) Screen.Onboarding.route else Screen.Splash.route

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var isTransactionRecordUpdated by remember { mutableStateOf<Boolean>(false) }
    var showFab by remember { mutableStateOf(true) }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    fun navigateToTransactionDetails(transaction: TransactionRecord) {
        val route = Screen.TransactionDetails.createRoute(transaction.id, transaction.isManual)
        navController.navigate(route)
    }

    fun navigateToMonthlyStatement(yearMonth: String) {
        Log.d("NavigateToMonthlyStatement: ", yearMonth)
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
                    containerColor = CustomColors.primary,
                    contentColor = CustomColors.onPrimary,
                    shape = CircleShape
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
                HomeScreen(
                    activity,
                    innerPadding,
                    homeViewModel,
                    sharedViewModel,
                    onRequestAutoAssignTags = { startAutoAssignTagsWorker() },
                    activity.smsAnalysisService.appDao
                )
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
                        // Save the selected transaction ID before navigating
                        savedStateHandle?.set("selectedTransactionId", transaction.id)
                        navigateToTransactionDetails(transaction)
                    },
                    savedStateHandle = savedStateHandle ?: SavedStateHandle()
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
                    settingsViewModel,
                    sharedViewModel,
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

