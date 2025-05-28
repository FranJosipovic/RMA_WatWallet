package com.example.watwallet.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.watwallet.core.navigation.mainNavigationItems

@Composable
fun BottomNavigation(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        mainNavigationItems.forEach { screen ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color.Transparent
                ),
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.createDefaultRoute()){
                        launchSingleTop = true
                        popUpTo(screen.route)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = screen.vectorAsset),
                        contentDescription = null,
                        tint = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    )
                },
                label = { Text(screen.label, color = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)}
            )
        }
    }
}