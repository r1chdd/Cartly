package com.rtech.cartly.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.rtech.cartly.data.DealsRepository
import com.rtech.cartly.data.FavouritesRepository
import com.rtech.cartly.data.StoreLocations
import com.rtech.cartly.data.UserProvider
import com.rtech.cartly.model.Deal

class DealsViewModel : ViewModel() {

    private val _deals = MutableLiveData<List<Deal>>(emptyList())
    val deals: LiveData<List<Deal>> = _deals

    private val _favourites = MutableLiveData<Set<String>>(emptySet())
    val favourites: LiveData<Set<String>> = _favourites

    private val _categories = MutableLiveData<List<String>>(emptyList())
    val categories: LiveData<List<String>> = _categories

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userLocation = MutableLiveData<GeoPoint?>(null)
    val userLocation: LiveData<GeoPoint?> = _userLocation

    private val _locationLabel = MutableLiveData("Nearby")
    val locationLabel: LiveData<String> = _locationLabel

    private var hasLoaded = false
    val isLoaded: Boolean get() = hasLoaded

    private var loadToken = 0

    var selectedStore = "All"
    var selectedCategory = "All"

    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = GeoPoint(latitude, longitude)
        reapplyLocation()
    }

    fun clearUserLocation() {
        _userLocation.value = null
        reapplyLocation()
    }

    fun setLocationLabel(label: String) {
        _locationLabel.value = if (label.isBlank()) "Nearby" else label
    }

    private fun reapplyLocation() {
        if (hasLoaded) _deals.value = decorate(_deals.value.orEmpty())
    }

    private fun decorate(rawList: List<Deal>): List<Deal> {
        val location = _userLocation.value
        val labelled = rawList.map { deal ->
            val label = StoreLocations.distanceLabel(deal.store, location)
            if (label != null) deal.copy(distance = label) else deal
        }
        if (location == null) return labelled
        return labelled.sortedBy { deal ->
            StoreLocations.nearestBranchDistance(deal.store, location) ?: Double.MAX_VALUE
        }
    }

    fun loadData() {
        if (hasLoaded) return
        refresh()
    }

    fun refresh() {
        _isLoading.value = true

        UserProvider.ensureSignedIn { uid ->
            if (uid == null) {
                _isLoading.value = false
                return@ensureSignedIn
            }

            FavouritesRepository.loadFavouriteNames(uid).addOnSuccessListener { names ->
                _favourites.value = names.toSet()
            }

            DealsRepository.loadCategories().addOnSuccessListener { list ->
                _categories.value = list
            }

            loadDeals(showLoading = true)
        }
    }

    fun setStoreFilter(store: String) {
        selectedStore = store
        loadDeals(showLoading = false)
    }

    fun setCategoryFilter(category: String) {
        selectedCategory = category
        loadDeals(showLoading = false)
    }

    private fun loadDeals(showLoading: Boolean) {
        val token = ++loadToken
        if (showLoading) _isLoading.value = true

        val store = selectedStore.takeUnless { it == "All" }
        val category = selectedCategory.takeUnless { it == "All" }

        DealsRepository.loadDeals(store, category)
            .addOnSuccessListener { list ->
                if (token != loadToken) return@addOnSuccessListener
                hasLoaded = true
                _deals.value = decorate(list)
                if (showLoading) _isLoading.value = false
            }
            .addOnFailureListener {
                if (token != loadToken) return@addOnFailureListener
                if (showLoading) _isLoading.value = false
            }
    }

    fun isFavourite(name: String): Boolean = name in favourites.value.orEmpty()

    fun toggleFavourite(name: String) {
        val uid = UserProvider.currentUid() ?: return
        val current = favourites.value.orEmpty()
        val isFav = name in current
        if (isFav) {
            FavouritesRepository.removeFavourite(uid, name)
            _favourites.value = current - name
        } else {
            FavouritesRepository.addFavourite(uid, name)
            _favourites.value = current + name
        }
    }
}