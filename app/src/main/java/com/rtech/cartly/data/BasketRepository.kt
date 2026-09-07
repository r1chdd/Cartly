package com.rtech.cartly.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.rtech.cartly.model.BasketItem

object BasketRepository {

    private val db = FirebaseFirestore.getInstance()

    fun loadBasket(uid: String): Task<List<BasketItem>> =
        db.collection("basket")
            .whereEqualTo("userId", uid)
            .get()
            .continueWith { task ->
                if (task.isSuccessful && task.result != null) {
                    task.result!!.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        if (name == "placeholder") return@mapNotNull null
                        BasketItem(
                            id = doc.id,
                            name = name,
                            price = doc.getString("price") ?: "",
                            store = doc.getString("store") ?: "",
                            imageUrl = doc.getString("image_url") ?: "",
                            checked = doc.getString("checked") == "true"
                        )
                    }
                } else {
                    throw (task.exception ?: RuntimeException("Failed to load basket"))
                }
            }

    fun addItem(uid: String, name: String, price: String, store: String, imageUrl: String) {
        db.collection("basket").add(mapOf(
            "userId" to uid,
            "name" to name,
            "price" to price,
            "store" to store,
            "image_url" to imageUrl,
            "checked" to "false"
        ))
    }

    fun setChecked(id: String, checked: Boolean) {
        db.collection("basket").document(id).update("checked", checked.toString())
    }

    fun deleteItem(id: String) {
        db.collection("basket").document(id).delete()
    }

    fun clearBasket(uid: String) {
        db.collection("basket")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.delete()
                }
            }
    }
}