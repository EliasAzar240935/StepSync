package com.stepsync.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stepsync.presentation.activity.ActivityScreen
import com.stepsync.presentation.activity.ActivityViewModel
import com.stepsync.presentation.auth.AuthViewModel
import com.stepsync.presentation.auth.LoginScreen
import com.stepsync.presentation.auth.RegisterScreen
import com.stepsync.presentation.goals.GoalsScreen
import com.stepsync.presentation.goals.GoalsViewModel
import com.stepsync.presentation.home.HomeScreen
import com.stepsync.presentation.home.HomeViewModel
import com.stepsync.presentation.profile.ProfileScreen
import com.stepsync.presentation.profile.ProfileViewModel
import com.stepsync.presentation.social.SocialScreen
import com.stepsync.presentation.social.SocialViewModel

/**
 * Main app composable with navigation
 */
@Composable
fun StepSyncApp(
    navController: NavHostController = rememberNavController()
) {
    val startDestination = Screen.Login.route // In a real app, check if user is logged in
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToActivity = {
                    navController.navigate(Screen.Activity.route)
                },
                onNavigateToGoals = {
                    navController.navigate(Screen.Goals.route)
                },
                onNavigateToSocial = {
                    navController.navigate(Screen.Social.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Activity.route) {
            val viewModel: ActivityViewModel = hiltViewModel()
            ActivityScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Goals.route) {
            val viewModel: GoalsViewModel = hiltViewModel()
            GoalsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Social.route) {
            val viewModel: SocialViewModel = hiltViewModel()
            SocialScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
