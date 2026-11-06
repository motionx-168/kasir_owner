package com.digitalwhy.kasir

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvDate: TextView
    private lateinit var tvProfit: TextView
    private lateinit var tvSubInfo: TextView
    private lateinit var tvLowStock: TextView
    private lateinit var tvExpired: TextView
    private lateinit var btnManageProducts: MaterialCardView
    private lateinit var btnLaporan: MaterialCardView
    private lateinit var chartDashboard: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        tvDate = findViewById(R.id.tvDate)
        tvProfit = findViewById(R.id.tvProfit)
        tvSubInfo = findViewById(R.id.tvSubInfo)
        tvLowStock = findViewById(R.id.tvLowStock)
        tvExpired = findViewById(R.id.tvExpired)
        btnManageProducts = findViewById(R.id.btnManageProducts)
        btnLaporan = findViewById(R.id.btnLaporan)
        chartDashboard = findViewById(R.id.chartDashboard)

        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        tvDate.text = dateFormat.format(Date())

        btnManageProducts.setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
        }

        btnLaporan.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_report -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                    true
                }
                R.id.nav_stock -> {
                    startActivity(Intent(this, ProductListActivity::class.java))
                    true
                }
                R.id.nav_home -> true
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profil segera hadir", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        findViewById<FloatingActionButton>(R.id.fabScan).setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.digitalwhy.kasir")
            if (intent != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this@DashboardActivity, "Aplikasi POS tidak ditemukan. Pastikan sudah terinstal.", Toast.LENGTH_LONG).show()
            }
        }

        loadDashboardData()
    }

    private fun setupChart(labels: List<String>, values: List<Double>) {
        val teal = ContextCompat.getColor(this, R.color.dw_teal)
        val textPrimary = ContextCompat.getColor(this, R.color.text_primary)
        val textSecondary = ContextCompat.getColor(this, R.color.text_secondary)
        val grid = ContextCompat.getColor(this, R.color.dw_border)

        chartDashboard.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setNoDataText("Belum ada data grafik")
            setNoDataTextColor(textSecondary)
            setBackgroundColor(Color.TRANSPARENT)
            extraLeftOffset = 8f
            extraRightOffset = 8f
            extraTopOffset = 8f
            extraBottomOffset = 8f

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.granularity = 1f
            xAxis.textColor = textSecondary
            xAxis.axisLineColor = grid

            axisLeft.textColor = textSecondary
            axisLeft.gridColor = grid
            axisLeft.axisLineColor = grid
            axisLeft.setDrawAxisLine(false)
            axisRight.isEnabled = false
        }

        if (values.isEmpty()) {
            chartDashboard.clear()
            chartDashboard.invalidate()
            return
        }

        val entries = values.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }

        val gradientFill = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.argb(90, 0, 191, 165), Color.TRANSPARENT)
        )

        val dataSet = LineDataSet(entries, "Omzet").apply {
            color = teal
            setCircleColor(teal)
            lineWidth = 2.5f
            circleRadius = 3.5f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            setFillDrawable(gradientFill)
            highLightColor = textPrimary
            setHighlightLineWidth(1f)
        }

        chartDashboard.data = LineData(dataSet)
        chartDashboard.xAxis.valueFormatter = IndexAxisValueFormatter(labels.toTypedArray())
        chartDashboard.xAxis.labelCount = labels.size.coerceIn(2, 7)
        chartDashboard.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when {
                    value >= 1_000_000f -> String.format(Locale.getDefault(), "%.1fJt", value / 1_000_000f)
                    value >= 1_000f -> String.format(Locale.getDefault(), "%.0fRb", value / 1_000f)
                    else -> value.toInt().toString()
                }
            }
        }
        chartDashboard.notifyDataSetChanged()
        chartDashboard.invalidate()
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
                        
                        tvProfit.text = format.format(data.revenue).replace("Rp", "Rp ")
                        
                        if (data.transactions_count == 0) {
                            tvSubInfo.text = "Belum ada transaksi hari ini."
                        } else {
                            val laba = format.format(data.profit).replace("Rp", "Rp ")
                            tvSubInfo.text = "Laba $laba • ${data.transactions_count} transaksi"
                        }
                        
                        tvLowStock.text = "${data.low_stock_count} Item"
                        tvExpired.text = "${data.expiring_count} Item"

                        setupChart(data.chart_labels, data.chart_data)
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
