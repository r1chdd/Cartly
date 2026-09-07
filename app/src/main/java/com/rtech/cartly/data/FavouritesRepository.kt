package com.rtech.cartly.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

object FavouritesRepository {

    private val db = FirebaseFirestore.getInstance()

    fun loadFavouriteNames(uid: String): Task<List<String>> =
        db.collection("favourites")
            .whereEqualTo("userId", uid)
            .get()
            .continueWith { task ->
                if (task.isSuccessful && task.result != null) {
                    task.result!!.mapNotNull { doc ->
                        doc.getString("name")?.takeIf { it.isNotEmpty() && it != "placeholder" }
                    }
                } else {
                    throw (task.exception ?: RuntimeException("Failed to load favourites"))
                }
            }

    fun addFavourite(uid: String, name: String) {
        db.collection("favourites").add(mapOf(
            "userId" to uid,
            "name" to name
        ))
    }

    fun removeFavourite(uid: String, name: String) {
        db.collection("favourites")
            .whereEqualTo("userId", uid)
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.delete()
                }
            }
    }
}