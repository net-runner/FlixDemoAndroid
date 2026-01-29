package com.example.androidflixmediademo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.URLEncoder

@Composable
fun BrowseScreen(navController: NavController, viewModel: BrowseViewModel) {
    val isFormValid = viewModel.mpn.isNotBlank() &&
            viewModel.distId.isNotBlank() &&
            viewModel.isoCode.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Browse Product", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        Text("Product Identifiers", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.mpn,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.MPN, it) },
            label = { Text("MPN (Required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = viewModel.ean,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.EAN, it) },
            label = { Text("EAN") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
        )

        OutlinedTextField(
            value = viewModel.distId,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.DIST_ID, it) },
            label = { Text("Distributor ID (Required)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = viewModel.brand,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.BRAND, it) },
            label = { Text("Brand") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.TITLE, it) },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = viewModel.price,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.PRICE, it) },
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = viewModel.currency,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.CURRENCY, it) },
            label = { Text("Currency") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Locale", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.isoCode,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.ISO_CODE, it) },
            label = { Text("ISO Code (Required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = viewModel.flIsoCode,
            onValueChange = { viewModel.updateField(ProductPreferenceManager.FL_ISO_CODE, it) },
            label = { Text("FL ISO Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val encodedMpn = URLEncoder.encode(viewModel.mpn, "UTF-8")
                val encodedEan = URLEncoder.encode(viewModel.ean, "UTF-8")
                val encodedDistId = URLEncoder.encode(viewModel.distId, "UTF-8")
                val encodedIsoCode = URLEncoder.encode(viewModel.isoCode, "UTF-8")
                val encodedBrand = URLEncoder.encode(viewModel.brand, "UTF-8")
                val encodedTitle = URLEncoder.encode(viewModel.title, "UTF-8")
                val encodedPrice = URLEncoder.encode(viewModel.price, "UTF-8")

                navController.navigate("product_detail/$encodedMpn/$encodedEan/$encodedDistId/$encodedIsoCode/$encodedBrand/$encodedTitle/$encodedPrice/")
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse", fontWeight = FontWeight.SemiBold)
        }
    }
}