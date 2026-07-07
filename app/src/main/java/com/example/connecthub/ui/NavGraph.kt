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
import com.example.connecthub.ui.bookmark.BookmarkScreen
import com.example.connecthub.ui.feed.CommentScreen
import com.example.connecthub.ui.feed.FeedScreen
import com.example.connecthub.ui.notification.NotificationScreen
import com.example.connecthub.ui.profile.EditProfileScreen
import com.example.connecthub.ui.profile.ProfileScreen
import com.example.connecthub.ui.profile.SearchUserScreen
import com.example.connecthub.ui.profile.UserProfileScreen
import com.example.connecthub.viewmodel.AuthViewModel
import com.example.connecthub.viewmodel.BookmarkViewModel
import com.example.connecthub.viewmodel.CommentViewModel
import com.example.connecthub.viewmodel.NotificationViewModel
import com.example.connecthub.viewmodel.ProfileViewModel
import com.example.connecthub.viewmodel.SearchViewModel
import com.example.connecthub.viewmodel.UserProfileViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val startDestination =
        if (authViewModel.isUserLoggedIn()) "feed" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    authViewModel.resetState()
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    authViewModel.resetState()
                    navController.navigate("feed") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onLoginClick = {
                    authViewModel.resetState()
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    authViewModel.resetState()
                    navController.navigate("feed") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("feed") {
            val bookmarkViewModel: BookmarkViewModel = viewModel()
            FeedScreen(
                bookmarkViewModel = bookmarkViewModel,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("feed") { inclusive = true }
                    }
                },
                onCommentClick = { postId, postText ->
                    navController.navigate("comment/$postId?postText=$postText")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onNotificationsClick = {
                    navController.navigate("notifications")
                },
                onBookmarksClick = {
                    navController.navigate("bookmarks")
                }
            )
        }

        composable("bookmarks") {
            val bookmarkViewModel: BookmarkViewModel = viewModel()
            BookmarkScreen(
                viewModel = bookmarkViewModel,
                onBackClick = { navController.popBackStack() },
                onCommentClick = { postId, postText ->
                    navController.navigate("comment/$postId?postText=$postText")
                }
            )
        }

        composable("notifications") {
            val notificationViewModel: NotificationViewModel = viewModel()
            NotificationScreen(
                viewModel = notificationViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("search") {
            val searchViewModel: SearchViewModel = viewModel()
            SearchUserScreen(
                viewModel = searchViewModel,
                onUserClick = { uid ->
                    navController.navigate("user_profile/$uid")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "user_profile/{uid}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid").orEmpty()
            val userProfileViewModel: UserProfileViewModel = viewModel()
            UserProfileScreen(
                uid = uid,
                viewModel = userProfileViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
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
                navArgument("postText") {
                    type = NavType.StringType
                    defaultValue = "Post Details"
                }
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