package com.rtech.cartly.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

object StatsRepository {

    private val db = FirebaseFirestore.getInstance()

    fun loadBasketCount(uid: String): Task<Int> =
        db.collection("basket")
            .whereEqualTo("userId", uid)
            .get()
            .continueWith { task ->
                if (task.isSuccessful && task.result != null) {
                    task.result!!.documents.count { it.getString("name") != "placeholder" }
                } else {
                    throw (task.exception ?: RuntimeException("Failed to load stats"))
                }
            }

    fun loadFavouritesCount(uid: String): Task<Int> =
        db.collection("favourites")
            .whereEqualTo("userId", uid)
            .get()
            .continueWith { task ->
                if (task.isSuccessful && task.result != null) {
                    task.result!!.documents.count { it.getString("name") != "placeholder" }
                } else {
                    throw (task.exception ?: RuntimeException("Failed to load stats"))
                }
            }
}