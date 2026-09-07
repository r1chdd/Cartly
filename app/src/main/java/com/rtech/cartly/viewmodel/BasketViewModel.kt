package com.rtech.cartly.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rtech.cartly.data.BasketRepository
import com.rtech.cartly.data.UserProvider
import com.rtech.cartly.model.BasketItem

class BasketViewModel : ViewModel() {

    private val _items = MutableLiveData<List<BasketItem>>(emptyList())
    val items: LiveData<List<BasketItem>> = _items

    fun loadBasket() {
        UserProvider.ensureSignedIn { uid ->
            if (uid == null) return@ensureSignedIn
            BasketRepository.loadBasket(uid).addOnSuccessListener { list ->
                _items.value = list
            }
        }
    }

    fun toggleChecked(item: BasketItem) {
        val newChecked = !item.checked
        BasketRepository.setChecked(item.id, newChecked)
        _items.value = items.value.orEmpty().map {
            if (it.id == item.id) it.copy(checked = newChecked) else it
        }
    }

    fun deleteItem(item: BasketItem) {
        BasketRepository.deleteItem(item.id)
        _items.value = items.value.orEmpty().filter { it.id != item.id }
    }

    fun clearBasket() {
        val uid = UserProvider.currentUid() ?: return
        BasketRepository.clearBasket(uid)
        _items.value = emptyList()
    }

    fun getTotal(): Double =
        items.value.orEmpty()
            .filter { !it.checked }
            .sumOf { it.price.replace("R", "").toDoubleOrNull() ?: 0.0 }
}