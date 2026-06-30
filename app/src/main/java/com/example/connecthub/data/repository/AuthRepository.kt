package com.example.connecthub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants

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
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        saveUserToFirestore(uid, username, email, onResult)
                    } else {
                        onResult(false, "User created but UID not found.")
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser() = auth.currentUser


    fun getCurrentUserData(
        onResult: (User?) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(null)
            return
        }

        firestore
            .collection(Constants.USERS_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                onResult(document.toObject(User::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
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
            email = email
        )

        firestore
            .collection(Constants.USERS_COLLECTION)
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

    fun updateUserProfile(
        username: String,
        bio: String,
        profileImageUrl: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(false, "User not logged in.")
            return
        }

        val updates = mapOf(
            "username" to username,
            "bio" to bio,
            "profileImageUrl" to profileImageUrl
        )

        firestore
            .collection(Constants.USERS_COLLECTION)
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

}
