package com.rtech.cartly.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rtech.cartly.model.Deal

object DealsRepository {

    private val db = FirebaseFirestore.getInstance()

    fun loadAllDeals(): Task<List<Deal>> = loadDeals(null, null)

    fun loadDeals(store: String?, category: String?): Task<List<Deal>> {
        var query: Query = db.collection("deals")
        store?.let { query = query.whereEqualTo("store", it) }
        category?.let { query = query.whereEqualTo("category", it) }
        return query.get().continueWith { task ->
            if (task.isSuccessful && task.result != null) {
                task.result!!.map { doc -> doc.toDeal() }
            } else {
                throw (task.exception ?: RuntimeException("Failed to load deals"))
            }
        }
    }

    fun loadCategories(): Task<List<String>> =
        db.collection("deals")
            .get()
            .continueWith { task ->
                if (task.isSuccessful && task.result != null) {
                    task.result!!.mapNotNull { doc -> doc.getString("category") }
                        .distinct()
                        .sorted()
                } else {
                    throw (task.exception ?: RuntimeException("Failed to load categories"))
                }
            }

    private fun com.google.firebase.firestore.DocumentSnapshot.toDeal(): Deal =
        Deal(
            id = id,
            name = getString("name") ?: "",
            store = getString("store") ?: "",
            distance = getString("distance") ?: "",
            priceNow = getString("price_now") ?: "",
            priceWas = getString("price_was") ?: "",
            discount = getString("discount") ?: "",
            category = getString("category") ?: "Other",
            imageUrl = getString("image_url") ?: ""
        )
}