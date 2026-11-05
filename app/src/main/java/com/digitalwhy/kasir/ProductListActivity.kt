package com.digitalwhy.kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProductListActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private val adapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_product_list)

            rvProducts = findViewById(R.id.rvProducts)
            rvProducts.layoutManager = LinearLayoutManager(this)
            rvProducts.adapter = adapter

            loadProducts()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getAllProducts()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val products = response.body()?.data ?: emptyList()
                    adapter.setData(products)
                } else {
                    Toast.makeText(this@ProductListActivity, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
        private var products: List<Product> = emptyList()

        fun setData(newProducts: List<Product>) {
            products = newProducts
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = products[position]
            holder.bind(product)
        }

        override fun getItemCount() = products.size

        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            private val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
            private val tvName: TextView = view.findViewById(R.id.tvName)
            private val tvBarcode: TextView = view.findViewById(R.id.tvBarcode)
            private val tvPrice: TextView = view.findViewById(R.id.tvPrice)
            private val tvStock: TextView = view.findViewById(R.id.tvStock)

            fun bind(product: Product) {
                tvName.text = product.name ?: "Tanpa Nama"
                tvBarcode.text = product.barcode ?: "-"

                val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
                tvPrice.text = runCatching { format.format(product.price ?: 0.0) }.getOrDefault("-")
                tvStock.text = "Stok: ${product.stock ?: 0.0}"

                val iconRes = when (product.icon) {
                    "egg" -> R.drawable.ic_lucide_egg
                    "milk" -> R.drawable.ic_lucide_milk
                    "wheat" -> R.drawable.ic_lucide_wheat
                    else -> R.drawable.ic_lucide_package
                }
                ivIcon.setImageResource(iconRes)
            }
        }
    }
}
