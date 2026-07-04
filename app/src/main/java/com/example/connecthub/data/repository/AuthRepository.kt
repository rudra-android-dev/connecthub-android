package com.example.connecthub.data.repository

import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun registerUser(
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    saveUserToFirestore(uid, username, email, onResult)
                } else {
                    onResult(false, "Registration failed.")
                }
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun saveUserToFirestore(
        uid: String,
        username: String,
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = User(
            uid = uid,
            username = username,
            usernameLower = username.lowercase(),
            email = email,
            createdAt = System.currentTimeMillis(),
            bio = "",
            profileImageUrl = ""
        )

        firestore.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }
}