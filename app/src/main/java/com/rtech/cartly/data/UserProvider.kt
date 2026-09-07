package com.rtech.cartly.data

import com.google.firebase.auth.FirebaseAuth

object UserProvider {

    private val auth = FirebaseAuth.getInstance()

    fun currentUid(): String? = auth.currentUser?.uid

    fun ensureSignedIn(onResult: (String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            onResult(user.uid)
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { result -> onResult(result.user?.uid) }
            .addOnFailureListener { onResult(null) }
    }
}