package com.example.watwallet.core.navigation

import androidx.annotation.DrawableRes
import com.example.watwallet.R

sealed class NavigationDestination(open val route: String)
sealed class NavigationItem(
    override val route: String,
    @DrawableRes val vectorAsset: Int,
    val label: String
) : NavigationDestination(route) {
    open fun createDefaultRoute(): String = route

    data object Auth : NavigationItem("auth", 0, "Auth")
    data object Main : NavigationItem("main", 0, "Main")
    data object Login : NavigationItem("login", 0, "Login")
    data object Register : NavigationItem("register", 0, "Register")
    data object Home : NavigationItem("home", R.drawable.home_icon, "Home")
    data object Add : NavigationItem("add/{startTab}", R.drawable.add_icon, "Add") {
        fun createRoute(startTab: Int = 0) = "add/$startTab"
        override fun createDefaultRoute() = "add/0"
    }
    data object Profile : NavigationItem("profile", R.drawable.person_icon, "Profile")
    data object AddJob : NavigationItem("addjob/{jobId}", 0, "AddJob"){
        fun createUpdateRoute(jobId: String) = "addjob/$jobId"
        override fun createDefaultRoute(): String {
            return "addJob/null"
        }
    }
    data object UpdateJob : NavigationItem("updateJob/{jobId}",0,"UpdateJob"){
        fun createUpdateRoute(jobId: String) = "updateJob/$jobId"
    }
}

val navigationBarItems = listOf(
    NavigationItem.Home,
    NavigationItem.Add,
    NavigationItem.Profile
)