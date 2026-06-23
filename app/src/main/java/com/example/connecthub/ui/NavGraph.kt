package com.example.connecthub.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.connecthub.ui.auth.LoginScreen
import com.example.connecthub.ui.auth.RegisterScreen
import com.example.connecthub.ui.feed.FeedScreen
import com.example.connecthub.viewmodel.AuthViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: AuthViewModel = viewModel()

    val startDestination =
        if (viewModel.isUserLoggedIn())
            "feed"
        else
            "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(

                onRegisterClick = {
                    navController.navigate("register")
                },

                onLoginSuccess = {

                    navController.navigate("feed") {

                        popUpTo("login") {
                            inclusive = true
                        }

                    }

                }
            )
        }
        composable("register") {
            RegisterScreen(

                onLoginClick = {
                    navController.popBackStack()
                },

                onRegisterSuccess = {

                    navController.navigate("feed") {

                        popUpTo("register") {
                            inclusive = true
                        }

                    }

                }
            )
        }
        composable("feed") {
            FeedScreen(
                onLogoutClick = {

                }
            )
        }
    }
}
