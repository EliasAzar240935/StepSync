package com.stepsync.presentation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Activity : Screen("activity")
    object Goals : Screen("goals")
    object Social : Screen("social")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}
