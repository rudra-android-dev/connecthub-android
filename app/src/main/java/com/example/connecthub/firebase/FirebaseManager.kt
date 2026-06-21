package com.example.connecthub.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {

    val auth = FirebaseAuth.getInstance()

    val firestore = FirebaseFirestore.getInstance()
}