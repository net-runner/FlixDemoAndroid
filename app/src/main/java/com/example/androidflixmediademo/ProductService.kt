package com.example.androidflixmediademo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class ProductDTO(
    val mpn: String,
    val ean: String? = null,
    val isoCode: String? = null,
    val title: String? = null,
    val price: String? = null,
    val category: String? = null,
    val createdAt: String? = null,
    val image: String? = null
)

data class RemoteProduct(
    val id: String,
    val mpn: String,
    val ean: String,
    val isoCode: String,
    val title: String,
    val price: String,
    val category: String?,
    val imageURL: String?,
    val createdAt: String?
) {
    companion object {
        fun fromDto(dto: ProductDTO) = RemoteProduct(
            id = dto.mpn,
            mpn = dto.mpn,
            ean = dto.ean ?: "",
            isoCode = dto.isoCode ?: "",
            title = dto.title ?: dto.mpn,
            price = dto.price ?: "",
            category = dto.category,
            imageURL = dto.image,
            createdAt = dto.createdAt
        )
    }
}

class ProductService(
    private val endpoint: String = "https://demo.flix360.io/mobile-api/supporting/home-product-list.json"
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchProducts(): List<ProductDTO> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw Exception("Invalid response: $responseCode")
                }
                val data = connection.inputStream.bufferedReader().readText()
                json.decodeFromString<List<ProductDTO>>(data)
            } finally {
                connection.disconnect()
            }
        }
    }
}