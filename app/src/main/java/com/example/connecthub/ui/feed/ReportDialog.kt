package com.example.connecthub.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.connecthub.data.model.Post
import com.example.connecthub.viewmodel.ReportViewModel

@Composable
fun ReportDialog(
    post: Post,
    onDismiss: () -> Unit,
    viewModel: ReportViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedReason by remember { mutableStateOf("") }

    val reportReasons = listOf("Spam", "Harassment", "Inappropriate Content", "Other")

    if (state.success) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetState()
                onDismiss()
            },
            title = { Text("Report Submitted") },
            text = { Text("Thank you. We'll review this post.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetState()
                    onDismiss()
                }) {
                    Text("OK")
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.resetState()
            onDismiss()
        },
        title = { Text("Report Post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.alreadyReported) {
                    Text(
                        text = "You have already reported this post.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Choose a reason:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    reportReasons.forEach { reason ->
                        TextButton(
                            onClick = { selectedReason = reason },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (selectedReason == reason) "✓  $reason" else "    $reason",
                                color = if (selectedReason == reason)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    state.error?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (state.isSubmitting) {
                CircularProgressIndicator()
            } else if (!state.alreadyReported) {
                TextButton(
                    onClick = { viewModel.submitReport(post, selectedReason) }
                ) {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.resetState()
                onDismiss()
            }) {
                Text(if (state.alreadyReported) "Close" else "Cancel")
            }
        }
    )
}