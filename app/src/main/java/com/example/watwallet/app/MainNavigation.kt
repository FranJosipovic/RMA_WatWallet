package com.example.watwallet.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.feature.addtransaction.ui.AddTransactionScreen
import com.example.watwallet.feature.auth.ui.LoginScreen
import com.example.watwallet.feature.auth.ui.RegisterScreen
import com.example.watwallet.feature.home.ui.HomeScreen
import com.example.watwallet.feature.profile.job.ui.AddJobRoute
import com.example.watwallet.feature.profile.job.ui.UpdateJobRoute
import com.example.watwallet.feature.profile.ui.ProfileScreen
import com.example.watwallet.ui.components.BottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    startingRoute: String,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val showBottomBar by remember {
        derivedStateOf {
            when (navBackStackEntry?.destination?.route) {
                NavigationItem.Home.route,
                NavigationItem.Add.route,
                NavigationItem.Profile.route -> true

                else -> false
            }
        }
    }

    val showTopBar by remember {
        derivedStateOf {
            when (navBackStackEntry?.destination?.route) {
                NavigationItem.AddJob.route -> true
                NavigationItem.UpdateJob.route -> true
                else -> false
            }
        }
    }

    val topAppBarTitle by remember {
        derivedStateOf {
            when (navBackStackEntry?.destination?.route) {
                NavigationItem.AddJob.route -> "Create New Job"
                NavigationItem.UpdateJob.route -> "Update Job"
                else -> ""
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = { if (showBottomBar) BottomNavigation(navController) },
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(topAppBarTitle) },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back icon",
                            Modifier.clickable {
                                navController.navigateUp()
                            })
                    },
                )
            }
        }
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = startingRoute,
                modifier = Modifier.padding(innerPadding),
            ) {
                navigation(
                    route = NavigationItem.Auth.route,
                    startDestination = NavigationItem.Login.route
                ) {
                    composable(NavigationItem.Login.route) {
                        LoginScreen(navController)
                    }
                    composable(NavigationItem.Register.route) {
                        RegisterScreen(navController)
                    }
                }
                navigation(
                    route = NavigationItem.Main.route,
                    startDestination = NavigationItem.Home.route
                ) {
                    composable(NavigationItem.Home.route) {
                        HomeScreen(navController)
                    }
                    composable(NavigationItem.Add.route) {
                        val startTab =
                            navBackStackEntry?.arguments?.getString("startTab")?.toInt() ?: 0
                        AddTransactionScreen(
                            startTabContent = startTab,
                            snackbarHostState = snackbarHostState
                        ) {
                            navController.navigate(NavigationItem.AddJob.route)
                        }
                    }
                    composable(NavigationItem.Profile.route) {
                        ProfileScreen(navController)
                    }
                    composable(NavigationItem.AddJob.route) {
                        AddJobRoute() {
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "reloadJobs",
                                true
                            )
                            navController.navigateUp()
                        }
                    }
                    composable(NavigationItem.UpdateJob.route) {
                        val jobId = navBackStackEntry?.arguments?.getString("jobId")
                        UpdateJobRoute(jobId ?: "") {
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "reloadJobs",
                                true
                            )
                            navController.navigateUp()
                        }
                    }
                }
            }
        }
    }
}
