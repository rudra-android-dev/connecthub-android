package com.example.connecthub.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.connecthub.data.model.Post
import com.example.connecthub.ui.components.LoadingPostItem
import com.example.connecthub.viewmodel.BookmarkViewModel
import com.example.connecthub.viewmodel.FeedViewModel
import com.example.connecthub.viewmodel.ReportViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = viewModel(),
    bookmarkViewModel: BookmarkViewModel = viewModel(),
    onLogoutClick: () -> Unit,
    onCommentClick: (String, String) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var postText by remember { mutableStateOf("") }

    val state by viewModel.uiState.collectAsState()
    val bookmarkState by bookmarkViewModel.uiState.collectAsState()

    val currentUserId = remember {
        FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    }

    var reportingPost by remember { mutableStateOf<Post?>(null) }
    val reportViewModel: ReportViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.startListeningToPosts()
        bookmarkViewModel.loadBookmarkedPostIds()
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            postText = ""
            viewModel.resetSuccess()
        }
    }

    reportingPost?.let { post ->
        ReportDialog(
            post = post,
            onDismiss = { reportingPost = null },
            viewModel = reportViewModel
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ConnectHub") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
                label = { Text("What's on your mind?") },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.createPost(postText) },
                enabled = !state.isLoading && postText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Posting..." else "Post")
            }

            Spacer(modifier = Modifier.height(12.dp))

            state.error?.let {
                Text(
                    text = "Couldn't load posts. Please check your connection.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                state.isLoading && state.posts.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(4) { LoadingPostItem() }
                    }
                }

                state.posts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Nothing here yet.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start following people or create your first post.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(state.posts) { post ->
                            PostItem(
                                post = post,
                                currentUserId = currentUserId,
                                isBookmarked = bookmarkState.bookmarkedPostIds.contains(post.postId),
                                onLikeClick = { viewModel.toggleLike(post) },
                                onDeleteClick = { viewModel.deletePost(post.postId) },
                                onCommentClick = { onCommentClick(post.postId, post.content) },
                                onBookmarkClick = { bookmarkViewModel.toggleBookmark(post.postId) },
                                onReportClick = { reportingPost = it }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onNotificationsClick, modifier = Modifier.fillMaxWidth()) {
                Text("🔔 Notifications")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onBookmarksClick, modifier = Modifier.fillMaxWidth()) {
                Text("🔖 Bookmarks")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onSearchClick, modifier = Modifier.fillMaxWidth()) {
                Text("Search Users")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onProfileClick, modifier = Modifier.fillMaxWidth()) {
                Text("Go to Profile")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                Text("Logout")
            }
        }
    }
}