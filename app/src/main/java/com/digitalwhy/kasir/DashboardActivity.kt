package com.digitalwhy.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvRevenue: TextView
    private lateinit var tvProfit: TextView
    private lateinit var tvLowStock: TextView
    private lateinit var tvExpired: TextView
    private lateinit var btnKasir: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Status bar transparan
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        // Inisialisasi View
        tvRevenue = findViewById(R.id.tvRevenue)
        tvProfit = findViewById(R.id.tvProfit)
        tvLowStock = findViewById(R.id.tvLowStock)
        tvExpired = findViewById(R.id.tvExpired)
        btnKasir = findViewById(R.id.btnKasir)

        // Set tanggal hari ini (Opsional, jika ada ID text tanggalnya)
        // Misal: findViewById<TextView>(R.id.tvDate).text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())

        // Navigasi ke POS (jika tombol ada di aplikasi owner)
        btnKasir.setOnClickListener {
            // Karena di aplikasi Owner kita tidak punya scanner, kita bisa arahkan ke Activity lain atau Toast
            Toast.makeText(this, "Buka aplikasi Digital Why POS untuk mulai berjualan", Toast.LENGTH_LONG).show()
        }

        loadDashboardData()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getDashboardData()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) {
                        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                            maximumFractionDigits = 0
                        }

                        tvRevenue.text = format.format(data.revenue)
                        tvProfit.text = format.format(data.profit) // Sementara kita kosongkan atau isi dengan estimasi jika ada
                        tvLowStock.text = data.low_stock_count.toString()
                        tvExpired.text = data.expiring_count.toString()
                    }
                } else {
                    Toast.makeText(this@DashboardActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "Error Jaringan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
