package com.example.androidflixmediademo

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch

import com.flixmedia.flixmediasdk.models.ProductRequestParameters
import com.flixmedia.flixmediasdk.WebViewConfiguration
import com.flixmedia.flixmediasdk.webview.WebViewState
import com.flixmedia.flixmediasdk.webview.FlixWebViewLogger
import com.flixmedia.flixmediasdk.webview.FlixWebView

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProductDetailScreen(
    params: ProductViewArguments,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val flixLogger = remember { FlixWebViewLogger() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var columnTopOnScreen by remember { mutableIntStateOf(0) }
    var webViewState by remember { mutableStateOf<WebViewState?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInWindow()
                    columnTopOnScreen = pos.y.toInt()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            with(sharedTransitionScope) {
                Text(
                    text = params.title ?: "Product name",
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "text-${params.mpn}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                SubcomposeAsyncImage(
                    model = params.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .height(250.dp)
                        .padding(horizontal = 16.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "image-${params.mpn}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    },
                    error = {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                )
            }

            Text(
                text = "Price: $${params.price ?: "99.99"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            FlixWebViewContainer(
                params = params,
                logger = flixLogger,
                columnTopOnScreen = columnTopOnScreen,
                scrollState = scrollState,
                onStateChange = { webViewState = it }
            )
        }

        if (webViewState !is WebViewState.Error) {
            Button(
                onClick = {
                    try {
                        flixLogger.callLogFromAppSync("cartButtonTapped")
                        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("Flix", "Logging failed", e)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Text("Buy now", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FlixWebViewContainer(
    params: ProductViewArguments,
    logger: FlixWebViewLogger,
    columnTopOnScreen: Int,
    scrollState: androidx.compose.foundation.ScrollState,
    onStateChange: (WebViewState?) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var webViewHeightPx by remember { mutableIntStateOf(200) }
    var webViewState by remember { mutableStateOf<WebViewState?>(null) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    val configuration = remember(params) {
        WebViewConfiguration(
            params = ProductRequestParameters(
                mpn = params.mpn,
                ean = params.ean,
                distId = params.distId,
                isoCode = params.isoCode,
                flIsoCode = params.flIsoCode,
                brand = params.brand,
                title = params.title,
                price = params.price,
                currency = params.currency
            ),
            baseURL = "https://www.example.com"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp)
            .height(webViewHeightPx.dp)
    ) {
        key(reloadTrigger) {
            FlixWebView(
                configuration = configuration,
                logger = logger,
                modifier = Modifier.fillMaxSize(),
                onHeightChange = { newHeightPx ->
                    if (newHeightPx > 0) webViewHeightPx = newHeightPx
                },
                onStateChange = { state ->
                    webViewState = state
                    onStateChange(state)
                },
                onRequestScroll = { absoluteYpx, animated ->
                    coroutineScope.launch {
                        val targetOffset = (absoluteYpx - columnTopOnScreen).coerceAtLeast(0)
                        if (animated) scrollState.animateScrollTo(targetOffset)
                        else scrollState.scrollTo(targetOffset)
                    }
                }
            )
        }

        if (webViewState is WebViewState.Loading) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp,
                    color = Color.Blue
                )
            }
        }

        val error = (webViewState as? WebViewState.Error)?.throwable
        if (webViewState is WebViewState.Error) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color = Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        modifier = Modifier.size(56.dp),
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error?.localizedMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        maxLines = 3,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            reloadTrigger++
                        }) {
                            Text(text = "Retry")
                        }
                    }
                }
            }
        }
    }
}