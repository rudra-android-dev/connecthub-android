package com.example.connecthub.ui.bookmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.connecthub.ui.feed.PostItem
import com.example.connecthub.viewmodel.BookmarkViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    viewModel: BookmarkViewModel = viewModel(),
    onBackClick: () -> Unit,
    onCommentClick: (String, String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    LaunchedEffect(Unit) {
        viewModel.loadBookmarks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.posts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved posts yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.posts) { post ->
                        PostItem(
                            post = post,
                            currentUserId = currentUserId,
                            isBookmarked = true,
                            onLikeClick = {},
                            onDeleteClick = {},
                            onCommentClick = { onCommentClick(post.postId, post.content) },
                            onBookmarkClick = { viewModel.removeBookmark(post.postId) }
                        )
                    }
                }
            }
        }
    }
}