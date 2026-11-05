package com.digitalwhy.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvProfit: TextView
    private lateinit var tvSubInfo: TextView
    private lateinit var tvLowStock: TextView
    private lateinit var tvExpired: TextView
    private lateinit var btnManageProducts: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        tvProfit = findViewById(R.id.tvProfit)
        tvSubInfo = findViewById(R.id.tvSubInfo)
        tvLowStock = findViewById(R.id.tvLowStock)
        tvExpired = findViewById(R.id.tvExpired)
        btnManageProducts = findViewById(R.id.btnManageProducts)

        btnManageProducts.setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
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
                        
                        tvProfit.text = format.format(data.profit).replace("Rp", "Rp ")
                        val omzet = format.format(data.revenue).replace("Rp", "Rp ")
                        tvSubInfo.text = "Omzet $omzet • ${data.transactions_count} transaksi"
                        tvLowStock.text = "${data.low_stock_count} Item"
                        tvExpired.text = "${data.expiring_count} Item"
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
