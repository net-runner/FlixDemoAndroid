package com.example.androidflixmediademo

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import com.flixmedia.flixmediasdk.webview.FlixWebViewLogger
import com.flixmedia.flixmediasdk.webview.WebViewState

@Composable
fun AccordionScreen() {
    var isExpanded by remember { mutableStateOf(false) }
    val logger = remember { FlixWebViewLogger() }
    val scope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    var columnTopOnScreen by remember { mutableIntStateOf(0) }
    var webViewState by remember { mutableStateOf<WebViewState?>(null) }

    val params = ProductViewArguments(
        mpn = "lego_10297",
        ean = "5702017151847",
        distId = "6",
        isoCode = "it",
        flIsoCode = "",
        brand = "Lego",
        title = "Lego Boutique Hotel Game Toy",
        price = "300.0",
        currency = "USD"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                columnTopOnScreen = pos.y.toInt()
            }
    ) {
        Text(params.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .padding(16.dp),
            tint = Color.Gray
        )

        Text("Price: $${params.price}", style = MaterialTheme.typography.headlineSmall)

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Full product description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            FlixWebViewContainer(
                params = params,
                logger = logger,
                columnTopOnScreen = columnTopOnScreen,
                scrollState = scrollState,
                onStateChange = { state -> webViewState = state }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec erat justo, varius eget commodo vitae, lacinia ut tortor. Curabitur dictum orci et lectus sollicitudin, eu malesuada elit semper.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (webViewState !is WebViewState.Error) {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            logger.callLogFromAppSync("cartButtonTapped")
                        } catch (e: Exception) {
                            Log.e("Flix", "Logging failed", e)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Buy now")
            }
        }
    }
}