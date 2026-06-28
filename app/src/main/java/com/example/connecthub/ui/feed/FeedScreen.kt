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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.connecthub.viewmodel.FeedViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = viewModel(),
    onLogoutClick: () -> Unit,
    onCommentClick: (String, String) -> Unit
) {
    var postText by remember {
        mutableStateOf("")
    }

    val state by viewModel.uiState.collectAsState()

    val currentUserId = remember {
        FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    }

    LaunchedEffect(Unit) {
        viewModel.startListeningToPosts()
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            postText = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Welcome to ConnectHub"
        )

        OutlinedTextField(
            value = postText,
            onValueChange = {
                postText = it
            },
            label = {
                Text("What's on your mind?")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.createPost(postText)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Post")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        state.error?.let {
            Text(text = it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.posts.isEmpty()) {
            Text(
                text = "No posts yet. Be the first to share something!",
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(state.posts) { post ->
                    PostItem(
                        post = post,
                        currentUserId = currentUserId,
                        onLikeClick = {
                            viewModel.toggleLike(post)
                        },
                        onDeleteClick = {
                            viewModel.deletePost(post.postId)
                        },
                        onCommentClick = {
                            onCommentClick(post.postId, post.content)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogoutClick
        ) {
            Text("Logout")
        }
    }
}
