package com.example.connecthub.ui

import android.net.Uri
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
import com.example.connecthub.ui.profile.FollowListScreen
import com.example.connecthub.ui.profile.ProfileScreen
import com.example.connecthub.ui.profile.SearchUserScreen
import com.example.connecthub.ui.profile.UserProfileScreen
import com.example.connecthub.ui.settings.BlockedUsersScreen
import com.example.connecthub.ui.settings.SettingsScreen
import com.example.connecthub.viewmodel.AuthViewModel
import com.example.connecthub.viewmodel.BookmarkViewModel
import com.example.connecthub.viewmodel.CommentViewModel
import com.example.connecthub.viewmodel.FeedViewModel
import com.example.connecthub.viewmodel.FollowListViewModel
import com.example.connecthub.viewmodel.FollowViewModel
import com.example.connecthub.viewmodel.NotificationViewModel
import com.example.connecthub.viewmodel.ProfileViewModel
import com.example.connecthub.viewmodel.SearchViewModel
import com.example.connecthub.viewmodel.UserProfileViewModel

@Composable
fun NavGraph(
    darkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val startDestination = if (authViewModel.isUserLoggedIn()) "feed" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

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
            val feedViewModel: FeedViewModel = viewModel()
            val bookmarkViewModel: BookmarkViewModel = viewModel()
            FeedScreen(
                viewModel = feedViewModel,
                bookmarkViewModel = bookmarkViewModel,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("feed") { inclusive = true }
                    }
                },
                onCommentClick = { postId, postText ->
                    val encoded = Uri.encode(postText)
                    navController.navigate("comment/$postId?postText=$encoded")
                },
                onProfileClick = { navController.navigate("profile") },
                onSearchClick = { navController.navigate("search") },
                onNotificationsClick = { navController.navigate("notifications") },
                onBookmarksClick = { navController.navigate("bookmarks") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                darkMode = darkMode,
                onDarkModeChanged = onDarkModeChanged,
                onEditProfile = { navController.navigate("editProfile") },
                onBlockedUsers = { navController.navigate("blockedUsers") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("blockedUsers") {
            BlockedUsersScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("bookmarks") {
            val bookmarkViewModel: BookmarkViewModel = viewModel()
            BookmarkScreen(
                viewModel = bookmarkViewModel,
                onBackClick = { navController.popBackStack() },
                onCommentClick = { postId, postText ->
                    val encoded = Uri.encode(postText)
                    navController.navigate("comment/$postId?postText=$encoded")
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
                onUserClick = { uid -> navController.navigate("user_profile/$uid") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "user_profile/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid").orEmpty()
            val userProfileViewModel: UserProfileViewModel = viewModel()
            val followViewModel: FollowViewModel = viewModel()
            val feedViewModel: FeedViewModel = viewModel()

            UserProfileScreen(
                uid = uid,
                viewModel = userProfileViewModel,
                followViewModel = followViewModel,
                onBackClick = { navController.popBackStack() },
                onCommentClick = { postId, postText ->
                    val encoded = Uri.encode(postText)
                    navController.navigate("comment/$postId?postText=$encoded")
                },
                onFollowersClick = {
                    navController.navigate("follow_list/$uid/followers")
                },
                onFollowingClick = {
                    navController.navigate("follow_list/$uid/following")
                },
                onBlockSuccess = { feedViewModel.refreshBlockedUsers() }
            )
        }

        composable(
            route = "follow_list/{uid}/{type}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid").orEmpty()
            val type = backStackEntry.arguments?.getString("type").orEmpty()
            val followListViewModel: FollowListViewModel = viewModel()
            FollowListScreen(
                uid = uid,
                type = type,
                onBackClick = { navController.popBackStack() },
                onUserClick = { clickedUid ->
                    navController.navigate("user_profile/$clickedUid")
                },
                viewModel = followListViewModel
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
                onEditProfileClick = { navController.navigate("editProfile") },
                onFollowersClick = { uid ->
                    navController.navigate("follow_list/$uid/followers")
                },
                onFollowingClick = { uid ->
                    navController.navigate("follow_list/$uid/following")
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