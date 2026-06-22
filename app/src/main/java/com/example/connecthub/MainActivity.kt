package com.example.connecthub

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.connecthub.firebase.FirebaseManager
import com.example.connecthub.ui.auth.LoginScreen
import com.example.connecthub.ui.auth.RegisterScreen
import com.example.connecthub.ui.theme.ConnectHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectHubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        LoginScreen(onRegisterClick = {})
                    }
                }
            }
        }

        Log.d("FirebaseTest", FirebaseManager.auth.toString())
        Log.d("FirebaseTest", FirebaseManager.firestore.toString())
    }
}
