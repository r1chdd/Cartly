package com.rtech.cartly.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rtech.cartly.data.DealsRepository
import com.rtech.cartly.model.Deal

class SearchViewModel : ViewModel() {

    private val _deals = MutableLiveData<List<Deal>>(emptyList())
    val deals: LiveData<List<Deal>> = _deals

    private var loaded = false

    fun loadDeals() {
        if (loaded) return
        loaded = true
        DealsRepository.loadAllDeals().addOnSuccessListener { list ->
            _deals.value = list
        }
    }

    fun search(query: String): List<Deal> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return deals.value.orEmpty().filter { deal ->
            deal.name.contains(q, ignoreCase = true) ||
                    deal.store.contains(q, ignoreCase = true) ||
                    deal.category.contains(q, ignoreCase = true)
        }
    }
}