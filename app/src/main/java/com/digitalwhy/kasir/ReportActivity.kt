package com.digitalwhy.kasir

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class ReportActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        webView = findViewById(R.id.webViewReport)

        // PENGATURAN ANTI-CACHE
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()

        // Tambahkan timestamp di URL agar selalu dianggap halaman baru
        val timestamp = System.currentTimeMillis()
        val url = "http://127.0.0.1:8002/laporan?v=9999
        
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
