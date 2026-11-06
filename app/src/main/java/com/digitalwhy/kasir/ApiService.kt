package com.digitalwhy.kasir

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ProductResponse(val status: String, val data: Product?)
data class ProductsResponse(val status: String, val data: List<Product>?)

data class DashboardData(
    val revenue: Double,
    val profit: Double,
    val transactions_count: Int,
    val low_stock_count: Int,
    val expiring_count: Int,
    @SerializedName("chart_labels") val chart_labels: List<String> = emptyList(),
    @SerializedName("chart_data") val chart_data: List<Double> = emptyList()
)
data class DashboardResponse(val status: String, val data: DashboardData)

data class ReportTransaction(
    val id: Long = 0,
    @SerializedName("total_amount") val totalAmount: Double = 0.0,
    @SerializedName("created_at") val createdAt: String? = null,
    val invoice: String? = null,
    @SerializedName("product_name") val productName: String? = null
)

data class ReportData(
    val omzet: Double = 0.0,
    val hpp: Double = 0.0,
    @SerializedName("laba_kotor") val labaKotor: Double = 0.0,
    val pengeluaran: Double = 0.0,
    @SerializedName("laba_bersih") val labaBersih: Double = 0.0,
    val transaksi: Int = 0,
    @SerializedName("chart_labels") val chartLabels: List<String> = emptyList(),
    @SerializedName("chart_data") val chartData: List<Double> = emptyList(),
    @SerializedName(
        value = "recent_transactions",
        alternate = ["transactions", "recent_items"]
    )
    val recentTransactions: List<ReportTransaction> = emptyList()
)

data class ReportResponse(val status: String, val data: ReportData?)

interface ApiService {
    @GET("api/products")
    suspend fun getAllProducts(): Response<ProductsResponse>

    @POST("api/products")
    suspend fun addProduct(@Body product: Product): Response<ProductResponse>

    @GET("api/dashboard")
    suspend fun getDashboardData(): Response<DashboardResponse>

    @GET("api/reports")
    suspend fun getReport(
        @Query("period") period: String,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): Response<ReportResponse>
}

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
