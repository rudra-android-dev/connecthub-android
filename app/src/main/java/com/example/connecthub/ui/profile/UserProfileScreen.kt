package com.example.connecthub.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.connecthub.data.model.Post
import com.example.connecthub.ui.feed.PostItem
import com.example.connecthub.ui.feed.ReportDialog
import com.example.connecthub.viewmodel.FollowViewModel
import com.example.connecthub.viewmodel.ReportViewModel
import com.example.connecthub.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    uid: String,
    viewModel: UserProfileViewModel = viewModel(),
    followViewModel: FollowViewModel = viewModel(),
    onBackClick: () -> Unit,
    onCommentClick: (String, String) -> Unit = { _, _ -> },
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    onBlockSuccess: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val followState by followViewModel.uiState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val isOwnProfile = uid == currentUserId

    var reportingPost by remember { mutableStateOf<Post?>(null) }
    val reportViewModel: ReportViewModel = viewModel()

    LaunchedEffect(uid) {
        viewModel.loadUserProfile(uid)
        viewModel.checkIsBlocked(uid)
    }

    LaunchedEffect(state.user) {
        state.user?.let { followViewModel.init(uid) }
    }

    LaunchedEffect(state.isBlocked) {
        onBlockSuccess()
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
                title = { Text(state.user?.username ?: "Profile") },
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.error ?: "Something went wrong.", color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!state.user?.profileImageUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = state.user?.profileImageUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.size(96.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = Color.White
                                        )
                                    }
                                }

                                Text(
                                    text = state.user?.username ?: "",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                if (!state.user?.bio.isNullOrEmpty()) {
                                    Text(
                                        text = state.user?.bio ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ClickableStatColumn(
                                        count = state.posts.size,
                                        label = "Posts",
                                        onClick = null
                                    )
                                    ClickableStatColumn(
                                        count = followState.followers,
                                        label = "Followers",
                                        onClick = onFollowersClick
                                    )
                                    ClickableStatColumn(
                                        count = followState.following,
                                        label = "Following",
                                        onClick = onFollowingClick
                                    )
                                }

                                if (!isOwnProfile) {
                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (followState.isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    } else if (followState.isFollowing) {
                                        OutlinedButton(
                                            onClick = { followViewModel.unfollowUser() },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Following") }
                                    } else {
                                        Button(
                                            onClick = { followViewModel.followUser() },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Follow") }
                                    }

                                    followState.error?.let { err ->
                                        Text(
                                            text = err,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    if (state.isBlocked) {
                                        OutlinedButton(
                                            onClick = { viewModel.unblockUser(uid) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) { Text("Unblock User") }
                                    } else {
                                        Button(
                                            onClick = { viewModel.blockUser(uid) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Block User") }
                                    }

                                    state.blockMessage?.let { msg ->
                                        Text(
                                            text = msg,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Posts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (state.posts.isEmpty()) {
                        item {
                            Text(
                                "No posts yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else {
                        items(state.posts) { post ->
                            PostItem(
                                post = post,
                                currentUserId = currentUserId,
                                onLikeClick = {},
                                onDeleteClick = {},
                                onCommentClick = { onCommentClick(post.postId, post.content) },
                                onReportClick = { reportingPost = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A stat column that optionally responds to taps.
 * Used for Posts (non-clickable), Followers, and Following (both clickable).
 */
@Composable
fun ClickableStatColumn(count: Int, label: String, onClick: (() -> Unit)?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null)
            Modifier.clickable { onClick() }.padding(8.dp)
        else
            Modifier.padding(8.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}