package com.rtech.cartly.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rtech.cartly.data.StatsRepository

class ProfileViewModel : ViewModel() {

    private val _basketCount = MutableLiveData(0)
    val basketCount: LiveData<Int> = _basketCount

    private val _favouritesCount = MutableLiveData(0)
    val favouritesCount: LiveData<Int> = _favouritesCount

    fun loadStats(uid: String?) {
        if (uid == null) {
            _basketCount.value = 0
            _favouritesCount.value = 0
            return
        }
        StatsRepository.loadBasketCount(uid).addOnSuccessListener { _basketCount.value = it }
        StatsRepository.loadFavouritesCount(uid).addOnSuccessListener { _favouritesCount.value = it }
    }
}