package com.example.connecthub.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.connecthub.ui.auth.LoginScreen
import com.example.connecthub.ui.auth.RegisterScreen
import com.example.connecthub.ui.feed.CommentScreen
import com.example.connecthub.ui.feed.FeedScreen
import com.example.connecthub.ui.profile.EditProfileScreen
import com.example.connecthub.ui.profile.ProfileScreen
import com.example.connecthub.viewmodel.AuthViewModel
import com.example.connecthub.viewmodel.CommentViewModel
import com.example.connecthub.viewmodel.ProfileViewModel

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
                    viewModel.resetState()
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    viewModel.resetState()
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
                    viewModel.resetState()
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    viewModel.resetState()
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
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("feed") {
                            inclusive = true
                        }
                    }
                },
                onCommentClick = { postId, postText ->
                    navController.navigate("comment/$postId?postText=$postText")
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                onLogoutClick = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                onEditProfileClick = {
                    navController.navigate("editProfile")
                }
            )
        }
        composable("editProfile") {
            val profileViewModel: ProfileViewModel = viewModel()

            EditProfileScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "comment/{postId}?postText={postText}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("postText") { type = NavType.StringType; defaultValue = "Post Details" }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId").orEmpty()
            val postText = backStackEntry.arguments?.getString("postText").orEmpty()

            val commentViewModel: CommentViewModel = viewModel()

            CommentScreen(
                postId = postId,
                postText = postText,
                viewModel = commentViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}