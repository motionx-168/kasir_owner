package com.digitalwhy.kasir

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- Model Response Produk ---
data class ProductResponse(
data class ProductsResponse(
    val status: String,
    val data: List<Product>?
)

    val status: String,
    val data: Product?
)

// --- Model Request & Response Transaksi ---
data class TransactionItemRequest(
    val product_id: Int,
    val quantity: Int
)

data class TransactionRequest(
    val items: List<TransactionItemRequest>
)

data class TransactionResponse(
    val status: String,
    val message: String?
)

// --- Model Response Dashboard ---
data class DashboardData(
    val profit: Double,
    val revenue: Double,
    val transactions_count: Int,
    val low_stock_count: Int,
    val expiring_count: Int
)

data class DashboardResponse(
    val status: String,
    val data: DashboardData
)

// --- Interface API ---
interface ApiService {
    @GET("api/products")
    suspend fun getAllProducts(): Response<ProductsResponse>

    @GET("api/products/{barcode}")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Response<ProductResponse>

    @POST("api/products")
    suspend fun addProduct(@Body product: Product): Response<ProductResponse>

    @POST("api/transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): Response<TransactionResponse>

    @GET("api/dashboard")
    suspend fun getDashboardData(): Response<DashboardResponse>
}

// --- Retrofit Client ---
object ApiClient {
    private const val BASE_URL = "http://127.0.0.1:8002/"
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
