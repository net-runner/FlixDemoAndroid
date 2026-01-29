package com.example.androidflixmediademo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ProductFormState(
    val mpn: String = "lego_10297",
    val ean: String = "5702017151847",
    val distId: String = "6",
    val isoCode: String = "it",
    val flIsoCode: String = "",
    val brand: String = "Lego",
    val title: String = "Lego Boutique Hotel Game Toy",
    val price: String = "300",
    val currency: String = "USD"
)

private val Context.dataStore by preferencesDataStore(name = "product_prefs")

class ProductPreferenceManager(private val context: Context) {
    companion object {
        val MPN = stringPreferencesKey("mpn")
        val EAN = stringPreferencesKey("ean")
        val DIST_ID = stringPreferencesKey("dist_id")
        val ISO_CODE = stringPreferencesKey("iso_code")
        val FL_ISO_CODE = stringPreferencesKey("fl_iso_code")
        val BRAND = stringPreferencesKey("brand")
        val TITLE = stringPreferencesKey("title")
        val PRICE = stringPreferencesKey("price")
        val CURRENCY = stringPreferencesKey("currency")
    }

    suspend fun saveField(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }

    val productData: Flow<ProductFormState> = context.dataStore.data.map { prefs ->
        ProductFormState(
            mpn = prefs[MPN] ?: "lego_10297",
            ean = prefs[EAN] ?: "5702017151847",
            distId = prefs[DIST_ID] ?: "6",
            isoCode = prefs[ISO_CODE] ?: "it",
            flIsoCode = prefs[FL_ISO_CODE] ?: "",
            brand = prefs[BRAND] ?: "Lego",
            title = prefs[TITLE] ?: "Lego Boutique Hotel Game Toy",
            price = prefs[PRICE] ?: "300",
            currency = prefs[CURRENCY] ?: "USD"
        )
    }
}