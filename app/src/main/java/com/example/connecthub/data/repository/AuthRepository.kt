package com.example.connecthub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants


class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    private val firestore = FirebaseFirestore.getInstance()

    fun registerUser(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
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
            .addOnFailureListener {

                onResult(
                    false,
                    it.message
                )

            }
    }
}
