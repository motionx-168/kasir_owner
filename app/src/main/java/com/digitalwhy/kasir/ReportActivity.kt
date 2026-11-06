package com.digitalwhy.kasir

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ReportActivity : AppCompatActivity() {

    private lateinit var tvPeriodSubtitle: TextView
    private lateinit var tvOmzet: TextView
    private lateinit var tvHpp: TextView
    private lateinit var tvLabaKotor: TextView
    private lateinit var tvPengeluaran: TextView
    private lateinit var tvLabaBersih: TextView
    private lateinit var tvTransaksi: TextView
    private lateinit var chartRevenue: LineChart
    private lateinit var rvReportItems: RecyclerView
    private lateinit var tvNoTransactions: TextView

    private val transactionAdapter = ReportTransactionAdapter()

    private var period: String = "30_days"
    private var startDate: String? = null
    private var endDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        if (savedInstanceState != null) {
            period = savedInstanceState.getString("period") ?: "30_days"
            startDate = savedInstanceState.getString("start_date")
            endDate = savedInstanceState.getString("end_date")
        }

        bindViews()
        setupRecyclerView()
        setupChart()
        setupActions()
        updatePeriodLabel()
        loadReport()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("period", period)
        outState.putString("start_date", startDate)
        outState.putString("end_date", endDate)
        super.onSaveInstanceState(outState)
    }

    private fun bindViews() {
        tvPeriodSubtitle = findViewById(R.id.tvPeriodSubtitle)
        tvOmzet = findViewById(R.id.tvOmzet)
        tvHpp = findViewById(R.id.tvHpp)
        tvLabaKotor = findViewById(R.id.tvLabaKotor)
        tvPengeluaran = findViewById(R.id.tvPengeluaran)
        tvLabaBersih = findViewById(R.id.tvLabaBersih)
        tvTransaksi = findViewById(R.id.tvTransaksi)
        chartRevenue = findViewById(R.id.chartRevenue)
        rvReportItems = findViewById(R.id.rvReportItems)
        tvNoTransactions = findViewById(R.id.tvNoTransactions)
    }

    private fun setupRecyclerView() {
        rvReportItems.layoutManager = LinearLayoutManager(this)
        rvReportItems.adapter = transactionAdapter
    }

    private fun setupActions() {
        findViewById<MaterialButton>(R.id.btnSelectPeriod).setOnClickListener {
            showPeriodSheet()
        }
        findViewById<MaterialButton>(R.id.btnExportExcel).setOnClickListener {
            Toast.makeText(this, "Export Excel segera hadir", Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialButton>(R.id.btnExportPdf).setOnClickListener {
            Toast.makeText(this, "Export PDF segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupChart() {
        val secondary = ContextCompat.getColor(this, R.color.text_secondary)
        val grid = ContextCompat.getColor(this, R.color.dw_border)

        chartRevenue.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setNoDataText("Belum ada data grafik")
            setNoDataTextColor(secondary)
            setBackgroundColor(Color.TRANSPARENT)
            extraLeftOffset = 8f
            extraRightOffset = 8f
            extraTopOffset = 8f
            extraBottomOffset = 8f

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.granularity = 1f
            xAxis.textColor = secondary
            xAxis.axisLineColor = grid

            axisLeft.textColor = secondary
            axisLeft.gridColor = grid
            axisLeft.axisLineColor = grid
            axisLeft.setDrawAxisLine(false)
            axisRight.isEnabled = false
        }
    }

    private fun updatePeriodLabel() {
        val label = when (period) {
            "today" -> "Hari Ini"
            "7_days" -> "7 Hari Terakhir"
            "30_days" -> "30 Hari Terakhir"
            "custom" -> {
                if (!startDate.isNullOrBlank() && !endDate.isNullOrBlank()) {
                    "${formatDisplayDate(startDate)} - ${formatDisplayDate(endDate)}"
                } else {
                    "Rentang Kustom"
                }
            }
            else -> "30 Hari Terakhir"
        }
        tvPeriodSubtitle.text = "Periode: $label"
    }

    private fun showPeriodSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_period_picker, null)
        dialog.setContentView(view)

        view.findViewById<View>(R.id.optionToday).setOnClickListener {
            dialog.dismiss()
            selectPreset("today")
        }
        view.findViewById<View>(R.id.option7Days).setOnClickListener {
            dialog.dismiss()
            selectPreset("7_days")
        }
        view.findViewById<View>(R.id.option30Days).setOnClickListener {
            dialog.dismiss()
            selectPreset("30_days")
        }
        view.findViewById<View>(R.id.optionCustom).setOnClickListener {
            dialog.dismiss()
            showCustomDateRange()
        }

        dialog.setOnShowListener {
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundResource(R.drawable.bg_bottom_sheet)
        }
        dialog.window?.setDimAmount(0.7f)
        dialog.show()
    }

    private fun selectPreset(newPeriod: String) {
        period = newPeriod
        startDate = null
        endDate = null
        updatePeriodLabel()
        loadReport()
    }

    private fun showCustomDateRange() {
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -29)
        }
        val startPicker = DatePickerDialog(
            this,
            R.style.Theme_DigitalWhy_DatePicker,
            { _, year, month, day ->
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                showEndDatePicker(startCal)
            },
            start.get(Calendar.YEAR),
            start.get(Calendar.MONTH),
            start.get(Calendar.DAY_OF_MONTH)
        )
        startPicker.datePicker.maxDate = System.currentTimeMillis()
        startPicker.show()
    }

    private fun showEndDatePicker(startCal: Calendar) {
        val defaultEnd = Calendar.getInstance()
        val endPicker = DatePickerDialog(
            this,
            R.style.Theme_DigitalWhy_DatePicker,
            { _, year, month, day ->
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                if (endCal.before(startCal)) {
                    Toast.makeText(this, "Tanggal akhir tidak boleh sebelum tanggal awal", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                period = "custom"
                startDate = formatApiDate(startCal)
                endDate = formatApiDate(endCal)
                updatePeriodLabel()
                loadReport()
            },
            defaultEnd.get(Calendar.YEAR),
            defaultEnd.get(Calendar.MONTH),
            defaultEnd.get(Calendar.DAY_OF_MONTH)
        )
        endPicker.datePicker.minDate = startCal.timeInMillis
        endPicker.datePicker.maxDate = System.currentTimeMillis()
        endPicker.show()
    }

    private fun loadReport() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getReport(period, startDate, endDate)
                val body = response.body()
                if (response.isSuccessful && body?.status == "success" && body.data != null) {
                    bindReport(body.data)
                } else {
                    Toast.makeText(
                        this@ReportActivity,
                        "Gagal memuat laporan (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ReportActivity,
                    "Error Jaringan: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun bindReport(data: ReportData) {
        tvOmzet.text = formatRupiah(data.omzet)
        tvHpp.text = formatRupiah(data.hpp)
        tvLabaKotor.text = formatRupiah(data.labaKotor)
        tvPengeluaran.text = formatRupiah(data.pengeluaran)
        tvLabaBersih.text = formatRupiah(data.labaBersih)
        tvTransaksi.text = data.transaksi.toString()

        renderChart(data.chartLabels, data.chartData)

        transactionAdapter.setData(data.recentTransactions)
        tvNoTransactions.visibility =
            if (data.recentTransactions.isEmpty()) View.VISIBLE else View.GONE
        rvReportItems.visibility =
            if (data.recentTransactions.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun renderChart(labels: List<String>, values: List<Double>) {
        if (values.isEmpty()) {
            chartRevenue.clear()
            chartRevenue.invalidate()
            return
        }

        val teal = ContextCompat.getColor(this, R.color.dw_teal)
        val entries = values.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }

        val dataSet = LineDataSet(entries, "Omzet").apply {
            color = teal
            setCircleColor(teal)
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = teal
            fillAlpha = 45
            highLightColor = Color.WHITE
            setHighlightLineWidth(1f)
        }

        chartRevenue.data = LineData(dataSet)
        chartRevenue.xAxis.valueFormatter = IndexAxisValueFormatter(labels.toTypedArray())
        chartRevenue.xAxis.labelCount = labels.size.coerceIn(2, 7)
        chartRevenue.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when {
                    value >= 1_000_000f -> String.format(Locale.getDefault(), "%.1fJt", value / 1_000_000f)
                    value >= 1_000f -> String.format(Locale.getDefault(), "%.0fRb", value / 1_000f)
                    else -> value.toInt().toString()
                }
            }
        }
        chartRevenue.notifyDataSetChanged()
        chartRevenue.invalidate()
    }

    private fun formatRupiah(value: Double): String {
        val format = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
        return "Rp ${format.format(value)}"
    }

    private fun formatApiDate(calendar: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    private fun formatDisplayDate(apiDate: String?): String {
        if (apiDate.isNullOrBlank()) return "-"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(apiDate)
            if (date != null) {
                SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(date)
            } else {
                apiDate
            }
        } catch (e: Exception) {
            apiDate
        }
    }

    private fun formatDateTime(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"
        return try {
            val base = raw.replace('T', ' ').take(19)
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = if (raw.endsWith("Z")) TimeZone.getTimeZone("UTC") else TimeZone.getDefault()
            }
            val date = parser.parse(base)
            if (date != null) {
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(date)
            } else {
                raw
            }
        } catch (e: Exception) {
            raw
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private inner class ReportTransactionAdapter :
        RecyclerView.Adapter<ReportTransactionAdapter.ViewHolder>() {

        private var items: List<ReportTransaction> = emptyList()

        fun setData(newItems: List<ReportTransaction>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_report_transaction, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvTitle: TextView = view.findViewById(R.id.tvTransactionTitle)
            private val tvDate: TextView = view.findViewById(R.id.tvTransactionDate)
            private val tvTotal: TextView = view.findViewById(R.id.tvTransactionTotal)

            fun bind(item: ReportTransaction) {
                tvTitle.text = when {
                    !item.invoice.isNullOrBlank() -> item.invoice
                    !item.productName.isNullOrBlank() -> item.productName
                    item.id > 0 -> "Transaksi #${item.id}"
                    else -> "Transaksi"
                }
                tvDate.text = formatDateTime(item.createdAt)
                tvTotal.text = formatRupiah(item.totalAmount)
            }
        }
    }
}
