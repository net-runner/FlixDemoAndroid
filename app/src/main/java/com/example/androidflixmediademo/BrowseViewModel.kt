package com.example.androidflixmediademo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

class BrowseViewModel(private val preferenceManager: ProductPreferenceManager) : ViewModel() {
    var mpn by mutableStateOf("")
    var ean by mutableStateOf("")
    var distId by mutableStateOf("")
    var isoCode by mutableStateOf("")
    var flIsoCode by mutableStateOf("")
    var brand by mutableStateOf("")
    var title by mutableStateOf("")
    var price by mutableStateOf("")
    var currency by mutableStateOf("")

    init {
        viewModelScope.launch {
            preferenceManager.productData.collect { state ->
                mpn = state.mpn
                ean = state.ean
                distId = state.distId
                isoCode = state.isoCode
                flIsoCode = state.flIsoCode
                brand = state.brand
                title = state.title
                price = state.price
                currency = state.currency
            }
        }
    }

    fun updateField(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        viewModelScope.launch {
            preferenceManager.saveField(key, value)
        }
    }
}



class BrowseViewModelFactory(private val preferenceManager: ProductPreferenceManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrowseViewModel(preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}