package com.rtech.cartly.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.rtech.cartly.model.Deal
import com.rtech.cartly.ui.components.DealCard
import com.rtech.cartly.viewmodel.DealsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    viewModel: DealsViewModel,
    onDealClick: (Deal) -> Unit
) {
    val deals by viewModel.deals.observeAsState(initial = emptyList())
    val categories by viewModel.categories.observeAsState(initial = emptyList())
    val favourites by viewModel.favourites.observeAsState(initial = emptySet())
    val isLoading by viewModel.isLoading.observeAsState(initial = true)
    val locationLabel by viewModel.locationLabel.observeAsState(initial = "Nearby")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var refreshing by remember { mutableStateOf(false) }
    var selectedStore by rememberSaveable { mutableStateOf("All") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var locationStarted by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { obtainGpsLocation(context, viewModel) }
        } else {
            scope.launch { obtainGeocodedLocation(context, viewModel) }
        }
    }

    val categoryOptions = remember(categories) {
        listOf("All") + categories.distinct()
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        if (locationStarted) return@LaunchedEffect
        locationStarted = true
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) obtainGpsLocation(context, viewModel)
        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(isLoading) {
        if (!isLoading) refreshing = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DealsHeader(locationLabel)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Checkers", "Pick n Pay", "Shoprite", "Spar").forEach { store ->
                FilterPill(
                    label = store,
                    active = selectedStore == store,
                    onClick = {
                        selectedStore = store
                        viewModel.setStoreFilter(store)
                    }
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryOptions) { category ->
                FilterPill(
                    label = category,
                    active = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                        viewModel.setCategoryFilter(category)
                    }
                )
            }
        }

        Text(
            text = "This week's specials",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading && deals.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = refreshing,
                        onRefresh = {
                            refreshing = true
                            viewModel.refresh()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (deals.isEmpty()) {
                            Text(
                                text = "No deals found",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(top = 10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(deals, key = { it.id }) { deal ->
                                    DealCard(
                                        deal = deal,
                                        isFavourite = deal.name in favourites,
                                        onClick = { onDealClick(deal) },
                                        onFavouriteClick = { viewModel.toggleFavourite(deal.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DealsHeader(locationLabel: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = "Cartly",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = locationLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun FilterPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (active) Color.Transparent
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (active) Color.White else MaterialTheme.colorScheme.primary
        )
    }
}

private suspend fun obtainGpsLocation(context: Context, viewModel: DealsViewModel) {
    val fused = LocationServices.getFusedLocationProviderClient(context)
    val tokenSource = CancellationTokenSource()
    try {
        val location = withTimeoutOrNull(15_000) {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .awaitOrNull()
        } ?: fused.lastLocation.awaitOrNull()

        if (location != null) {
            viewModel.setUserLocation(location.latitude, location.longitude)
            viewModel.setLocationLabel(reverseGeocode(context, location.latitude, location.longitude))
        } else {
            obtainGeocodedLocation(context, viewModel)
        }
    } finally {
        tokenSource.cancel()
    }
}

private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String =
    withContext(Dispatchers.IO) {
        runCatching {
            val addresses = Geocoder(context).getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                listOfNotNull(address.subLocality, address.locality)
                    .distinct()
                    .joinToString(", ")
            } else {
                ""
            }
        }.getOrDefault("")
    }

private suspend fun obtainGeocodedLocation(context: Context, viewModel: DealsViewModel) {
    withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("CartlyPrefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("location", "").orEmpty()
        if (saved.isBlank()) {
            viewModel.clearUserLocation()
            viewModel.setLocationLabel("Nearby")
            return@withContext
        }
        val address = runCatching {
            Geocoder(context).getFromLocationName(saved, 1)?.firstOrNull()
        }.getOrNull()
        if (address != null) {
            viewModel.setUserLocation(address.latitude, address.longitude)
            viewModel.setLocationLabel(saved)
        } else {
            viewModel.clearUserLocation()
            viewModel.setLocationLabel("Nearby")
        }
    }
}

private suspend fun <T> Task<T>.awaitOrNull(): T? =
    suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (cont.isActive) {
                if (task.isSuccessful) cont.resume(task.result)
                else cont.resume(null)
            }
        }
    }